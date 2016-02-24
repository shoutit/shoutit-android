package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.utils.ResourcesHelper;

import javax.annotation.Nonnull;

import rx.Observer;
import rx.subjects.PublishSubject;

public class ShoutAdapterItem implements BaseAdapterItem {

    @Nonnull
    private final Shout shout;
    @Nonnull
    private final Context context;
    @Nonnull
    private final Observer<String> shoutSelectedObserver;

    public ShoutAdapterItem(@Nonnull Shout shout,
                            @Nonnull Context context,
                            @Nonnull Observer<String> shoutSelectedObserver) {
        this.shout = shout;
        this.context = context;
        this.shoutSelectedObserver = shoutSelectedObserver;
    }

    @Override
    public long adapterId() {
        return BaseAdapterItem.NO_ID;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ShoutAdapterItem &&
                shout.getId().equals(((ShoutAdapterItem) item).shout.getId());
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return this.equals(item);
    }

    @Nonnull
    public Shout getShout() {
        return shout;
    }

    @Nullable
    public String getCategoryIconUrl() {
        if (shout.getCategory() != null) {
            return Strings.emptyToNull(shout.getCategory().getIcon());
        } else {
            return null;
        }
    }

    @IdRes
    @NonNull
    public Optional<Integer> getCountryResId() {
        return ResourcesHelper.getCountryResId(context, shout);
    }

    public void onShoutSelected() {
        shoutSelectedObserver.onNext(shout.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ShoutAdapterItem that = (ShoutAdapterItem) o;

        return shout.equals(that.shout);

    }

    @Override
    public int hashCode() {
        return shout.hashCode();
    }
}
