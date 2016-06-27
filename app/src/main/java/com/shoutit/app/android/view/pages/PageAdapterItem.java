package com.shoutit.app.android.view.pages;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.Page;

import javax.annotation.Nonnull;

import rx.Observer;

public class PageAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final Page page;
    @Nonnull
    private final Observer<Page> pageSelectedObserver;

    public PageAdapterItem(@Nonnull Page page,
                           @Nonnull Observer<Page> pageSelectedObserver) {
        this.page = page;
        this.pageSelectedObserver = pageSelectedObserver;
    }

    @Nonnull
    public Page getPage() {
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