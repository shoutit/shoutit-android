package com.shoutit.app.android.view.postlogininterest;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.functions.BothParams;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.dao.CategoriesDao;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.subjects.PublishSubject;

public class PostLoginPresenter {

    private final Observable<List<CategoryItem>> mCategoryItems;
    private final Observable<Throwable> mError;
    private final SelectionHelper<String> mStringSelectionHelper;
    private final PublishSubject<Object> clickedSubject = PublishSubject.create();
    private final Observable<Object> mCategoriesSentObservable;

    @Inject
    public PostLoginPresenter(CategoriesDao dao, final ApiService apiService) {
        mStringSelectionHelper = new SelectionHelper<>();

        final Observable<ResponseOrError<List<Category>>> listObservableResponseOrError = dao.getListObservableResponseOrError();
        final Observable<List<Category>> success = listObservableResponseOrError.compose(ResponseOrError.<List<Category>>onlySuccess());

        mError = listObservableResponseOrError.compose(ResponseOrError.<List<Category>>onlyError());

        mCategoryItems = success.map(new Func1<List<Category>, List<CategoryItem>>() {
            @Override
            public List<CategoryItem> call(List<Category> categories) {
                return ImmutableList.copyOf(Iterables.transform(categories, new Function<Category, CategoryItem>() {
                    @Nullable
                    @Override
                    public CategoryItem apply(Category input) {
                        return new CategoryItem(input.getMainTag().getImage(), input.getName(), input.getSlug());
                    }
                }));
            }
        });

        mCategoriesSentObservable = clickedSubject
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        return mStringSelectionHelper
                                .getSelectedItems()
                                .flatMap(new Func1<Set<String>, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(Set<String> strings) {
                                        return Observable.from(strings);
                                    }
                                })
                                .flatMap(new Func1<String, Observable<?>>() {
                                    @Override
                                    public Observable<?> call(String s) {
                                        return apiService.postCategoryListen(s);
                                    }
                                });
                    }
                });
    }

    @NonNull
    public Observable<List<CategoryItem>> getCategoriesList() {
        return mCategoryItems;
    }

    @NonNull
    public Observable<Throwable> getErrorObservable() {
        return mError;
    }

    @NonNull
    public Observer<Object> nextClickedObserver() {
        return clickedSubject;
    }

    @NonNull
    public Observable<Object> getCategoriesSentObservable() {
        return mCategoriesSentObservable;
    }

    public class CategoryItem {

        @Nullable
        private final String imageUrl;
        @NonNull
        private final String name;
        @Nonnull
        private final String mId;

        public CategoryItem(@Nullable String imageUrl, @NonNull String name, @Nonnull String id) {
            this.imageUrl = imageUrl;
            this.name = name;
            mId = id;
        }

        @NonNull
        public Observable<Boolean> selection() {
            return mStringSelectionHelper.itemSelectionObservable(mId);
        }

        @NonNull
        public Observer<Boolean> selectionObserver() {
            return Observers.create(new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    mStringSelectionHelper.getToggleObserver().onNext(BothParams.of(mId, aBoolean));
                }
            });
        }
    }
}
