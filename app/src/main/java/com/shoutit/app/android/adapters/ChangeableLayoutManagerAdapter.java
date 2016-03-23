package com.shoutit.app.android.adapters;

import android.content.Context;

import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;

public abstract class ChangeableLayoutManagerAdapter extends BaseAdapter {

    public static final int VIEW_TYPE_SHOUT = 20;

    protected boolean isLinearLayoutManager;

    public ChangeableLayoutManagerAdapter(@ForActivity @Nonnull Context context) {
        super(context);
    }

    public void switchLayoutManager(boolean isLinearLayoutManager) {
        this.isLinearLayoutManager = isLinearLayoutManager;
        notifyDataSetChanged();
    }
}
