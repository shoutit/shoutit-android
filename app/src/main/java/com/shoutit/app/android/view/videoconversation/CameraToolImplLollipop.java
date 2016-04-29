package com.shoutit.app.android.view.videoconversation;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import com.shoutit.app.android.dagger.ForApplication;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraToolImplLollipop implements CameraTool {

    @Nonnull
    private final CameraManager cameraManager;

    @Inject
    public CameraToolImplLollipop(@Nonnull @ForApplication Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public boolean isFrontCameraAvailable() throws CameraException {
        try {
            for (final String cameraID : cameraManager.getCameraIdList()) {
                final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
                final int cameraOrientation = checkNotNull(characteristics.get(CameraCharacteristics.LENS_FACING));
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    return true;
                }
            }
            return false;
        } catch (CameraAccessException e) {
            throw new CameraException(e);
        }
    }
}
