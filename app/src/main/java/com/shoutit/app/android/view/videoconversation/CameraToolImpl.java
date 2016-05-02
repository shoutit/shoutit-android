package com.shoutit.app.android.view.videoconversation;

import android.hardware.Camera;

import javax.inject.Inject;

public class CameraToolImpl implements CameraTool {

    @Inject
    public CameraToolImpl() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFrontCameraAvailable() throws CameraException {
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            final Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (Camera.CameraInfo.CAMERA_FACING_FRONT == info.facing) {
                return true;
            }
        }
        return false;
    }
}
