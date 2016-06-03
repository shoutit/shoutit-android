package com.shoutit.app.android.view.media;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraSelectionCriteria;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FocusMode;
import com.google.common.base.Preconditions;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PermissionHelper;

import java.util.List;

public class RecordMediaActivity extends AbstractCameraActivity
        implements CameraFragment.CameraFragmentListener {

    private static final String TAG_CAMERA = CameraFragment.class.getCanonicalName();
    public static final String EXTRA_IS_VIDEO = "extra_is_video";
    public static final String EXTRA_MEDIA = "extra_media";
    private static final String EXTRA_IS_FOR_RESULT = "extra_for_result";
    private static final String EXTRA_VIDEO_FIRST = "extra_video_first";
    private static final String EXTRA_IS_CHAT_MEDIA = "extra_is_chat_media";
    private static final String EXTRA_IS_FIRST_MEDIA = "extra_is_first_media";
    private static final String EXTRA_USE_EDITOR = "extra_use_editor";

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    public static final int REQUEST_CODE_PERMISSION_RECORD_AUDIO = 2;

    private CameraFragment cameraFragment;

    private boolean isActivityForResult;
    private boolean isVideoFirst;
    private boolean isChatMedia;
    private boolean isFirstMedia;
    private boolean useEditor;

    public static Intent newIntent(Context context, boolean activityForResult, boolean videoFirst, boolean isChatMedia, boolean firstMedia, boolean useEditor) {
        return new Intent(context, RecordMediaActivity.class)
                .putExtra(EXTRA_IS_FOR_RESULT, activityForResult)
                .putExtra(EXTRA_VIDEO_FIRST, videoFirst)
                .putExtra(EXTRA_IS_CHAT_MEDIA, isChatMedia)
                .putExtra(EXTRA_IS_FIRST_MEDIA, firstMedia)
                .putExtra(EXTRA_USE_EDITOR, useEditor);
    }

    @Override
    protected boolean needsOverlay() {
        return false;
    }

    @Override
    protected boolean needsActionBar() {
        return false;
    }

    @Override
    protected boolean isVideo() {
        return false;
    }

    @Override
    protected com.commonsware.cwac.cam2.CameraFragment buildFragment() {
        return null;
    }

    @Override
    protected String[] getNeededPermissions() {
        return new String[]{};
    }

    @Override
    protected void configEngine(CameraEngine cameraEngine) {
        // do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isActivityForResult = extras.getBoolean(EXTRA_IS_FOR_RESULT);
            isVideoFirst = extras.getBoolean(EXTRA_VIDEO_FIRST);
            isChatMedia = extras.getBoolean(EXTRA_IS_CHAT_MEDIA);
            isFirstMedia = extras.getBoolean(EXTRA_IS_FIRST_MEDIA);
            useEditor = extras.getBoolean(EXTRA_USE_EDITOR);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init() {
        if (!hasPermissions()) {
            return;
        }

        final Fragment fragment = getFragmentManager().findFragmentByTag(TAG_CAMERA);

        if (fragment == null) {
            cameraFragment = CameraFragment.newInstance(isVideoFirst, isChatMedia, isFirstMedia, useEditor);
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, cameraFragment, TAG_CAMERA)
                    .commit();
        } else if (fragment instanceof CameraFragment) {
            cameraFragment = (CameraFragment) fragment;
        }

        if (cameraFragment != null) {
            initCamera();
        }
    }

    private boolean hasPermissions() {
        return PermissionHelper.checkPermissions(
                this, REQUEST_CODE_PERMISSIONS, ColoredSnackBar.contentView(this),
                R.string.permission_camera_create_shout_explanation,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public void onInitializationFailed(Exception cause) {
    }

    @Override
    public void onMediaResult(Intent intent) {
        final List<String> media = intent.getExtras().getStringArrayList(CameraFragment.EXTRA_IMAGE_URI);
        final boolean isVideo = intent.getExtras().getBoolean(CameraFragment.EXTRA_IS_VIDEO);

        final String mediaFile;
        if (media == null) {
            final Uri uri = (Uri) intent.getExtras().get(CameraFragment.EXTRA_IMAGE_URI);
            Preconditions.checkNotNull(uri);
            mediaFile = uri.getPath();
        } else {
            mediaFile = media.get(0);
        }
        Preconditions.checkNotNull(mediaFile);

        if (isActivityForResult) {
            setResult(RESULT_OK, new Intent()
                    .putExtra(EXTRA_IS_VIDEO, isVideo)
                    .putExtra(EXTRA_MEDIA, mediaFile));
            finish();
        } else {
            final Fragment publishMediaShoutFragment = PublishMediaShoutFragment.newInstance(mediaFile, isVideo);
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, publishMediaShoutFragment, TAG_CAMERA)
                    .commit();
        }
    }

    private void initCamera() {
        FocusMode focusMode = (FocusMode) getIntent().getSerializableExtra(EXTRA_FOCUS_MODE);
        CameraController ctrlClassic = new CameraController(focusMode, true, isVideo());

        cameraFragment.setController(ctrlClassic);
        cameraFragment.setMirrorPreview(getIntent().getBooleanExtra(EXTRA_MIRROR_PREVIEW, false));

        SHCameraInfo shCameraInfo = SHCameraInfo.getInstance();

        if ((!shCameraInfo.isHasFrontFacingCamera() && !shCameraInfo.isHasBackFacingCamera())
                || shCameraInfo.getNumberOfCameras() == 0) {
            Toast.makeText(this, "no camera", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        CameraSelectionCriteria criteria =
                new CameraSelectionCriteria.Builder().facing(Facing.BACK).facingExactMatch(false).build();

        ctrlClassic.setEngine(CameraEngine.buildInstance(RecordMediaActivity.this, true),
                criteria);
        ctrlClassic.getEngine().setDebug(getIntent().getBooleanExtra(EXTRA_DEBUG_ENABLED, true));

        if (!cameraFragment.isVisible()) {
            getFragmentManager().beginTransaction().show(cameraFragment).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Fragment fragment = getFragmentManager().findFragmentByTag(TAG_CAMERA);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onEventMainThread(CameraController.ControllerDestroyedEvent event) {
        // dont destroy
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_RECORD_AUDIO && cameraFragment != null) {
            cameraFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted && requestCode == REQUEST_CODE_PERMISSIONS) {
                ColoredSnackBar.success(findViewById(android.R.id.content), R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
                init();
            } else if (!permissionsGranted) {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


}