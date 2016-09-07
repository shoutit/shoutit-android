package com.shoutit.app.android.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

public class MyLinearLayoutManager extends LinearLayoutManager implements MyLayoutManager {

    public MyLinearLayoutManager(Context context) {
        super(context);
        setRecycleChildrenOnDetach(true);
        setAutoMeasureEnabled(true);
    }

    public MyLinearLayoutManager(Context context, int horizontal, boolean b) {
        super(context, horizontal, b);
        setRecycleChildrenOnDetach(true);
        setAutoMeasureEnabled(true);
    }
}
