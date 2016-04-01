package com.shoutit.app.android.widget;

import android.content.Context;
import android.support.annotation.StringRes;

public class SimpleCurrencySpinnerAdapter extends CurrencySpinnerAdapter {

    public SimpleCurrencySpinnerAdapter(@StringRes int startingText, Context context) {
        super(startingText, context, android.R.layout.simple_list_item_1, android.R.layout.simple_list_item_1);
    }
}