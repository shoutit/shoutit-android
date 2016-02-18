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

public class DiscoverShoutAdapterItem implements BaseAdapterItem {

    @Nonnull
    private final Shout shout;
    @Nonnull
    private final Context context;

    public DiscoverShoutAdapterItem(@Nonnull Shout shout, @Nonnull Context context) {
        this.shout = shout;
        this.context = context;
    }

    @Override
    public long adapterId() {
        return BaseAdapterItem.NO_ID;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof DiscoverShoutAdapterItem &&
                shout.getId().equals(((DiscoverShoutAdapterItem) item).shout.getId());
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
            return Strings.emptyToNull(shout.getCategory().getMainTag().getImage());
        } else {
            return null;
        }
    }

    @IdRes
    @NonNull
    public Optional<Integer> getCountryResId() {
        if (shout.getLocation() != null && !TextUtils.isEmpty(shout.getLocation().getCountry())) {
            final String countryCode = shout.getLocation().getCountry().toLowerCase();
            return Optional.of(ResourcesHelper.getResourceIdForName(countryCode, context));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DiscoverShoutAdapterItem that = (DiscoverShoutAdapterItem) o;

        return shout.equals(that.shout);

    }

    @Override
    public int hashCode() {
        return shout.hashCode();
    }
}
