package com.shoutit.app.android.view.media;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraSelectionCriteria;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FocusMode;
import com.google.common.base.Preconditions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.shoutit.app.android.R;

import java.util.List;

public class RecordVideoActivity extends AbstractCameraActivity
        implements CameraFragment.CameraFragmentListener {

    private static final String TAG_CAMERA = CameraFragment.class.getCanonicalName();

    private CameraFragment cameraFragment;
    private Bundle savedInstance;
    private MultiplePermissionsListener customMultiplePermissionsListener;

    public static Intent newIntent(Context context) {
        return new Intent(context, RecordVideoActivity.class);
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
        return new String[]{Manifest.permission.CAMERA};
    }

    @Override
    protected void configEngine(CameraEngine cameraEngine) {
        // do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.savedInstance = savedInstanceState;
        } else if (getIntent().getExtras() != null) {
            this.savedInstance = getIntent().getExtras();
        } else {
            this.savedInstance = new Bundle();
        }

        Dexter.continuePendingRequestsIfPossible(customMultiplePermissionsListener);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init() {
        final Fragment fragment = getFragmentManager().findFragmentByTag(TAG_CAMERA);

        if (fragment == null) {
            cameraFragment = CameraFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, cameraFragment, TAG_CAMERA)
                    .commit();
        } else if (fragment instanceof CameraFragment) {
            cameraFragment = (CameraFragment) fragment;
        }

        if (cameraFragment != null) {
            this.initPermissionLiseners();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Dexter.isRequestOngoing()) return;
                Dexter.checkPermissions(customMultiplePermissionsListener, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                initCamera();
            }
        }
    }

    private void initPermissionLiseners() {

        customMultiplePermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    initCamera();
                } else {
                    new AlertDialog.Builder(RecordVideoActivity.this).setTitle("denied permission")
                            .setMessage("denied permission")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setIcon(R.mipmap.ic_launcher)
                            .show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                           PermissionToken token) {
                token.continuePermissionRequest();
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(this.savedInstance);
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

        final Fragment publishMediaShoutFragment = PublishMediaShoutFragment.newInstance(mediaFile, isVideo);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, publishMediaShoutFragment, TAG_CAMERA)
                .commit();
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

        ctrlClassic.setEngine(CameraEngine.buildInstance(RecordVideoActivity.this, true),
                criteria);
        ctrlClassic.getEngine().setDebug(getIntent().getBooleanExtra(EXTRA_DEBUG_ENABLED, true));

        if (!cameraFragment.isVisible()) {
            getFragmentManager().beginTransaction().show(cameraFragment).commit();
        }
    }

    @Override
    public void onEventMainThread(CameraController.ControllerDestroyedEvent event) {
        // dont destroy
    }
}