package com.shoutit.app.android.view.promote;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.dagger.ForActivity;

import javax.annotation.Nonnull;

public class PromoteAdapter extends BaseAdapter {

    public PromoteAdapter(@ForActivity @Nonnull Context context) {
        super(context);
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }
}
