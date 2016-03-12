package com.shoutit.app.android.view.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.SystemUIUtils;
import com.shoutit.app.android.widget.FlashlightButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

@SuppressWarnings("deprecation")
public class NativeCameraActivity extends FragmentActivity {

    private Camera mCamera;

    private CameraPreview mPreview;

    @Bind(R.id.camera_flashlight)
    FlashlightButton flashlightCheckbox;

    @Bind(R.id.camera_preview)
    FrameLayout mCameraPreview;

    @Bind(R.id.camera_rotate)
    CheckBox mCameraRotate;

    @Bind(R.id.button_capture)
    ImageButton captureButton;

    @Bind(R.id.camera_close)
    ImageButton closeButton;

    private Integer mFaceCameraId;

    public static NativeCameraActivity newInstance() {
        return new NativeCameraActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        SystemUIUtils.setFullscreen(this);

        ButterKnife.bind(this);

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        flashlightCheckbox.setOnFlashStateChanged(new FlashlightButton.OnFlashStateChanged() {
            @Override
            public void flashOff() {
                mPreview.setFlashOff();
            }

            @Override
            public void flashOn() {
                mPreview.setFlashOn();
            }

            @Override
            public void flashAuto() {
                mPreview.setFlashAuto();
            }
        });

        safeBackCameraOpenInView();

        mFaceCameraId = faceCameraId();
        if (mFaceCameraId == null) {
            mCameraRotate.setVisibility(View.GONE);
        } else {
            mCameraRotate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        safeFaceCameraOpenInView();
                    } else {
                        safeBackCameraOpenInView();
                    }
                }
            });
        }

        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    @Nullable
    private Integer faceCameraId() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        final int numberOfCameras = Camera.getNumberOfCameras();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return id;
            }
        }

        return null;
    }

    private boolean safeCameraOpenInView(Camera camera) {
        mCamera = camera;

        final boolean cameraOpened = mCamera != null;
        if (cameraOpened) {
            mPreview = new CameraPreview(this, mCamera, mCameraPreview);
            mCameraPreview.removeAllViews();
            mCameraPreview.addView(mPreview);
            mPreview.startCameraPreview();
            checkFlashLightCapability();
        }

        return cameraOpened;
    }

    private void checkFlashLightCapability() {
        final List<String> supportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
        if (supportedFlashModes != null && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            flashlightCheckbox.setVisibility(View.VISIBLE);
        } else {
            flashlightCheckbox.setVisibility(View.GONE);
        }
    }

    private boolean safeFaceCameraOpenInView() {
        releaseCameraAndPreview();
        return safeCameraOpenInView(Camera.open(mFaceCameraId));
    }

    private boolean safeBackCameraOpenInView() {
        releaseCameraAndPreview();
        return safeCameraOpenInView(Camera.open());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }

    @SuppressWarnings("deprecation")
    class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private SurfaceHolder mHolder;

        private Camera mCamera;

        private Context mContext;

        private Camera.Size mPreviewSize;

        private List<Camera.Size> mSupportedPreviewSizes;

        private View mCameraView;

        public CameraPreview(Context context, Camera camera, View cameraView) {
            super(context);

            mCameraView = cameraView;
            mContext = context;
            setCamera(camera);

            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);

            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void startCameraPreview() {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setCamera(Camera camera) {
            mCamera = camera;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

            final List<String> supportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
            if (supportedFlashModes != null && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            }

            requestLayout();
        }

        private void setFlashMode(@NonNull String flashMode) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(flashMode);
            mCamera.setParameters(parameters);
        }

        public void setFlashOn() {
            setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }

        public void setFlashOff() {
            setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }

        public void setFlashAuto() {
            setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if (mHolder.getSurface() == null) {
                return;
            }

            try {
                Camera.Parameters parameters = mCamera.getParameters();

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                if (mPreviewSize != null) {
                    Camera.Size previewSize = mPreviewSize;
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                }

//                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (changed) {
                final int width = right - left;
                final int height = bottom - top;

                int previewWidth = width;
                int previewHeight = height;

                Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                if (mPreviewSize != null) {
                    switch (display.getRotation()) {
                        case Surface.ROTATION_0:
                            previewWidth = mPreviewSize.height;
                            previewHeight = mPreviewSize.width;
                            break;
                        case Surface.ROTATION_90:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            break;
                        case Surface.ROTATION_180:
                            previewWidth = mPreviewSize.height;
                            previewHeight = mPreviewSize.width;
                            break;
                        case Surface.ROTATION_270:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            break;
                    }
                }

                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        mCamera.setDisplayOrientation(90);
                        break;
                    case Surface.ROTATION_270:
                        mCamera.setDisplayOrientation(180);
                        break;
                    case Surface.ROTATION_180:
                        break;
                    case Surface.ROTATION_90:
                        break;
                }

                final int scaledChildHeight = previewHeight * width / previewWidth;
                mCameraView.layout(0, height - scaledChildHeight, width, height);
            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
            Camera.Size optimalSize = null;

            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) height / width;

            for (Camera.Size size : sizes) {

                if (size.height != width) continue;
                double ratio = (double) size.width / size.height;
                if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE) {
                    optimalSize = size;
                }
            }

            return optimalSize;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                final Optional<File> outputMediaFile = getOutputMediaFile();
                if (outputMediaFile.isPresent()) {
                    Files.write(data, outputMediaFile.get());
                } else {
                    // TODO
                }
            } catch (IOException e) {
                e.printStackTrace(); // TODO
            }
        }
    };

    @SuppressLint("SimpleDateFormat")
    private Optional<File> getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "UltimateCameraGuideApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Camera Guide", "Required media storage does not exist");
                return Optional.absent();
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return Optional.of(mediaFile);
    }
}