package com.shoutit.app.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.shoutit.app.android.R;

public class FlashlightButton extends ImageButton {

    public interface OnFlashStateChanged {

        void flashOff();

        void flashOn();

        void flashAuto();

    }

    private static final int STATE_OFF = 0;
    private static final int STATE_ON = 1;
    private static final int STATE_AUTO = 2;

    private int mCurrentState = STATE_OFF;

    private OnFlashStateChanged mOnFlashStateChanged;

    public FlashlightButton(Context context) {
        super(context);
        flipState();
    }

    public FlashlightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        flipState();
    }

    public FlashlightButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        flipState();
    }


    private void flipState() {
        switch (mCurrentState) {
            case STATE_OFF: {
                mCurrentState = STATE_ON;
                setImageResource(R.drawable.flash);
                break;
            }
            case STATE_ON: {
                mCurrentState = STATE_AUTO;
                setImageResource(R.drawable.flash_auto);
                break;
            }
            case STATE_AUTO: {
                mCurrentState = STATE_OFF;
                setImageResource(R.drawable.flash_off);
                break;
            }
            default:
                throw new RuntimeException("no switch case for " + mCurrentState);
        }
        notifyListener();
    }

    private void notifyListener() {
        if (mOnFlashStateChanged != null) {
            switch (mCurrentState) {
                case STATE_OFF: {
                    mOnFlashStateChanged.flashOff();
                    break;
                }
                case STATE_ON: {
                    mOnFlashStateChanged.flashOn();
                    break;
                }
                case STATE_AUTO: {
                    mOnFlashStateChanged.flashAuto();
                    break;
                }
                default:
                    throw new RuntimeException("no switch case for " + mCurrentState);
            }
        }
    }

    @Override
    public boolean performClick() {
        flipState();
        return super.performClick();
    }

    public void setOnFlashStateChanged(OnFlashStateChanged onFlashStateChanged) {
        mOnFlashStateChanged = onFlashStateChanged;
    }
}
