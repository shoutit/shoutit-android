package com.shoutit.app.android.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.Spinner;

public class StateSpinner extends Spinner {

    public StateSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
    }

    public StateSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public StateSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StateSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StateSpinner(Context context, int mode) {
        super(context, mode);
    }

    public StateSpinner(Context context) {
        super(context);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        StateSpinnerSavedState stateSpinnerSavedState = (StateSpinnerSavedState) state;
        super.onRestoreInstanceState(stateSpinnerSavedState.mSuperState);

        final long selectedItemId = stateSpinnerSavedState.selectedItemId();

        final android.widget.SpinnerAdapter adapter = getAdapter();
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                final int count = adapter.getCount();
                for (int i = 0; i < count; i++) {
                    final long itemId = adapter.getItemId(i);
                    if (itemId == selectedItemId) {
                        setSelection(i);
                        adapter.unregisterDataSetObserver(this);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable parcelable = super.onSaveInstanceState();
        final long selectedItemId = getSelectedItemId();

        return new StateSpinnerSavedState(parcelable, selectedItemId);
    }

    static class StateSpinnerSavedState extends BaseSavedState {

        private long mSelectedItemId;
        private Parcelable mSuperState;

        public StateSpinnerSavedState(Parcel source) {
            super(source);
            mSelectedItemId = source.readLong();
            mSuperState = source.readParcelable(Spinner.BaseSavedState.class.getClassLoader());
        }

        public StateSpinnerSavedState(Parcelable superState, long selectedItemId) {
            super(superState);
            mSuperState = superState;
            mSelectedItemId = selectedItemId;
        }

        public long selectedItemId() {
            return mSelectedItemId;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(mSelectedItemId);
            out.writeParcelable(mSuperState, flags);
        }

        public static final Parcelable.Creator<StateSpinnerSavedState> CREATOR =
                new Parcelable.Creator<StateSpinnerSavedState>() {
                    public StateSpinnerSavedState createFromParcel(Parcel in) {
                        return new StateSpinnerSavedState(in);
                    }

                    public StateSpinnerSavedState[] newArray(int size) {
                        return new StateSpinnerSavedState[size];
                    }
                };
    }
}
