package com.shoutit.app.android.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;

import com.shoutit.app.android.api.model.Category;

public class CategorySpinnerAdapter extends BaseSpinnerAdapter<Category> {

    public CategorySpinnerAdapter(Context context, @LayoutRes int layoutId) {
        super(context, layoutId);
    }

    @Override
    protected String getDisplayName(int position) {
        return getItem(position).getName();
    }
}
