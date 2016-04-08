package com.shoutit.app.android.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;

import com.shoutit.app.android.api.model.SortType;

public class SortTypeSpinnerAdapter extends BaseSpinnerAdapter<SortType> {

    public SortTypeSpinnerAdapter(Context context, @LayoutRes int layoutId) {
        super(context, layoutId);
    }

    @Override
    protected String getDisplayName(int position) {
        return getItem(position).getName();
    }
}
