package com.shoutit.app.android.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;

import javax.annotation.Nonnull;

public class LayoutManagerHelper {

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
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == ChangeableLayoutManagerAdapter.VIEW_TYPE_SHOUT) {
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
}
