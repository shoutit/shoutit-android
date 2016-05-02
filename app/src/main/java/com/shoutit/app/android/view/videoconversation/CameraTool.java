package com.shoutit.app.android.view.videoconversation;

public interface CameraTool {

    class CameraException extends Exception {

        public CameraException(Exception e) {
            super(e);
        }
    }

    boolean isFrontCameraAvailable() throws CameraException;
}
