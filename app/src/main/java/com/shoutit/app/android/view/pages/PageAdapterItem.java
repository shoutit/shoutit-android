package com.shoutit.app.android.view.pages;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;

import rx.Observer;

public class PageAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final User page;
    @Nonnull
    private final Observer<User> pageSelectedObserver;

    public PageAdapterItem(@Nonnull User page,
                           @Nonnull Observer<User> pageSelectedObserver) {
        this.page = page;
        this.pageSelectedObserver = pageSelectedObserver;
    }

    @Nonnull
    public User getPage() {
        return page;
    }

    public void onPagesSelected() {
        pageSelectedObserver.onNext(page);
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return baseAdapterItem instanceof PageAdapterItem &&
                ((PageAdapterItem) baseAdapterItem).page.getId().equals(page.getId());
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
        return baseAdapterItem instanceof PageAdapterItem &&
                ((PageAdapterItem) baseAdapterItem).page.equals(page);
    }
}