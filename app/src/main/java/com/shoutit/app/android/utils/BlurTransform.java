package com.shoutit.app.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;

import com.shoutit.app.android.BuildConfig;

public class BlurTransform {

    private final RenderScript mRenderScript;

    public BlurTransform(@NonNull Context context) {
        mRenderScript = RenderScript.create(context);
    }

    @NonNull
    public Bitmap transform(@NonNull Bitmap bitmap, boolean recycleOld) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return bitmap;
        }

        if (!Bitmap.Config.ARGB_8888.equals(bitmap.getConfig())) {
            final Bitmap tmp = Bitmap
                    .createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(tmp);
            canvas.drawBitmap(bitmap, 0.f, 0.f, null);
            bitmap.recycle();
            bitmap = tmp;
        }

        // Create another bitmap that will hold the results of the filter.
        final Bitmap blurredBitmap = Bitmap
                .createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        // Allocate memory for Renderscript to work with
        final Allocation input = Allocation.createFromBitmap(mRenderScript, bitmap);
        final Allocation output = Allocation.createFromBitmap(mRenderScript, blurredBitmap);

        // Load up an instance of the specific script that we want to use.
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur
                .create(mRenderScript, Element.U8_4(mRenderScript));
        script.setInput(input);

        // Set the blur radius
        script.setRadius(25);

        // Start the ScriptIntrinisicBlur
        script.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(blurredBitmap);

        if (recycleOld) {
            bitmap.recycle();
        }

        return blurredBitmap;
    }

}
