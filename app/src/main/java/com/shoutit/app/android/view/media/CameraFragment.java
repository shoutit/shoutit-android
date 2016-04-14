package com.shoutit.app.android.view.media;

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
import android.graphics.PorterDuff;
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
    private static final int REQUEST_GALLERY_VIDEO_CODE = 1;
    private static final int VIDEO_LENGTH = 60_000;

    private static final String ARGS_VIDEO_FIRST = "arg_video_first";
    private static final String ARGS_CHAT_MEDIA = "arg_is_chat";
    private static final String ARGS_FIRST_MEDIA = "arg_first_media";

    private static final String TAG = CameraFragment.class.getCanonicalName();

    public interface CameraFragmentListener {
        void onInitializationFailed(Exception cause);

        void onMediaResult(Intent intent);
    }

    public static final String EXTRA_IMAGE_URI = CameraFragment.class.getName() + ".image_path";
    public static final String EXTRA_IMAGE_VIDEO_LIST = CameraFragment.class.getName() + ".video_image_path";
    public static final String EXTRA_EXISTING_MEDIA = CameraFragment.class.getName() + ".existing_media";
    public static final String EXTRA_IS_VIDEO = CameraFragment.class.getName() + ".existing_media";
    public static final String IS_IMAGE_LIST = CameraFragment.class.getName() + ".is_list";

    public static final int DEFAULT_IMAGE_QUALITY = 100;

    public static final int RC_MEDIA_COMPRESS = 1339;

    private CameraController ctlr;
    private boolean isVideoRecording = false;
    private boolean mirrorPreview = false;
    private boolean isVideoMode = false;
    private CountDownTimer countDownTimer;
    private String videoOutput, imageOutput;
    private CameraFragmentListener cameraFragmentListener;
    private boolean isMFfcEnabled = false;
    private boolean chatMedia;
    private boolean firstMedia;

    @Bind(R.id.fragment_camera_preview_stack)
    ViewGroup previewStack;
    @Bind(R.id.fragment_camera_switch_camera)
    ImageButton switchCameraButton;
    @Bind(R.id.fragment_camera_close)
    ImageButton closeButton;
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
    @Bind(R.id.camera_text)
    TextView cameraText;
    @Bind(R.id.camera_text_header)
    TextView cameraTextHeader;

    public static CameraFragment newInstance(boolean videoFirst, boolean chatMedia, boolean firstMedia) {
        final Bundle args = new Bundle();
        args.putBoolean(ARGS_VIDEO_FIRST, videoFirst);
        args.putBoolean(ARGS_CHAT_MEDIA, chatMedia);
        args.putBoolean(ARGS_FIRST_MEDIA, firstMedia);
        final CameraFragment cameraFragment = new CameraFragment();
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof CameraFragmentListener)) {
            throw new IllegalStateException(
                    "Parent Activity must implement CameraFragmentListener: " + activity.getClass()
                            .getName());
        }

        cameraFragmentListener = (CameraFragmentListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isVideoMode = getArguments().getBoolean(ARGS_VIDEO_FIRST);
        chatMedia = getArguments().getBoolean(ARGS_CHAT_MEDIA);
        firstMedia = getArguments().getBoolean(ARGS_FIRST_MEDIA);

        if (hasCameras()) {
            cameraFragmentListener.onInitializationFailed(
                    new IllegalStateException("Camera not found"));
        }
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!SHCameraInfo.getInstance().isHasFrontFacingCamera()
                || !SHCameraInfo.getInstance().isHasBackFacingCamera()) {
            switchCameraButton.setVisibility(View.GONE);
        }

        textViewTime.setText(String.valueOf(VIDEO_LENGTH / 1000));
        if (isVideoMode) {
            enableVideoMode();
        } else {
            enablePictureMode();
        }

        if (chatMedia) {
            buttonConfirm.setText(R.string.camera_send);
            cameraTextHeader.setText(null);
        } else if (firstMedia) {
            buttonConfirm.setText(R.string.camera_publish);
            cameraTextHeader.setText(R.string.camera_header);
        } else {
            buttonConfirm.setText(R.string.camera_publish);
            cameraTextHeader.setText(null);
        }

        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
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

    @Override
    public void onStop() {
        if (ctlr != null) {
            ctlr.stop();
        }

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (ctlr != null) {
            ctlr.destroy();
        }

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_camera, container, false);

        ButterKnife.bind(this, view);

        onHiddenChanged(false);

        if (ctlr != null && ctlr.getNumberOfCameras() > 0) {
            prepController();
        }

        return view;
    }

    private boolean hasCameras() {
        return SHCameraInfo.getInstance().getNumberOfCameras() != 0;
    }

    @SuppressWarnings("unused")
    public CameraController getController() {
        return (ctlr);
    }

    public void setController(CameraController ctlr) {
        this.ctlr = ctlr;
    }

    public void setMirrorPreview(boolean mirror) {
        mirrorPreview = mirror;
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
        isVideoRecording = false;
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
            break;
            case REQUEST_GALLERY_IMAGE_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final Optional<Uri> uriOptional = onResult(resultCode, data);
                    final String imageFile = Utils.getPictureDirectory(getActivity(), true) + File.separator + Utils.getPictureName();
                    if (uriOptional.isPresent()) {
                        final Uri uri = uriOptional.get();
                        try {
                            copyGalleryFile(imageFile, uri);

                            imageOutput = imageFile;
                            showConfirmImage();
                        } catch (IOException e) {
                            Log.e(TAG, "error", e);
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
                }
                break;
            case REQUEST_GALLERY_VIDEO_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final Optional<Uri> videoUri = onResult(resultCode, data);
                    final String videoFile = Utils.getVideoDirectory(getActivity(), true) + File.separator + Utils.getVideoName();
                    if (videoUri.isPresent()) {
                        final Uri uri = videoUri.get();
                        try {
                            copyGalleryFile(videoFile, uri);
                            videoOutput = videoFile;

                            layoutConfirm.setVisibility(View.VISIBLE);

                            getActivity().getFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_camera_layout_preview_overlay,
                                            PlayVideoFragment.newInstance(videoOutput))
                                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                    .commit();

                        } catch (IOException e) {
                            Log.e(TAG, "error", e);
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
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void copyGalleryFile(String file, Uri uri) throws IOException {
        final InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
        Preconditions.checkNotNull(inputStream);
        final Source source = Okio.buffer(Okio.source(inputStream));
        final BufferedSink bufferedSink = Okio.buffer(Okio.sink(new File(file)));

        bufferedSink.writeAll(source);
        bufferedSink.flush();
    }

    public Optional<Uri> onResult(int resultCode, Intent intent) {
        Uri uri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                if (intent.getData() != null) {
                    uri = intent.getData();
                } else {
                    final String outputMediaFileUri = getUriFromBitmap((Bitmap) intent.getParcelableExtra("data"));
                    if (outputMediaFileUri == null) {
                        return Optional.absent();
                    }
                    uri = Uri.fromFile(new File(outputMediaFileUri));
                }
            }
            return Optional.fromNullable(uri);
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
        switchCameraButton.setEnabled(false);
        galleryPick.setEnabled(false);
        picture.setEnabled(false);

        setTimer();
    }

    private void setTimer() {
        countDownTimer = new CountDownTimer(VIDEO_LENGTH, 1000) {

            public void onTick(long millisUntilFinished) {
                textViewTime.setText(String.valueOf(millisUntilFinished / 1000));
                if (millisUntilFinished / 1000 <= VIDEO_LENGTH / 1000) {
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
        switchCameraButton.setEnabled(false);
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

            if (isMFfcEnabled) {
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, DEFAULT_IMAGE_QUALITY, fos);
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

        actionBtn.setEnabled(true);
        actionBtn.setSelected(false);
        actionBtn.setActivated(false);

        if (switchCameraButton.getVisibility() == View.VISIBLE) {
            switchCameraButton.setEnabled(true);
        }

        picture.setEnabled(true);
        video.setEnabled(true);
        galleryPick.setEnabled(true);


        if (textViewTime.getVisibility() == View.VISIBLE) {
            textViewTime.setText(String.valueOf(VIDEO_LENGTH / 1000));
        }

        buttonConfirm.setEnabled(true);
        buttonDiscard.setEnabled(true);
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

                    if (CameraUtils.bitmapToFile(imageBitmap, imageFile, Bitmap.CompressFormat.JPEG, DEFAULT_IMAGE_QUALITY)) {
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

        isMFfcEnabled = SHCameraInfo.getInstance().isHasFrontFacingCamera() && !isMFfcEnabled;
    }

    @OnClick(R.id.fragment_camera_close)
    void close() {
        getActivity().finish();
    }

    @OnClick(R.id.fragment_camera_action_btn)
    void performCameraAction() {
        if (isVideoMode) {
            try {
                recordVideo();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "camera busy", Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                takePicture();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "camera busy", Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.fragment_camera_mode_picture_btn)
    void pictureModeClick() {
        enablePictureMode();
    }

    private void enablePictureMode() {
        isVideoMode = false;
        actionBtn.setImageResource(R.drawable.camera_capture);
        textViewTime.setVisibility(View.GONE);
        video.setVisibility(View.VISIBLE);
        picture.setVisibility(View.GONE);

        if (chatMedia || !firstMedia) {
            cameraText.setText(null);
        } else {
            cameraText.setText(getString(R.string.camera_sub_header, getString(R.string.camera_photo)));
        }
    }

    @OnClick(R.id.fragment_camera_mode_video_btn)
    void videoModeClick() {
        enableVideoMode();
    }

    private void enableVideoMode() {
        isVideoMode = true;
        actionBtn.setImageResource(R.drawable.video_capture);
        textViewTime.setVisibility(View.VISIBLE);
        video.setVisibility(View.GONE);
        picture.setVisibility(View.VISIBLE);

        if (chatMedia || !firstMedia) {
            cameraText.setText(null);
        } else {
            cameraText.setText(getString(R.string.camera_sub_header, getString(R.string.camera_video)));
        }
    }

    @OnClick(R.id.fragment_camera_confirm_yes_btn)
    void onConfirmMedia() {
        if (isVideoMode) {
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
            final Intent selectImageIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .setType("video/*");
            startActivityForResult(selectImageIntent, REQUEST_GALLERY_VIDEO_CODE);
        } else {
            final Intent selectImageIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .setType("image/*");
            startActivityForResult(selectImageIntent, REQUEST_GALLERY_IMAGE_CODE);
        }
    }
}
