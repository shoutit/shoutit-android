package com.shoutit.app.android.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class SpanUtils {

    public static SpannableString clickableColoredSpan(CharSequence wholeText, CharSequence clickablePart, int color, OnClickListener onClickListener) {
        return clickableStyledSpan(wholeText, clickablePart, ImmutableList.of(new ForegroundColorSpan(color)), onClickListener);
    }

    public static SpannableString clickableColoredUnderlinedSpan(CharSequence wholeText, CharSequence clickablePart, int color, OnClickListener onClickListener) {
        return clickableStyledSpan(wholeText, clickablePart, ImmutableList.of(new UnderlineSpan(), new ForegroundColorSpan(color)), onClickListener);
    }

    public static SpannableString clickableStyledSpan(CharSequence wholeText, CharSequence clickablePart, List<Object> stylesSpan, OnClickListener onClickListener) {
        final int start = new StringBuffer(wholeText).indexOf(String.valueOf(clickablePart));
        final int end = start + clickablePart.length();

        final SpannableString spannableString = new SpannableString(wholeText);
        spannableString.setSpan(new NoUnderlineClickableSpan() {
            @Override
            public void onClick(View widget) {
                onClickListener.onClick();
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        for (Object styleSpan : stylesSpan) {
            spannableString.setSpan(styleSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    public interface OnClickListener {
        void onClick();
    }

    public abstract static class NoUnderlineClickableSpan extends ClickableSpan {

        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }
    }
}
