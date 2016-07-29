package com.shoutit.app.android.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

public class MyGridLayoutManager extends GridLayoutManager implements MyLayoutManager {

    public MyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        setRecycleChildrenOnDetach(true);
    }

}
