package com.shoutit.app.android.view.search.categories;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.dao.CategoriesDao;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SearchCategoriesPresenter {

    private final PublishSubject<String> categorySelectedObserver = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> categoriesObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorsObservable;

    @Inject
    public SearchCategoriesPresenter(CategoriesDao categoriesDao,
                                     @UiScheduler Scheduler uiScheduler) {

        final Observable<ResponseOrError<List<Category>>> categoriesRequest = categoriesDao.getListObservableResponseOrError()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<List<Category>>>behaviorRefCount());

        categoriesObservable = categoriesRequest
                .compose(ResponseOrError.<List<Category>>onlySuccess())
                .map(new Func1<List<Category>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Category> categories) {
                        final List<BaseAdapterItem> items = Lists.transform(categories, new Function<Category, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(@Nullable Category input) {
                                return new CategoryAdapterItem(input, categorySelectedObserver);
                            }
                        });

                        return ImmutableList.<BaseAdapterItem>builder()
                                .add(new NoDataAdapterItem())
                                .addAll(items)
                                .build();

                    }
                });

        progressObservable = categoriesRequest.map(Functions1.returnFalse())
                .startWith(true);

        errorsObservable = categoriesRequest
                .compose(ResponseOrError.<List<Category>>onlyError());
    }

    public Observable<List<BaseAdapterItem>> getCategoriesObservable() {
        return categoriesObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observable<Throwable> getErrorsObservable() {
        return errorsObservable;
    }

    public Observable<String> getCategorySelectedObservable() {
        return categorySelectedObserver;
    }

    public static class CategoryAdapterItem extends BaseNoIDAdapterItem {

        private final Category category;
        private final Observer<String> categorySelectedObserver;

        public CategoryAdapterItem(Category category, Observer<String> categorySelectedObserver) {
            this.category = category;
            this.categorySelectedObserver = categorySelectedObserver;
        }

        public Category getCategory() {
            return category;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof CategoryAdapterItem &&
                    category.getSlug().equals(((CategoryAdapterItem) item).getCategory().getSlug());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof CategoryAdapterItem
                    && category.equals(((CategoryAdapterItem) item).category);
        }

        public void onCategorySelected() {
            categorySelectedObserver.onNext(category.getSlug());
        }
    }
}
