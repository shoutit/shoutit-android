package com.shoutit.app.android.view.createpage.pagecategory;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PageCategory;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

public class CreatePageCategoryPresenter {

    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private Listener mListener;
    private CompositeSubscription mCompositeSubscription;

    @Inject
    public CreatePageCategoryPresenter(@NonNull ApiService apiService,
                                       @NetworkScheduler Scheduler networkScheduler,
                                       @UiScheduler Scheduler uiScheduler) {
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
    }

    public void register(Listener listener) {
        mListener = listener;
        listener.showProgress(true);
        mCompositeSubscription = new CompositeSubscription(mApiService.pagesCategories()
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(categories -> {
                    listener.showProgress(false);
                    final List<BaseAdapterItem> list = ImmutableList.<BaseAdapterItem>builder()
                            .add(new HeaderItem())
                            .addAll(Iterables.transform(categories, new Function<PageCategory, CategoryItem>() {
                                @Nullable
                                @Override
                                public CategoryItem apply(@Nullable PageCategory input) {
                                    assert input != null;
                                    return new CategoryItem(input.getId(), input.getName(), input.getImage());
                                }
                            }))
                            .build();
                    listener.setData(list);
                }, throwable -> {
                    listener.error();
                }));
    }

    public void unregister() {
        mListener = null;
        mCompositeSubscription.unsubscribe();
    }

    private void itemClicked(String id) {
        mListener.startDetailsActivity(id);
    }

    public interface Listener {

        void setData(List<BaseAdapterItem> items);

        void showProgress(boolean show);

        void error();

        void startDetailsActivity(String categoryId);
    }

    public class CategoryItem extends BaseNoIDAdapterItem {

        private final String id;
        private final String name;
        private final String image;

        public CategoryItem(String id, String name, String image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getImage() {
            return image;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final CategoryItem that = (CategoryItem) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            return image != null ? image.equals(that.image) : that.image == null;

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (image != null ? image.hashCode() : 0);
            return result;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof CategoryItem && ((CategoryItem) baseAdapterItem).getId().equals(id);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return equals(baseAdapterItem);
        }

        public void click() {
            itemClicked(id);
        }
    }

    public static class HeaderItem extends BaseNoIDAdapterItem {


        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof HeaderItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof HeaderItem;
        }
    }
}
