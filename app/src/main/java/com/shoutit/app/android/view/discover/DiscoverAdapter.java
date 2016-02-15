package com.shoutit.app.android.view.discover;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DiscoverAdapter extends BaseAdapter {

    @Inject
    public DiscoverAdapter(@ForActivity @Nonnull Context context) {
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
