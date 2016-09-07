package com.shoutit.app.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import com.shoutit.app.android.R;

public class ListenCheckedTextView extends CheckedTextView {
    public ListenCheckedTextView(Context context) {
        super(context);
    }

    public ListenCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListenCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListenCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        setText(checked ? R.string.listening : R.string.listen);
    }

    public void setListened(boolean listened) {
        setChecked(listened);
    }
}
