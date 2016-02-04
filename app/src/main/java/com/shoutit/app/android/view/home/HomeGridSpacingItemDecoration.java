package com.shoutit.app.android.view.home;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class HomeGridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spacing;

    public HomeGridSpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        final int viewType = parent.getAdapter().getItemViewType(position);
        if (viewType != HomeAdapter.VIEW_TYPE_SHOUT_ITEM) {
            return;
        }

        if (position % 2 == 0) {
            outRect.right = spacing;
        } else {
            outRect.left = spacing;
        }
    }
}