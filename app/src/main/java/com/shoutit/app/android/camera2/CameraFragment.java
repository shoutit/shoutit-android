package com.shoutit.app.android.camera2;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.internal.Preconditions;
import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraView;
import com.commonsware.cwac.cam2.PictureTransaction;
import com.commonsware.cwac.cam2.VideoTransaction;
import com.google.common.base.Optional;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class CameraFragment extends Fragment {

    private static final int REQUEST_GALLERY_IMAGE_CODE = 0;

    public interface CameraFragmentListener {
        void onInitializationFailed(Exception cause);

        void onMediaResult(Intent intent);
    }

    public static final String EXTRA_MEDIA_NAME_PREFIX = CameraFragment.class.getName() + ".media_name_prefix";
    public static final String EXTRA_IMAGE_URI = CameraFragment.class.getName() + ".image_path";
    public static final String EXTRA_DISABLE_VIDEO = CameraFragment.class.getName() + ".disable_video";
    public static final String EXTRA_IMAGE_VIDEO_LIST = CameraFragment.class.getName() + ".video_image_path";
    public static final String EXTRA_VIDEO_QUALITY = CameraFragment.class.getName() + ".video_quality";
    public static final String EXTRA_USE_FRONT_FACING_CAMERA = CameraFragment.class.getName() + ".use_ffc";
    public static final String EXTRA_CAMERA_SOURCE_TOGGLE_ENABLED = CameraFragment.class.getName() + ".camera_source_toggle_enabled";
    public static final String EXTRA_VIDEO_MAX_LENGTH = CameraFragment.class.getName() + ".video_max_len";
    public static final String EXTRA_VIDEO_ENABLED = CameraFragment.class.getName() + ".video_enabled";
    public static final String EXTRA_PICTURE_ENABLED = CameraFragment.class.getName() + ".picture_enabled";
    public static final String EXTRA_GALLERY_ENABLED = CameraFragment.class.getName() + ".gallery_enabled";
    public static final String EXTRA_EXISTING_MEDIA = CameraFragment.class.getName() + ".existing_media";
    public static final String EXTRA_IS_VIDEO = CameraFragment.class.getName() + ".existing_media";
    public static final String EXTRA_IMAGE_QUALITY = CameraFragment.class.getName() + ".image_quality";
    public static final String IS_IMAGE_LIST = CameraFragment.class.getName() + ".is_list";
    public static final String EXTRA_PREVIEW_OVERLAY_LAYOUT_RESOURCE = CameraFragment.class.getName() + ".preview_overlay_res";
    public static final String EXTRA_VIDEO_MIN_LENGTH = CameraFragment.class.getName() + ".video_min_len";
    public static final String EXTRA_CONTROLS_BACKGROUND_COLOR = CameraFragment.class.getName() + ".controls_background_color";
    public static final String EXTRA_IMAGE_COMPRESSION_TYPE = CameraFragment.class.getName() + ".compression_type";

    public static final int DEFAULT_VIDEO_QUALITY = CamcorderProfile.QUALITY_480P;
    public static final long VIDEO_NO_MIN_LENGTH = 0;
    public static final long VIDEO_NO_MAX_LENGTH = -1;
    public static final String DEFAULT_MEDIA_NAME_PREFIX = "media_";
    public static final int DEFAULT_IMAGE_QUALITY = 100;
    public static final int DEFAULT_CONTROLS_BACKGROUND_COLOR = 0x00000000;

    public static final int RC_MEDIA_COMPRESS = 1339;
    public static final int NO_OVERLAY = 0;

    private CameraController ctlr;
    private boolean isVideoRecording = false;
    private boolean mirrorPreview = false;
    private boolean isVideoMode = true;
    private CountDownTimer countDownTimer;
    private String videoOutput, imageOutput, mMediaNamePrefix;
    private CameraFragmentListener cameraFragmentListener;
    private Boolean mVideoEnabled, mPictureEnabled, mGalleryEnabled, mDisableVideo, mCameraSourceToggleEnabled;
    private Boolean mUseFfc, isMFfcEnabled = false;
    private long mVideoMinLength, mVideoMaxLength;
    private int mVideoQuality, mImageQuality;
    private int mPreviewOverlayLayoutResId, mControlsBackgroundColor;
    private Bitmap.CompressFormat mCompression;

    @Bind(R.id.fragment_camera_preview_stack)
    ViewGroup previewStack;
    @Bind(R.id.fragment_camera_switch_camera)
    ImageView viewSwitchCameraSource;
    @Bind(R.id.fragment_camera_mode_video_btn)
    ImageButton video;
    @Bind(R.id.fragment_camera_mode_picture_btn)
    ImageButton picture;
    @Bind(R.id.fragment_camera_action_btn)
    ImageView actionBtn;
    @Bind(R.id.fragment_camera_imageview_gallery_btn)
    ImageButton galleryPick;
    @Bind(R.id.fragment_camera_layout_controls_main)
    ViewGroup layoutControlsMain;
    @Bind(R.id.fragment_camera_layout_preview_overlay)
    ViewGroup layoutPreviewOverlay;
    @Bind(R.id.fragment_camera_layout_preview_overlay_live)
    ViewGroup layoutPreviewOverlayLive;
    @Bind(R.id.fragment_camera_layout_confirm)
    ViewGroup layoutConfirm;
    @Bind(R.id.fragment_camera_confirm_yes_btn)
    Button buttonConfirm;
    @Bind(R.id.fragment_camera_confirm_no_btn)
    Button buttonDiscard;
    @Bind(R.id.fragment_camera_timer)
    TextView textViewTime;

    public static CameraFragment newInstance(Bundle bundle) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle(bundle);

        if (bundle.containsKey(CameraFragment.EXTRA_DISABLE_VIDEO)) {
            if (bundle.containsKey(CameraFragment.EXTRA_GALLERY_ENABLED)) {
                args.putBoolean(CameraFragment.EXTRA_GALLERY_ENABLED, bundle.getBoolean(CameraFragment.EXTRA_GALLERY_ENABLED));
            }
            args.putBoolean(CameraFragment.EXTRA_DISABLE_VIDEO, bundle.getBoolean(CameraFragment.EXTRA_DISABLE_VIDEO));
        }
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof CameraFragmentListener)) {
            throw new IllegalStateException(
                    "Parent Activity must implement CameraFragmentListener: " + activity.getClass()
                            .getName());
        }

        this.cameraFragmentListener = (CameraFragmentListener) getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(EXTRA_MEDIA_NAME_PREFIX, this.mMediaNamePrefix);
        outState.putBoolean(EXTRA_DISABLE_VIDEO, this.mDisableVideo);
        outState.putBoolean(EXTRA_USE_FRONT_FACING_CAMERA, this.mUseFfc);
        outState.putBoolean(EXTRA_VIDEO_ENABLED, this.mVideoEnabled);
        outState.putBoolean(EXTRA_PICTURE_ENABLED, this.mPictureEnabled);
        outState.putBoolean(EXTRA_GALLERY_ENABLED, this.mGalleryEnabled);
        outState.putLong(EXTRA_VIDEO_MIN_LENGTH, this.mVideoMinLength);
        outState.putLong(EXTRA_VIDEO_MAX_LENGTH, this.mVideoMaxLength);
        outState.putInt(EXTRA_VIDEO_QUALITY, this.mVideoQuality);
        outState.putInt(EXTRA_IMAGE_QUALITY, this.mImageQuality);
        outState.putInt(EXTRA_PREVIEW_OVERLAY_LAYOUT_RESOURCE, this.mPreviewOverlayLayoutResId);
        outState.putInt(EXTRA_CONTROLS_BACKGROUND_COLOR, this.mControlsBackgroundColor);
    }

    /**
     * Standard fragment entry point.
     *
     * @param savedInstanceState State of a previous instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.updateMediaDB();
        Bundle extras = savedInstanceState == null ? getArguments() : savedInstanceState;

        if (!this.init(extras)) {
            this.cameraFragmentListener.onInitializationFailed(
                    new IllegalStateException("Camera not found"));
        }
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!SHCameraInfo.getInstance().isHasFrontFacingCamera()
                || !SHCameraInfo.getInstance().isHasBackFacingCamera()
                || !this.mCameraSourceToggleEnabled) {
            this.viewSwitchCameraSource.setVisibility(View.GONE);
        }

        if (!this.mPictureEnabled || !this.mVideoEnabled) {
            this.picture.setVisibility(View.GONE);
            this.video.setVisibility(View.VISIBLE);
            this.video.setVisibility(View.GONE);
            this.picture.setVisibility(View.GONE);
        }

        if (!this.mVideoEnabled || this.mVideoMaxLength == VIDEO_NO_MAX_LENGTH) {
            this.textViewTime.setVisibility(View.GONE);
        }
        if (this.textViewTime.getVisibility() == View.VISIBLE) {
            this.textViewTime.setText(String.valueOf(mVideoMaxLength / 1000));
        }

        if (!this.mGalleryEnabled) {
            this.galleryPick.setVisibility(View.GONE);
        }

        if (this.isVideoMode) {
            this.actionBtn.setImageResource(R.drawable.video_capture);
            this.video.setVisibility(View.GONE);
        } else {
            this.actionBtn.setImageResource(R.drawable.camera_capture);
            this.picture.setVisibility(View.GONE);
        }

        if (this.mUseFfc && !this.mVideoEnabled) {
            this.isVideoMode = false;
        }
    }

    private void updateMediaDB() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.parse("file://"
                        + Environment.getExternalStorageDirectory());
                mediaScanIntent.setData(contentUri);
                getActivity().sendBroadcast(mediaScanIntent);
            } else {
                getActivity().sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            }
        } catch (Exception e) {
            Log.e("tag", "Couldn't update media DB", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        if (ctlr != null) {
            ctlr.start();
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onHiddenChanged(boolean isHidden) {
        super.onHiddenChanged(isHidden);

        if (!isHidden) {
            ActionBar ab = getActivity().getActionBar();

            if (ab != null) {
                ab.setTitle("");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ab.setBackgroundDrawable(getActivity().getResources()
                            .getDrawable(com.commonsware.cwac.cam2.R.drawable.cwac_cam2_action_bar_bg_transparent, getActivity()
                                    .getTheme()));
                    ab.setDisplayHomeAsUpEnabled(false);
                } else {
                    ab.setDisplayShowHomeEnabled(false);
                    ab.setHomeButtonEnabled(false);
                    ab.setBackgroundDrawable(
                            getActivity().getResources().getDrawable(com.commonsware.cwac.cam2.R.drawable.cwac_cam2_action_bar_bg_transparent));
                }
            }
        }
    }

    /**
     * Standard lifecycle method, for when the fragment moves into
     * the stopped state. Passed along to the CameraController.
     */
    @Override
    public void onStop() {
        if (ctlr != null) {
            ctlr.stop();
        }

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    /**
     * Standard lifecycle method, for when the fragment is utterly,
     * ruthlessly destroyed. Passed along to the CameraController,
     * because why should the fragment have all the fun?
     */
    @Override
    public void onDestroy() {
        if (ctlr != null) {
            ctlr.destroy();
        }

        super.onDestroy();
    }

    /**
     * Standard callback method to create the UI managed by
     * this fragment.
     *
     * @param inflater           Used to inflate layouts
     * @param container          Parent of the fragment's UI (eventually)
     * @param savedInstanceState State of a previous instance
     * @return the UI being managed by this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        ButterKnife.bind(this, view);

        onHiddenChanged(false);

        if (ctlr != null && ctlr.getNumberOfCameras() > 0) {
            prepController();
        }

        if (this.mPreviewOverlayLayoutResId != NO_OVERLAY) {
            inflater.inflate(this.mPreviewOverlayLayoutResId, this.layoutPreviewOverlayLive, true);
            this.layoutPreviewOverlayLive.setVisibility(View.VISIBLE);
        }
        this.layoutControlsMain.setBackgroundColor(this.mControlsBackgroundColor);
        return (view);
    }

    private boolean init(Bundle extras) {
        if (SHCameraInfo.getInstance().getNumberOfCameras() == 0) {
            return false;
        }

        if (extras.containsKey(EXTRA_MEDIA_NAME_PREFIX)) {
            this.mMediaNamePrefix = extras.getString(EXTRA_MEDIA_NAME_PREFIX);
        } else {
            this.mMediaNamePrefix = DEFAULT_MEDIA_NAME_PREFIX;
        }

        this.mVideoEnabled = extras.getBoolean(EXTRA_VIDEO_ENABLED, true);
        this.mPictureEnabled = extras.getBoolean(EXTRA_PICTURE_ENABLED, true);
        this.mGalleryEnabled = extras.getBoolean(EXTRA_GALLERY_ENABLED, true);
        this.mDisableVideo = extras.getBoolean(EXTRA_DISABLE_VIDEO, false);
        this.mCameraSourceToggleEnabled = extras.getBoolean(EXTRA_CAMERA_SOURCE_TOGGLE_ENABLED, true);

        this.mVideoMinLength = extras.getLong(EXTRA_VIDEO_MIN_LENGTH, VIDEO_NO_MIN_LENGTH);
        this.mVideoMaxLength = extras.getLong(EXTRA_VIDEO_MAX_LENGTH, VIDEO_NO_MAX_LENGTH);

        if (this.mVideoMinLength < 0) {
            this.mVideoMinLength = VIDEO_NO_MIN_LENGTH;
        }

        if (this.mVideoMaxLength < -1) {
            this.mVideoMaxLength = VIDEO_NO_MAX_LENGTH;
        }

        if (this.mVideoMaxLength != VIDEO_NO_MAX_LENGTH && this.mVideoMinLength > this.mVideoMaxLength) {
            throw new IllegalArgumentException("Video min length > max length: " + this.mVideoMinLength + " > " + this.mVideoMaxLength);
        }

        this.mVideoQuality = extras.getInt(EXTRA_VIDEO_QUALITY, DEFAULT_VIDEO_QUALITY);

        this.isVideoMode = this.mVideoEnabled;
        this.mUseFfc = extras.getBoolean(EXTRA_USE_FRONT_FACING_CAMERA, false);

        if (this.mUseFfc && SHCameraInfo.getInstance().isHasFrontFacingCamera()) {
            this.isMFfcEnabled = true;
        }

        if (extras.containsKey(EXTRA_IMAGE_COMPRESSION_TYPE)) {
            this.mCompression = Bitmap.CompressFormat.valueOf(extras.getString(EXTRA_IMAGE_COMPRESSION_TYPE));
        } else {
            this.mCompression = Bitmap.CompressFormat.JPEG;
        }

        this.mImageQuality = extras.getInt(EXTRA_IMAGE_QUALITY, DEFAULT_IMAGE_QUALITY);
        this.mPreviewOverlayLayoutResId = extras.getInt(EXTRA_PREVIEW_OVERLAY_LAYOUT_RESOURCE, NO_OVERLAY);
        this.mControlsBackgroundColor = extras.getInt(EXTRA_CONTROLS_BACKGROUND_COLOR, DEFAULT_CONTROLS_BACKGROUND_COLOR);

        return true;
    }


    /**
     * @return the CameraController this fragment delegates to
     */
    @SuppressWarnings("unused")
    public CameraController getController() {
        return (ctlr);
    }

    /**
     * Establishes the controller that this fragment delegates to
     *
     * @param ctlr the controller that this fragment delegates to
     */
    public void setController(CameraController ctlr) {
        this.ctlr = ctlr;
    }

    /**
     * Indicates if we should mirror the preview or not. Defaults
     * to false.
     *
     * @param mirror true if we should horizontally mirror the
     *               preview, false otherwise
     */
    public void setMirrorPreview(boolean mirror) {
        this.mirrorPreview = mirror;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraController.ControllerReadyEvent event) {
        if (event.isEventForController(ctlr)) {
            prepController();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraEngine.VideoTakenEvent event) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        this.isVideoRecording = false;
        actionBtn.setImageResource(R.drawable.video_capture);
        textViewTime.setTextColor(Color.WHITE);

        if (event.getVideoTransaction() == null) {
            Log.e("tag", "Not able to get video transaction");
        } else {
            layoutConfirm.setVisibility(View.VISIBLE);

            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_camera_layout_preview_overlay,
                            PlayVideoFragment.newInstance(videoOutput))
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraEngine.PictureTakenEvent event) {
        showConfirmImage();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraEngine.OpenedEvent event) {
        if (event.exception != null) {
            Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<Image> images;
        switch (requestCode) {
            case RC_MEDIA_COMPRESS: {
                if (resultCode == Activity.RESULT_OK) {
                    images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    ArrayList<String> paths = new ArrayList<>();
                    for (int i = 0; i < images.size(); i++) {
                        paths.add(images.get(i).path);
                    }
                    Intent result = new Intent();
                    result.putStringArrayListExtra(EXTRA_IMAGE_URI, paths);
                    result.putParcelableArrayListExtra(EXTRA_IMAGE_VIDEO_LIST, images);
                    result.putExtra(EXTRA_EXISTING_MEDIA, true);
                    result.putExtra(EXTRA_IS_VIDEO, isVideoMode);
                    result.putExtra(IS_IMAGE_LIST, true);
                    cameraFragmentListener.onMediaResult(result);
                }
            }
            case REQUEST_GALLERY_IMAGE_CODE:
                final Optional<Uri> uriOptional = onResult(resultCode, data);
                final String file = Utils.getPictureDirectory(getActivity(), true) + File.separator + Utils.getPictureName();
                if (uriOptional.isPresent()) {
                    final Uri uri = uriOptional.get();
                    try {
                        final InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                        Preconditions.checkNotNull(inputStream);
                        final Source source = Okio.buffer(Okio.source(inputStream));
                        final BufferedSink bufferedSink = Okio.buffer(Okio.sink(new File(file)));

                        bufferedSink.writeAll(source);
                        bufferedSink.flush();

                        imageOutput = file;
                        showConfirmImage();
                    } catch (IOException e) {
                        ColoredSnackBar.error(
                                ColoredSnackBar.contentView(getActivity()),
                                R.string.error_default,
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    ColoredSnackBar.error(
                            ColoredSnackBar.contentView(getActivity()),
                            R.string.error_default,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public Optional<Uri> onResult(int resultCode, Intent intent) {
        Uri mLastImageOrVideoUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                if (intent.getData() != null) {
                    mLastImageOrVideoUri = intent.getData();
                } else {
                    final String outputMediaFileUri = getUriFromBitmap((Bitmap) intent.getParcelableExtra("data"));
                    if (outputMediaFileUri == null) {
                        return Optional.absent();
                    }
                    mLastImageOrVideoUri = Uri.fromFile(new File(outputMediaFileUri));
                }
            }
            return Optional.fromNullable(mLastImageOrVideoUri);
        }

        return Optional.absent();
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private String getUriFromBitmap(Bitmap bitmap) {
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

            final File f = new File(Environment.getExternalStorageDirectory()
                    + File.separator + System.currentTimeMillis() + ".jpg");

            final FileOutputStream fo = new FileOutputStream(f);
            try {
                fo.write(bytes.toByteArray());
            } finally {
                fo.close();
            }

            return f.toString();
        } catch (Exception ignore) {
        }
        return null;
    }

    private void recordVideo() {
        if (isVideoRecording) {
            try {
                actionBtn.setSelected(true);
                actionBtn.setEnabled(false);
                ctlr.stopVideoRecording();
            } catch (Exception e) {
                Log.e("tag", "Exception stopping recording of video", e);
            }
        } else {
            try {
                videoOutput =
                        Utils.getVideoDirectory(getActivity(), true) + File.separator + Utils.getVideoName();
                Uri videoOutputFile = Uri.fromFile(new File(videoOutput));

                VideoTransaction.Builder b = new VideoTransaction.Builder();
                b.to(new File(videoOutputFile.getPath())).quality(1).sizeLimit(0).durationLimit(0);

                ctlr.recordVideo(b.build());
                onStartVideoRecord();
            } catch (Exception e) {
                Log.e("tag", "Exception recording video", e);
            }
        }
    }

    private void onStartVideoRecord() {
        isVideoRecording = true;
        actionBtn.setActivated(false);
        actionBtn.setEnabled(false);
        textViewTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.camera_timer_color));
        actionBtn.setImageResource(R.drawable.camera_stop);
        viewSwitchCameraSource.setEnabled(false);
        galleryPick.setEnabled(false);
        picture.setEnabled(false);

        setTimer();
    }

    private void setTimer() {
        countDownTimer = new CountDownTimer(mVideoMaxLength, 1000) {

            public void onTick(long millisUntilFinished) {
                textViewTime.setText(String.valueOf(millisUntilFinished / 1000));
                if (millisUntilFinished / 1000 <= mVideoMaxLength / 1000 - mVideoMinLength / 1000) {
                    actionBtn.setEnabled(true);
                    actionBtn.setActivated(true);
                }
            }

            public void onFinish() {
                textViewTime.setText("0");
                try {
                    ctlr.stopVideoRecording();
                } catch (Exception e) {
                    Log.e("tag", "Exception stopping recording of video", e);
                }
            }
        }.start();
    }

    private void takePicture() {
        onStartTakingPicture();
        imageOutput =
                Utils.getPictureDirectory(getActivity(), true) + File.separator + Utils.getPictureName();
        Uri output = Uri.fromFile(new File(imageOutput));
        PictureTransaction.Builder b = new PictureTransaction.Builder();

        if (output != null) {
            b.toUri(getActivity(), output, true);
        }

        ctlr.takePicture(b.build());
    }

    private void onStartTakingPicture() {
        actionBtn.setActivated(false);
        actionBtn.setEnabled(false);
        viewSwitchCameraSource.setEnabled(false);
        galleryPick.setEnabled(false);
        picture.setEnabled(false);
    }

    public void showConfirmImage() {
        ExifInterface ei = null;
        layoutConfirm.setVisibility(View.VISIBLE);

        try {
            ei = new ExifInterface(imageOutput);
        } catch (IOException e) {
            Log.e("tag", "Error getting image exif info", e);
        }

        if (ei != null) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(imageOutput);

            int orientation =
                    ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.d("IMAGE", "orientation : " + orientation);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    imageBitmap = rotateBitmap(imageBitmap, 90);
                    Log.d("IMAGE", "Image needs rotation 90");
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    imageBitmap = rotateBitmap(imageBitmap, 180);
                    Log.d("IMAGE", "Image needs rotation 180");
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    imageBitmap = rotateBitmap(imageBitmap, -90);
                    Log.d("IMAGE", "Image needs rotation 270");
                    break;
                default:
                    Log.d("IMAGE", "Image needs no rotation");
                    break;
            }

            if (this.isMFfcEnabled) {
                Matrix matrix = new Matrix();
                float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                Matrix matrixMirrorY = new Matrix();
                matrixMirrorY.setValues(mirrorY);
                matrix.postConcat(matrixMirrorY);
                imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(),
                        imageBitmap.getHeight(), matrix, true);
            }

            saveBitmapToFile(imageBitmap, imageOutput);
            imageBitmap.recycle();
        }

        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_camera_layout_preview_overlay,
                        ImageFragment.newInstance(imageOutput, null)).setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void saveBitmapToFile(Bitmap bitmap, String outputFilePath) {
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(outputFilePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, mImageQuality, fos);
            fos.close();
        } catch (IOException e) {
            Log.e("tag", "File output stream error", e);
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void prepController() {
        LinkedList<CameraView> cameraViews = new LinkedList<>();
        CameraView cv = (CameraView) previewStack.getChildAt(0);

        cv.setMirror(mirrorPreview);
        cameraViews.add(cv);

        for (int i = 1; i < ctlr.getNumberOfCameras(); i++) {
            cv = new CameraView(getActivity());
            cv.setVisibility(View.INVISIBLE);
            cv.setMirror(mirrorPreview);
            previewStack.addView(cv);
            cameraViews.add(cv);
        }

        ctlr.setCameraViews(cameraViews);
    }

    private void resetView() {
        layoutPreviewOverlay.removeAllViews();
        layoutConfirm.setVisibility(View.GONE);

        this.actionBtn.setEnabled(true);
        this.actionBtn.setSelected(false);
        this.actionBtn.setActivated(false);

        if (this.viewSwitchCameraSource.getVisibility() == View.VISIBLE) {
            this.viewSwitchCameraSource.setEnabled(true);
        }

        this.picture.setEnabled(true);
        this.video.setEnabled(true);
        this.galleryPick.setEnabled(true);


        if (this.textViewTime.getVisibility() == View.VISIBLE) {
            this.textViewTime.setText(String.valueOf(this.mVideoMaxLength / 1000));
        }

        this.buttonConfirm.setEnabled(true);
        this.buttonDiscard.setEnabled(true);
    }


    private void onPictureConfirmed() {
        new AsyncTask<Void, Void, String[]>() {
            @Override
            protected void onPreExecute() {
                buttonConfirm.setEnabled(false);
                buttonDiscard.setEnabled(false);
            }

            @Override
            protected String[] doInBackground(Void... params) {
                if (imageOutput == null) return null;
                File imageFile = new File(imageOutput);

                if (!imageFile.exists()) return null;

                try {
                    Bitmap imageBitmap = CameraUtils.getResizedImage(imageFile, 1280);

                    Log.d("tag", String.format("  > %s x %s", imageBitmap.getWidth(), imageBitmap.getHeight()));

                    if (CameraUtils.bitmapToFile(imageBitmap, imageFile, mCompression, mImageQuality)) {
                        Log.d("tag", String.format("  > new JPEG file size: %s bytes", imageFile.length()));
                        return new String[]{EXTRA_IMAGE_URI, "file://" + imageOutput};
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String[] keyValue) {
                if (keyValue != null) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_EXISTING_MEDIA, false);

                    if (isVideoMode) {
                        returnIntent.putExtra(keyValue[0], keyValue[1]);
                    } else {
                        returnIntent.putExtra(keyValue[0], Uri.parse(keyValue[1]));
                        returnIntent.putExtra(IS_IMAGE_LIST, false); //to define that there is only single image
                    }
                    returnIntent.putExtra(EXTRA_IS_VIDEO, isVideoMode);

                    ((CameraFragmentListener) getActivity()).onMediaResult(returnIntent);
                } else {
                    Toast.makeText(getActivity(), "resizing failed",
                            Toast.LENGTH_LONG).show();

                    resetView();
                }
            }
        }.execute();
    }

    @OnClick(R.id.fragment_camera_switch_camera)
    void switchCamera() {
        ctlr.switchCamera();

        this.isMFfcEnabled = SHCameraInfo.getInstance().isHasFrontFacingCamera() && !this.isMFfcEnabled;
    }

    @OnClick(R.id.fragment_camera_action_btn)
    void performCameraAction() {
        if (this.isVideoMode) {
            try {
                this.recordVideo();
            } catch (Exception e) {
                Toast.makeText(CameraFragment.this.getActivity(), "camera busy", Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                this.takePicture();
            } catch (Exception e) {
                Toast.makeText(CameraFragment.this.getActivity(), "camera busy", Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.fragment_camera_mode_picture_btn)
    void enablePictureMode() {
        this.isVideoMode = false;
        actionBtn.setImageResource(R.drawable.camera_capture);
        textViewTime.setVisibility(View.GONE);
        video.setVisibility(View.VISIBLE);
        picture.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_camera_mode_video_btn)
    void enableVideoMode() {
        this.isVideoMode = true;
        actionBtn.setImageResource(R.drawable.video_capture);
        textViewTime.setVisibility(View.VISIBLE);
        video.setVisibility(View.GONE);
        picture.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.fragment_camera_confirm_yes_btn)
    void onConfirmMedia() {
        if (this.isVideoMode) {
            final File file = new File(videoOutput);

            final ArrayList<Image> tempImages = new ArrayList<>();
            try {
                final Image image = new Image(System.currentTimeMillis(), file.getName(), file.getAbsolutePath(), false,
                        true, VideoUtils.getDuration(file.getAbsolutePath()) + "");
                tempImages.add(image);
            } catch (Exception e) {
                Log.e("tag", "Error while getting image.", e);
            }
            VideoUtils.CompressVideo(tempImages, CameraFragment.this);
        } else {
            onPictureConfirmed();
        }
    }

    @OnClick(R.id.fragment_camera_confirm_no_btn)
    void onDiscardMedia() {
        resetView();
    }

    @OnClick(R.id.fragment_camera_imageview_gallery_btn)
    void showGallery() {
        if (isVideoMode) {

        } else {
            final Intent selectImageIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .setType("image/*");
            startActivityForResult(selectImageIntent, REQUEST_GALLERY_IMAGE_CODE);
        }
    }
}
