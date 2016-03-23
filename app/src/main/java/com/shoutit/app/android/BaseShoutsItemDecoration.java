package com.shoutit.app.android;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;


public class BaseShoutsItemDecoration extends RecyclerView.ItemDecoration {

    private final int spacing;
    private int firstGridPosition = -1;
    private boolean isFirstGridPositionEven;

    public BaseShoutsItemDecoration(int spacingInPx) {
        this.spacing = spacingInPx;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        final RecyclerView.Adapter adapter = parent.getAdapter();
        if (layoutManager == null || position == RecyclerView.NO_POSITION || adapter == null) {
            return;
        }

        final int viewType = adapter.getItemViewType(position);
        if (viewType != ChangeableLayoutManagerAdapter.VIEW_TYPE_SHOUT) {
            return;
        }

        if ((layoutManager instanceof MyGridLayoutManager && ((GridLayoutManager) layoutManager).getSpanSizeLookup().getSpanSize(position) == 2) ||
                layoutManager instanceof MyLinearLayoutManager) {
            outRect.left = spacing;
            outRect.right = spacing;
        } else {
            if (firstGridPosition == -1) {
                firstGridPosition = position;
                isFirstGridPositionEven = firstGridPosition % 2 == 0;
            }

            boolean isCurrentPositionEven = position % 2 == 0;
            if (isFirstGridPositionEven) {
                if (isCurrentPositionEven) {
                    outRect.left = spacing;
                } else {
                    outRect.right = spacing;
                }
            } else {
                if (isCurrentPositionEven) {
                    outRect.right = spacing;
                } else {
                    outRect.left = spacing;
                }
            }
        }
    }
}