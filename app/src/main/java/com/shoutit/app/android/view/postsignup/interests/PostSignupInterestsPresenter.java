package com.shoutit.app.android.view.postsignup.interests;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.HeaderItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.TagsRequest;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.CategoriesDao;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.subjects.PublishSubject;

public class PostSignupInterestsPresenter {

    private final Observable<List<BaseAdapterItem>> mCategoryItems;
    private final Observable<Throwable> mError;
    private final SelectionHelper<String> mStringSelectionHelper;
    private final PublishSubject<Object> clickedSubject = PublishSubject.create();
    private final Observable<Throwable> mErrorCategoriesObservable;
    private final Observable<Object> mSuccessCategoriesObservable;
    private final Observable<Boolean> progressObservable;

    @Inject
    public PostSignupInterestsPresenter(CategoriesDao dao,
                                        final ApiService apiService,
                                        @NetworkScheduler final Scheduler networkScheduler,
                                        @UiScheduler final Scheduler uiScheduler,
                                        SelectionHelper<String> selectionHelper,
                                        @ForActivity Resources resources) {
        mStringSelectionHelper = selectionHelper;

        final Observable<ResponseOrError<List<Category>>> categoriesRequest = dao
                .categoriesObservable()
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<List<Category>> successRequest = categoriesRequest.compose(ResponseOrError.<List<Category>>onlySuccess());

        mError = categoriesRequest.compose(ResponseOrError.<List<Category>>onlyError());

        mCategoryItems = successRequest.map((Func1<List<Category>, List<BaseAdapterItem>>) categories -> {
            final ImmutableList.Builder<BaseAdapterItem> builder = new ImmutableList.Builder<>();
            builder.add(new HeaderItem(resources.getString(R.string.post_signup_interests_subheader)));

            builder.addAll(Iterables.transform(categories, new Function<Category, BaseAdapterItem>() {
                @Nullable
                @Override
                public CategoryItem apply(Category input) {
                    return new CategoryItem(input.getIcon(), input.getName(), input.getSlug());
                }
            }));

            return builder.build();
        });

        final Observable<ResponseOrError<Object>> sendObservable = clickedSubject
                .flatMap(new Func1<Object, Observable<ResponseOrError<Object>>>() {
                    @Override
                    public Observable<ResponseOrError<Object>> call(Object o) {
                        return mStringSelectionHelper
                                .getSelectedItems()
                                .take(1)
                                .flatMap(new Func1<Set<String>, Observable<ResponseOrError<Object>>>() {
                                    @Override
                                    public Observable<ResponseOrError<Object>> call(Set<String> strings) {
                                        final ImmutableList<TagsRequest.TagToListen> tagToListens = ImmutableList.copyOf(Iterables.transform(strings, new Function<String, TagsRequest.TagToListen>() {
                                            @Nullable
                                            @Override
                                            public TagsRequest.TagToListen apply(@Nullable String input) {
                                                return new TagsRequest.TagToListen(input);
                                            }
                                        }));
                                        return apiService.batchListen(new TagsRequest(tagToListens))
                                                .subscribeOn(networkScheduler)
                                                .observeOn(uiScheduler)
                                                .compose(ResponseOrError.toResponseOrErrorObservable());
                                    }
                                });
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<Object>>behaviorRefCount());

        mErrorCategoriesObservable = sendObservable.compose(ResponseOrError.onlyError());
        mSuccessCategoriesObservable = sendObservable.compose(ResponseOrError.onlySuccess());

        progressObservable = Observable.merge(
                categoriesRequest.map(Functions1.returnFalse()),
                sendObservable.map(Functions1.returnFalse()),
                clickedSubject.map(Functions1.returnTrue())
        ).startWith(true);
    }

    @NonNull
    public Observable<List<BaseAdapterItem>> getCategoriesItems() {
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
    public Observable<Object> getSuccessCategoriesObservable() {
        return mSuccessCategoriesObservable;
    }

    @NonNull
    public Observable<Boolean> hasAnySelectedCategoriesObservable() {
        return mStringSelectionHelper.getSelectedItems()
                .map(selectedItems -> !selectedItems.isEmpty());
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @NonNull
    public Observable<Throwable> getPostCategoriesError() {
        return mErrorCategoriesObservable;
    }

    public class CategoryItem implements BaseAdapterItem {

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

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final CategoryItem that = (CategoryItem) o;

            if (imageUrl != null ? !imageUrl.equals(that.imageUrl) : that.imageUrl != null)
                return false;
            if (!name.equals(that.name)) return false;
            return mId.equals(that.mId);

        }

        @Override
        public int hashCode() {
            int result = imageUrl != null ? imageUrl.hashCode() : 0;
            result = 31 * result + name.hashCode();
            result = 31 * result + mId.hashCode();
            return result;
        }

        @Nullable
        public String getImageUrl() {
            return imageUrl;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof CategoryItem && mId.equals(((CategoryItem) item).mId);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return equals(item);
        }
    }


}
