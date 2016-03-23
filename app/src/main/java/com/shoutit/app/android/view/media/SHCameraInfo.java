package com.shoutit.app.android.view.media;

import android.hardware.Camera;

public class SHCameraInfo {

  private boolean hasFrontFacingCamera = false;
  private boolean hasBackFacingCamera = false;
  private int numberOfCameras = 0;
  private static SHCameraInfo shCameraInfo;

  public static SHCameraInfo getInstance() {
    if (shCameraInfo == null) {
      shCameraInfo = new SHCameraInfo();
    }
    return shCameraInfo;
  }

  @SuppressWarnings("deprecation")
  private SHCameraInfo() {
    Camera.CameraInfo ci = new Camera.CameraInfo();
    numberOfCameras = Camera.getNumberOfCameras();
    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
      Camera.getCameraInfo(i, ci);

      if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        hasFrontFacingCamera = true;
      } else if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        hasBackFacingCamera = true;
      }
    }
  }

  public boolean isHasBackFacingCamera() {
    return hasBackFacingCamera;
  }

  public boolean isHasFrontFacingCamera() {
    return hasFrontFacingCamera;
  }

  public int getNumberOfCameras() {
    return numberOfCameras;
  }
}
