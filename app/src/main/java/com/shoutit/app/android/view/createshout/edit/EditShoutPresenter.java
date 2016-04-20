package com.shoutit.app.android.view.createshout.edit;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.EditShoutRequest;
import com.shoutit.app.android.api.model.EditShoutRequestWithPrice;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.ResourcesHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func3;
import rx.subscriptions.CompositeSubscription;

public class EditShoutPresenter {

    private String shoutType;

    public static class RequestData {

        @NonNull
        private final String mTitle;
        @NonNull
        private final String mDescription;
        @NonNull
        private final String mBudget;
        @NonNull
        private final String mCurrencyId;
        @Nullable
        private final String mCategoryId;
        @Nullable
        private final List<Pair<String, String>> mOptionsIdValue;
        @NonNull
        private final List<String> mImages;
        @NonNull
        private final List<Video> mVideos;
        @Nonnull
        private final String mMobile;

        public RequestData(@NonNull String title,
                           @NonNull String description,
                           @NonNull String budget,
                           @NonNull String currencyId,
                           @Nullable String categoryId,
                           @Nullable List<Pair<String, String>> optionsIdValue,
                           @NonNull List<String> images,
                           @NonNull List<Video> videos,
                           @Nullable String mobile) {
            mTitle = title;
            mDescription = description;
            mBudget = budget;
            mCurrencyId = currencyId;
            mCategoryId = categoryId;
            mOptionsIdValue = optionsIdValue;
            mImages = images;
            mVideos = videos;
            mMobile = mobile;
        }
    }

    private static class ResponseData {

        private final ShoutResponse mShoutResponse;
        private final List<Category> mCategories;
        private final List<Currency> mCurrencies;

        public ResponseData(@Nullable ShoutResponse shoutResponse,
                            @Nullable List<Category> categories,
                            @Nullable List<Currency> currencies) {
            mShoutResponse = shoutResponse;
            mCategories = categories;
            mCurrencies = currencies;
        }
    }

    private final Context mContext;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final String mShoutId;
    @NonNull
    private final ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter;
    private List<Category> mCategories;
    private Listener mListener;
    private UserLocation mUserLocation;
    private final CompositeSubscription pendingSubscriptions = new CompositeSubscription();

    @Inject
    public EditShoutPresenter(@ForActivity Context context,
                              ApiService apiService,
                              @NetworkScheduler Scheduler networkScheduler,
                              @UiScheduler Scheduler uiScheduler,
                              @NonNull String shoutId,
                              @NonNull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter) {
        mContext = context;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mShoutId = shoutId;
        this.shoutsGlobalRefreshPresenter = shoutsGlobalRefreshPresenter;
    }

    public void registerListener(@NonNull Listener listener) {
        mListener = listener;

        mListener.setCurrenciesEnabled(false);
        mListener.showProgress();

        subscribeEditRequestShout();
    }

    private void subscribeEditRequestShout() {
        pendingSubscriptions.add(Observable.zip(
                mApiService.getShout(mShoutId)
                        .onErrorResumeNext(Observable.<ShoutResponse>just(null)),
                mApiService.categories()
                        .onErrorResumeNext(Observable.<List<Category>>just(null)),
                mApiService.getCurrencies()
                        .onErrorResumeNext(Observable.<List<Currency>>just(null)),
                new Func3<ShoutResponse, List<Category>, List<Currency>, ResponseData>() {
                    @Override
                    public ResponseData call(ShoutResponse shoutResponse, List<Category> categories, List<Currency> currencies) {
                        return new ResponseData(shoutResponse, categories, currencies);
                    }
                })
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<ResponseData>() {
                    @Override
                    public void call(ResponseData responseData) {
                        mListener.hideProgress();

                        if (responseData.mCategories != null) {
                            categorySuccess(responseData.mCategories);
                        } else {
                            mListener.showCategoriesError();
                        }

                        final ShoutResponse shoutResponse = responseData.mShoutResponse;
                        if (responseData.mCurrencies != null) {
                            currencySuccess(responseData.mCurrencies);
                            if (shoutResponse != null) {
                                for (int i = 0; i < responseData.mCurrencies.size(); i++) {
                                    final Currency currency = responseData.mCurrencies.get(i);
                                    if (currency.getCode().equals(shoutResponse.getCurrency())) {
                                        mListener.setSelectedCurrency(i);
                                    }
                                }
                            }
                        } else {
                            mListener.showCurrenciesError();
                        }
                        if (shoutResponse != null) {
                            mListener.setActionbarTitle(mContext.getString(R.string.edit_shout_title, capitalize(shoutResponse.getType())));
                            mListener.setTitle(shoutResponse.getTitle());
                            mListener.setPrice(PriceUtils.formatPriceToEdit(shoutResponse.getPrice()));
                            mListener.setDescription(shoutResponse.getText());
                            mUserLocation = shoutResponse.getLocation();
                            mListener.setLocation(
                                    ResourcesHelper.getResourceIdForName(mUserLocation.getCountry(), mContext),
                                    mUserLocation.getCity());
                            mListener.setMobilePhone(shoutResponse.getMobile());
                            mListener.setMediaData(shoutResponse.getImages(), shoutResponse.getVideos(), shoutResponse.getType().equals(Shout.TYPE_OFFER));
                            changeCategoryWithSelectedOptions(shoutResponse.getCategory().getSlug(), shoutResponse.getFilters());
                        } else {
                            mListener.showBodyError();
                        }
                    }
                }));
    }

    private String capitalize(@NonNull final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private void categorySuccess(@NonNull List<Category> responseData) {
        final List<Pair<String, String>> list = ImmutableList.copyOf(Iterables.transform(responseData,
                new Function<Category, Pair<String, String>>() {
                    @Nullable
                    @Override
                    public Pair<String, String> apply(@Nullable Category input) {
                        assert input != null;
                        return Pair.create(input.getSlug(), input.getName());
                    }
                }));
        mListener.setCategories(list);
        mCategories = responseData;
    }

    private void currencySuccess(@NonNull List<Currency> responseData) {
        final List<PriceUtils.SpinnerCurrency> list = PriceUtils.transformCurrencyToPair(responseData);

        mListener.setCurrencies(list);
    }

    private void setNewUserLocation(@NonNull UserLocation userLocation) {
        mUserLocation = userLocation;
        mListener.setLocation(ResourcesHelper.getResourceIdForName(userLocation.getCountry(), mContext), userLocation.getCity());
    }

    private List<EditShoutRequestWithPrice.FilterValue> getFilters(List<Pair<String, String>> optionsIdValue) {
        return ImmutableList.copyOf(Iterables.transform(optionsIdValue, new Function<Pair<String, String>, EditShoutRequestWithPrice.FilterValue>() {
            @Nullable
            @Override
            public EditShoutRequestWithPrice.FilterValue apply(@Nullable Pair<String, String> input) {
                assert input != null;
                return new EditShoutRequestWithPrice.FilterValue(input.first, new EditShoutRequestWithPrice.FilterValue.Value(input.second));
            }
        }));
    }

    private boolean checkValidity(@NonNull RequestData requestData) {
        final boolean isTitleError;
        isTitleError = requestData.mTitle.length() > 0 && requestData.mTitle.length() < 6;

        mListener.showTitleTooShortError(isTitleError);

        return !isTitleError;
    }

    public void updateLocation(@NonNull UserLocation userLocation) {
        setNewUserLocation(userLocation);
    }

    public void unregister() {
        mListener = null;
        pendingSubscriptions.unsubscribe();
    }

    public void categorySelected(@NonNull final String id) {
        changeCategory(id);
    }

    private void changeCategoryWithSelectedOptions(@NonNull final String id, @Nullable List<CategoryFilter> selectedOptions) {
        if (mCategories != null) {
            final Iterable<Category> filters = Iterables.filter(mCategories, new Predicate<Category>() {
                @Override
                public boolean apply(@Nullable Category input) {
                    assert input != null;
                    return input.getSlug().equals(id);
                }
            });
            final Category category = filters.iterator().next();

            final List<CategoryFilter> categoryFilters = category.getFilters();
            if (selectedOptions != null) {
                for (CategoryFilter selectedOption : selectedOptions) {
                    for (CategoryFilter categoryFilter : categoryFilters) {
                        if (categoryFilter.getSlug().equals(selectedOption.getSlug())) {
                            categoryFilter.setSelectedValue(selectedOption.getSelectedValue());
                        }
                    }
                }
            } else {
                mListener.setOptions(categoryFilters);
            }
            mListener.setCategory(category);
        }
    }

    private void changeCategory(@NonNull final String id) {
        changeCategoryWithSelectedOptions(id, null);
    }

    public void onBudgetChanged(String budget) {
        mListener.setCurrenciesEnabled(!Strings.isNullOrEmpty(budget));
    }

    public void dataReady(RequestData requestData) {
        if (!checkValidity(requestData)) return;

        mListener.showProgress();

        Observable<CreateShoutResponse> editShoutObservable = Strings.isNullOrEmpty(requestData.mBudget) ?
                mApiService.editShout(mShoutId, new EditShoutRequest(
                        requestData.mTitle,
                        requestData.mDescription,
                        new UserLocationSimple(mUserLocation.getLatitude(), mUserLocation.getLongitude()),
                        requestData.mCategoryId,
                        getFilters(requestData.mOptionsIdValue), requestData.mImages, requestData.mVideos, requestData.mMobile)) :
                mApiService.editShout(mShoutId, new EditShoutRequestWithPrice(
                        requestData.mTitle,
                        requestData.mDescription,
                        new UserLocationSimple(mUserLocation.getLatitude(), mUserLocation.getLongitude()),
                        PriceUtils.getPriceInCents(requestData.mBudget),
                        requestData.mCurrencyId,
                        requestData.mCategoryId,
                        getFilters(requestData.mOptionsIdValue), requestData.mImages, requestData.mVideos, requestData.mMobile));

        pendingSubscriptions.add(editShoutObservable
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<CreateShoutResponse>() {
                    @Override
                    public void call(CreateShoutResponse responseBody) {
                        shoutsGlobalRefreshPresenter.refreshShouts();
                        mListener.hideProgress();
                        mListener.finishActivity();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mListener.hideProgress();
                        mListener.showEditShoutApiError(throwable);
                    }
                }));
    }

    public interface Listener {

        void setLocation(@DrawableRes int flag, @NonNull String name);

        void showProgress();

        void hideProgress();

        void showEditShoutApiError(Throwable throwable);

        void showCategoriesError();

        void showCurrenciesError();

        void showBodyError();

        void setCurrencies(@NonNull List<PriceUtils.SpinnerCurrency> list);

        void setCurrenciesEnabled(boolean enabled);

        void showTitleTooShortError(boolean show);

        void finishActivity();

        void setDescription(@Nullable String description);

        void setCategories(@NonNull List<Pair<String, String>> list);

        void setOptions(@NonNull List<CategoryFilter> options);

        void setSelectedCurrency(int currencyPostion);

        void setTitle(@NonNull String title);

        void setPrice(@NonNull String price);

        void setCategory(@Nullable Category category);

        void setActionbarTitle(@NonNull String title);

        void setMediaData(@NonNull List<String> images, @NonNull List<Video> videos, boolean b);

        void setMobilePhone(@Nullable String mobileHint);
    }

}
