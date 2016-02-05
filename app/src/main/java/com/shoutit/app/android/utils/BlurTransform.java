package com.shoutit.app.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public class BlurTransform {

    public BlurTransform(@NonNull Context context) {
    }

    @NonNull
    public Bitmap transform(@NonNull Bitmap bitmap, boolean recycleOld) {
        return bitmap;
    }

}
