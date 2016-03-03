package com.shoutit.app.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.shoutit.app.android.R;

public class LinedTextView extends TextView {

    private Paint mPaint;
    private int internalPadding;

    public LinedTextView(Context context) {
        super(context);
        init();
    }

    public LinedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LinedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        internalPadding = getContext().getResources().getDimensionPixelSize(R.dimen.textview_padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final TextPaint paint = getPaint();
        final CharSequence text = getText();

        final int heightMiddle = getHeight() / 2;
        final int widthMiddle = getWidth() / 2;
        final int halfText = (int) (paint.measureText(text, 0, text.length()) / 2);

        super.onDraw(canvas);

        canvas.drawLine(0, heightMiddle - 1, widthMiddle - halfText - internalPadding, heightMiddle + 1, mPaint);
        canvas.drawLine(widthMiddle + halfText + internalPadding, heightMiddle - 1, getWidth(), heightMiddle + 1, mPaint);
    }
}