package com.shoutit.app.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import com.appunite.detector.ChangesDetector;
import com.appunite.detector.SimpleDetector;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.dagger.ForActivity;

import java.util.List;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public abstract class BaseAdapter extends RecyclerView.Adapter<ViewHolderManager.BaseViewHolder> implements
        Action1<List<BaseAdapterItem>>, ChangesDetector.ChangesAdapter {

    @Nonnull
    private final ChangesDetector<BaseAdapterItem, BaseAdapterItem> changesDetector;
    @Nonnull
    protected final Context context;
    @Nonnull
    protected LayoutInflater layoutInflater;
    @Nonnull
    protected List<BaseAdapterItem> items = ImmutableList.of();

    public BaseAdapter(@ForActivity Context context) {
        this.context = context;
        changesDetector = new ChangesDetector<>(new SimpleDetector<BaseAdapterItem>());
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void call(List<BaseAdapterItem> items) {
        this.items = items;
        changesDetector.newData(this, items, false);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
