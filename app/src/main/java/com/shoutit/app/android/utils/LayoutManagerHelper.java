package com.shoutit.app.android.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CheckedTextView;

import com.shoutit.app.android.R;
import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;
import com.shoutit.app.android.adapters.FBAdsAdapter;

import javax.annotation.Nonnull;

public class LayoutManagerHelper {

    private final MyGridLayoutManager gridLayoutManager;
    private final MyLinearLayoutManager linearLayoutManager;
    private final Context mContext;
    private final ChangeableLayoutManagerAdapter mAdapter;
    private final RecyclerView mRecyclerView;
    private final CheckedTextView mLayoutSwitchIcon;

    public LayoutManagerHelper(Context context, ChangeableLayoutManagerAdapter adapter, RecyclerView recyclerView, CheckedTextView layoutSwitchIcon) {
        mContext = context;
        mAdapter = adapter;
        mRecyclerView = recyclerView;
        mLayoutSwitchIcon = layoutSwitchIcon;

        gridLayoutManager = new MyGridLayoutManager(context, 2);
        linearLayoutManager = new MyLinearLayoutManager(context);
    }

    public static void setLinearLayoutManager(@Nonnull RecyclerView recyclerView,
                                              @Nonnull ChangeableLayoutManagerAdapter adapter,
                                              @Nonnull MyLinearLayoutManager linearLayoutManager) {
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.switchLayoutManager(true);
    }

    public static void setGridLayoutManager(@Nonnull RecyclerView recyclerView,
                                            @Nonnull final ChangeableLayoutManagerAdapter adapter,
                                            @Nonnull final MyGridLayoutManager gridLayoutManager) {
        gridLayoutManager.setSpanSizeLookup(new MyGridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == ChangeableLayoutManagerAdapter.VIEW_TYPE_SHOUT ||
                        adapter.getItemViewType(position) == FBAdsAdapter.VIEW_TYPE_AD) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.switchLayoutManager(false);
    }

    public void setGridLayoutManager() {
        setGridLayoutManager(mRecyclerView, mAdapter, gridLayoutManager);
    }

    public void setupLayoutSwitchIcon() {
        mLayoutSwitchIcon.setOnClickListener(v -> {
            mLayoutSwitchIcon.setChecked(!mLayoutSwitchIcon.isChecked());
            if (mLayoutSwitchIcon.isChecked()) {
                mLayoutSwitchIcon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_grid_switch));
                setLinearLayoutManager(mRecyclerView, mAdapter, linearLayoutManager);
            } else {
                mLayoutSwitchIcon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_list_switch));
                setGridLayoutManager(mRecyclerView, mAdapter, gridLayoutManager);
            }
        });
    }

    public MyGridLayoutManager getGridLayoutManager() {
        return gridLayoutManager;
    }

    public MyLinearLayoutManager getLinearLayoutManager() {
        return linearLayoutManager;
    }
}
