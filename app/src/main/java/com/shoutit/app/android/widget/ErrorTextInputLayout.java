package com.shoutit.app.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ErrorTextInputLayout extends LinearLayout {

    private static final int ANIMATION_DURATION = 200;
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private EditText mEditText;

    private boolean mErrorEnabled;
    private TextView mErrorView;
    private int mErrorTextAppearance;

    public ErrorTextInputLayout(Context context) {
        this(context, null);
    }

    public ErrorTextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ErrorTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        // Can't call through to super(Context, AttributeSet, int) since it doesn't exist on API 10
        super(context, attrs);

        setOrientation(VERTICAL);
        setWillNotDraw(false);
        setAddStatesFromChildren(true);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                android.support.design.R.styleable.TextInputLayout, defStyleAttr, android.support.design.R.style.Widget_Design_TextInputLayout);

        mErrorTextAppearance = a.getResourceId(android.support.design.R.styleable.TextInputLayout_errorTextAppearance, 0);
        final boolean errorEnabled = a.getBoolean(android.support.design.R.styleable.TextInputLayout_errorEnabled, false);
        a.recycle();

        setErrorEnabled(errorEnabled);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            setEditText((EditText) child);
            super.addView(child, 0, params);
        } else {
            super.addView(child, index, params);
        }
    }

    private void setEditText(EditText editText) {
        // If we already have an EditText, throw an exception
        if (mEditText != null) {
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        }
        mEditText = editText;
        if (mErrorView != null) {
            ViewCompat.setPaddingRelative(mErrorView, ViewCompat.getPaddingStart(mEditText),
                    0, ViewCompat.getPaddingEnd(mEditText), mEditText.getPaddingBottom());
        }
    }

    public void setErrorEnabled(boolean enabled) {
        if (mErrorEnabled != enabled) {
            if (mErrorView != null) {
                ViewCompat.animate(mErrorView).cancel();
            }

            if (enabled) {
                mErrorView = new TextView(getContext());
                mErrorView.setTextAppearance(getContext(), mErrorTextAppearance);
                mErrorView.setVisibility(GONE);
                addView(mErrorView);

                if (mEditText != null) {
                    // Add some start/end padding to the error so that it matches the EditText
                    ViewCompat.setPaddingRelative(mErrorView, ViewCompat.getPaddingStart(mEditText),
                            0, ViewCompat.getPaddingEnd(mEditText), mEditText.getPaddingBottom());
                }
            } else {
                removeView(mErrorView);
                mErrorView = null;
            }
            mErrorEnabled = enabled;
        }
    }

    public void setError(@Nullable CharSequence error) {
        if (!mErrorEnabled) {
            if (TextUtils.isEmpty(error)) {
                // If error isn't enabled, and the error is empty, just return
                return;
            }
            // Else, we'll assume that they want to enable the error functionality
            setErrorEnabled(true);
        }

        if (!TextUtils.isEmpty(error)) {
            showError(error);
        } else {
            hideError();
        }

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    private void showError(@Nullable CharSequence error) {
        ViewCompat.setAlpha(mErrorView, 0f);
        mErrorView.setText(error);
        ViewCompat.animate(mErrorView)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(View view) {
                        view.setVisibility(VISIBLE);
                    }
                })
                .start();

        ViewCompat.setBackgroundTintList(mEditText,
                ColorStateList.valueOf(mErrorView.getCurrentTextColor()));
    }

    public void hideError() {
        if (mErrorView != null && mErrorView.getVisibility() == VISIBLE) {
            ViewCompat.animate(mErrorView)
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            view.setVisibility(GONE);
                        }
                    }).start();

            // Restore the 'original' tint, using colorControlNormal and colorControlActivated
            ViewCompat.setBackgroundTintList(mEditText, ContextCompat.getColorStateList(getContext(), android.support.design.R.drawable.abc_edit_text_material));
        }
    }
}