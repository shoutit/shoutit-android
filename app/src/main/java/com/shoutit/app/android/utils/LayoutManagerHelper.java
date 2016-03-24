package com.shoutit.app.android.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;

import javax.annotation.Nonnull;

public class LayoutManagerHelper {

    public static void setLinearLayoutManager(@Nonnull Context context,
                                              @Nonnull RecyclerView recyclerView,
                                              @Nonnull ChangeableLayoutManagerAdapter adapter) {
        recyclerView.setLayoutManager(new MyLinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        adapter.switchLayoutManager(true);
    }

    public static void setGridLayoutManager(@Nonnull Context context,
                                            @Nonnull RecyclerView recyclerView,
                                            @Nonnull final ChangeableLayoutManagerAdapter adapter) {
        final MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(context, 2);
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
