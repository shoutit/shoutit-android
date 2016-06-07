package com.shoutit.app.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class CheckableImageButton extends ImageButton {

    private boolean mChecked;

    public CheckableImageButton(Context context) {
        super(context);
    }

    public CheckableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    private void toggle() {
        setChecked(!mChecked);
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public boolean isChecked() {
        return mChecked;
    }
}
