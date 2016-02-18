package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;

public class ShoutsAdapter extends BaseAdapter {

    @Inject
    public ShoutsAdapter(@ForActivity Context context) {
        super(context);
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {

    }
}
