package com.shoutit.app.android.view.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreateOfferShoutWithImageRequest;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.EditShoutPriceRequest;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.SystemUIUtils;
import com.shoutit.app.android.widget.FlashlightButton;
import com.shoutit.app.android.widget.SpinnerAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@SuppressWarnings("deprecation")
public class NativeCameraActivity extends BaseActivity {

    private static final int STATE_PHOTO_CAPTURE = 0;
    private static final int STATE_PHOTO_TAKEN = 1;
    private static final int STATE_PHOTO_PUBLISHED = 2;

    private Camera mCamera;

    private CameraPreview mPreview;

    @Bind(R.id.camera_flashlight)
    FlashlightButton flashlightCheckbox;

    @Bind(R.id.camera_rotate)
    CheckBox mCameraRotate;

    @Bind(R.id.camera_close)
    ImageButton closeButton;

    @Bind(R.id.button_capture)
    ImageButton captureButton;

    @Bind(R.id.camera_preview)
    FrameLayout mCameraPreview;

    @Bind(R.id.camera_camera_controlls)
    ViewGroup mCameraControlls;

    @Bind(R.id.camera_capture_controlls)
    ViewGroup mCameraCaptureControlls;

    @Bind(R.id.camera_preview_controlls)
    ViewGroup mCameraPreviewControlls;

    @Bind(R.id.camera_preview_image)
    ImageView mCameraImagePreview;

    @Bind(R.id.camera_preview_controlls_publish)
    Button mPublishButton;

    @Bind(R.id.camera_published_layout)
    ViewGroup mPublishedLayout;

    @Bind(R.id.camera_published_currency)
    Spinner mPublishedCurrencySpinner;

    @Bind(R.id.camera_published_price)
    EditText mPublishedCurrencyEditText;

    @Bind(R.id.camera_progress)
    ViewGroup mProgressBar;

    @Inject
    AmazonHelper mAmazonHelper;

    @Inject
    ApiService mApiService;

    private String createdShoutOfferId;
    private Integer mFaceCameraId;
    private SpinnerAdapter mCurrencyAdapter;

    public static NativeCameraActivity newInstance() {
        return new NativeCameraActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        SystemUIUtils.setFullscreen(this);

        ButterKnife.bind(this);

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
        }

        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        mCurrencyAdapter = new SpinnerAdapter(R.string.camera_publish_currency, this);
        mPublishedCurrencySpinner.setAdapter(mCurrencyAdapter);
    }

    @OnClick(R.id.camera_preview_controlls_retake)
    public void previewRetake() {
        photoTakenState(false);
        captureState(true);
    }

    @OnClick(R.id.button_capture)
    public void capture() {
        mCamera.takePicture(null, null, mPicture);
    }

    @SuppressWarnings("unchecked")
    @OnClick(R.id.camera_published_done)
    public void done() {
        final String price = mPublishedCurrencyEditText.getText().toString();
        if (!Strings.isNullOrEmpty(price)) {
            final long priceInCents = PriceUtils.getPriceInCents(price);
            final Pair<String, String> selectedItem = (Pair<String, String>) mPublishedCurrencySpinner.getSelectedItem();
            mApiService.editShoutPrice(createdShoutOfferId, new EditShoutPriceRequest(priceInCents, selectedItem.first))
                    .subscribeOn(Schedulers.io())
                    .observeOn(MyAndroidSchedulers.mainThread())
                    .subscribe(new Action1<CreateShoutResponse>() {
                        @Override
                        public void call(CreateShoutResponse createShoutResponse) {
                            finish();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            // TODO
                        }
                    });
        } else {
            // TODO error
        }
    }

    @OnClick(R.id.camera_close)
    public void close() {
        finish();
    }

    @OnCheckedChanged(R.id.camera_rotate)
    public void captureCameraRotate(boolean checked) {
        if (checked) {
            safeFaceCameraOpenInView();
        } else {
            safeBackCameraOpenInView();
        }
    }

    private void captureState(boolean enter) {
        int visibility = enter ? View.VISIBLE : View.GONE;
        mCameraCaptureControlls.setVisibility(visibility);
        mCameraControlls.setVisibility(visibility);
        mCameraPreview.setVisibility(visibility);

        if (enter) {
            safeBackCameraOpenInView();
        } else {
            releaseCameraAndPreview();
        }
    }

    private void photoTakenState(boolean enter) {
        int visibility = enter ? View.VISIBLE : View.GONE;
        mCameraImagePreview.setVisibility(visibility);
        mCameraPreviewControlls.setVisibility(visibility);
    }

    private void publishedState(boolean enter) {
        int visibility = enter ? View.VISIBLE : View.GONE;
        mPublishedLayout.setVisibility(visibility);
        mApiService.getCurrencies()
                .subscribeOn(Schedulers.io())
                .observeOn(MyAndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<Currency>>() {
                            @Override
                            public void call(List<Currency> currencies) {
                                mCurrencyAdapter.setData(PriceUtils.transformCurrencyToPair(currencies));
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                // TODO
                            }
                        });
    }

    @Nullable
    private Integer faceCameraId() {
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
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

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final NativeCameraActivityComponent component = DaggerNativeCameraActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);
        return component;
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
                e.printStackTrace(); // TODO
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
                e.printStackTrace(); // TODO
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
                final Camera.Parameters parameters = mCamera.getParameters();

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                if (mPreviewSize != null) {
                    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                }

                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace(); // TODO
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

                final Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
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
                    final File file = outputMediaFile.get();
                    Files.write(data, file); // TODO replace with compress bitmap

                    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    final Matrix matrix = new Matrix();
                    final Display display = ((WindowManager) NativeCameraActivity.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                    Bitmap finalBitmap = bitmap;

                    switch (display.getRotation()) {
                        case Surface.ROTATION_0:
                            matrix.postRotate(90);
                            finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            bitmap.recycle();
                            break;
                        case Surface.ROTATION_270:
                            matrix.postRotate(180);
                            finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            bitmap.recycle();
                            break;
                        case Surface.ROTATION_180:
                        case Surface.ROTATION_90:
                            break;
                    }

                    mCameraImagePreview.setImageBitmap(finalBitmap);

                    captureState(false);
                    photoTakenState(true);

                    mPublishButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mProgressBar.setVisibility(View.VISIBLE);
                            mAmazonHelper.uploadShoutImageObservable(file)
                                    .flatMap(new Func1<String, Observable<CreateShoutResponse>>() {
                                        @Override
                                        public Observable<CreateShoutResponse> call(String url) {
                                            return mApiService.createShoutOffer(CreateOfferShoutWithImageRequest.withImage(url))
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(MyAndroidSchedulers.mainThread());
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(MyAndroidSchedulers.mainThread())
                                    .doOnTerminate(new Action0() {
                                        @Override
                                        public void call() {
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    })
                                    .subscribe(
                                            new Action1<CreateShoutResponse>() {
                                                @Override
                                                public void call(CreateShoutResponse createShoutResponse) {
                                                    createdShoutOfferId = createShoutResponse.getId();
                                                    publishedState(true);
                                                }
                                            }, new Action1<Throwable>() {
                                                @Override
                                                public void call(Throwable throwable) {
                                                    // TODO error
                                                }
                                            });
                        }
                    });
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