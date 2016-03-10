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
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.EditShoutRequest;
import com.shoutit.app.android.api.model.EditShoutRequestWithPrice;
import com.shoutit.app.android.api.model.ShoutResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.ResourcesHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func3;
import rx.subscriptions.CompositeSubscription;

public class EditShoutPresenter {

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

        public RequestData(@NonNull String title,
                           @NonNull String description,
                           @NonNull String budget,
                           @NonNull String currencyId,
                           @Nullable String categoryId,
                           @Nullable List<Pair<String, String>> optionsIdValue) {
            mTitle = title;
            mDescription = description;
            mBudget = budget;
            mCurrencyId = currencyId;
            mCategoryId = categoryId;
            mOptionsIdValue = optionsIdValue;
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
    private List<Category> mCategories;
    private Listener mListener;
    private UserLocation mUserLocation;
    private final CompositeSubscription pendingSubscriptions = new CompositeSubscription();

    @Inject
    public EditShoutPresenter(@ForActivity Context context,
                              ApiService apiService,
                              @NetworkScheduler Scheduler networkScheduler,
                              @UiScheduler Scheduler uiScheduler,
                              @NonNull String shoutId) {
        mContext = context;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mShoutId = shoutId;
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

                        if (responseData.mCurrencies != null) {
                            currencySuccess(responseData.mCurrencies);
                            if (responseData.mShoutResponse != null) {
                                for (int i = 0; i < responseData.mCurrencies.size(); i++) {
                                    final Currency currency = responseData.mCurrencies.get(i);
                                    if (currency.getCode().equals(responseData.mShoutResponse.getCurrency())) {
                                        mListener.setSelectedCurrency(i);
                                    }
                                }
                            }
                        } else {
                            mListener.showCurrenciesError();
                        }

                        if (responseData.mShoutResponse != null) {
                            mListener.setTitle(responseData.mShoutResponse.getTitle());
                            mListener.setPrice(PriceUtils.formatPrice(responseData.mShoutResponse.getPrice()));
                            mListener.setDescription(responseData.mShoutResponse.getDescription());
                            mUserLocation = responseData.mShoutResponse.getLocation();
                            mListener.setLocation(
                                    ResourcesHelper.getResourceIdForName(mUserLocation.getCountry(), mContext),
                                    mUserLocation.getCity());
                        } else {
                            mListener.showBodyError();
                        }
                    }
                }));
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
        final List<Pair<String, String>> list = ImmutableList.copyOf(
                Iterables.transform(responseData,
                        new Function<Currency, Pair<String, String>>() {
                            @Nullable
                            @Override
                            public Pair<String, String> apply(Currency input) {
                                return Pair.create(input.getCode(),
                                        String.format("%s (%s)", input.getName(), input.getCountry()));
                            }
                        }));

        mListener.setCurrenciesEnabled(true);
        mListener.setCurrencies(list);
    }

    private void setNewUserLocation(@NonNull UserLocation userLocation) {
        mUserLocation = userLocation;
        mListener.setLocation(ResourcesHelper.getResourceIdForName(userLocation.getCountry(), mContext), userLocation.getCity());
    }

    public void confirmClicked() {
        final RequestData requestData = mListener.getRequestData();
        if (!checkValidity(requestData)) return;

        mListener.showProgress();

        final EditShoutRequest request = Strings.isNullOrEmpty(requestData.mBudget) ?
                new EditShoutRequest(
                        requestData.mTitle,
                        requestData.mDescription,
                        new UserLocationSimple(mUserLocation.getLatitude(), mUserLocation.getLongitude()),
                        requestData.mCategoryId,
                        getFilters(requestData.mOptionsIdValue)) :
                new EditShoutRequestWithPrice(
                        requestData.mTitle,
                        requestData.mDescription,
                        new UserLocationSimple(mUserLocation.getLatitude(), mUserLocation.getLongitude()),
                        PriceUtils.getPriceInCents(requestData.mBudget),
                        requestData.mCurrencyId,
                        requestData.mCategoryId,
                        getFilters(requestData.mOptionsIdValue));

        pendingSubscriptions.add(mApiService.editShout(mShoutId, request)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<CreateShoutResponse>() {
                    @Override
                    public void call(CreateShoutResponse responseBody) {
                        mListener.hideProgress();
                        mListener.finishActivity();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mListener.hideProgress();
                        mListener.showPostError();
                    }
                }));
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
        final boolean erroredTitle = requestData.mTitle.length() < 6;
        mListener.showTitleTooShortError(erroredTitle);

        return !erroredTitle;
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

    private void changeCategory(@NonNull final String id) {
        if (mCategories != null) {
            final Iterable<Category> filters = Iterables.filter(mCategories, new Predicate<Category>() {
                @Override
                public boolean apply(@Nullable Category input) {
                    assert input != null;
                    return input.getSlug().equals(id);
                }
            });
            final Category category = filters.iterator().next();


            mListener.setOptions(category.getFilters());
            mListener.setCategoryImage(category.getIcon());
        }
    }

    public interface Listener {

        RequestData getRequestData();

        void setLocation(@DrawableRes int flag, @NonNull String name);

        void showProgress();

        void hideProgress();

        void showPostError();

        void showCategoriesError();

        void showCurrenciesError();

        void showBodyError();

        void setCurrencies(@NonNull List<Pair<String, String>> list);

        void setCurrenciesEnabled(boolean enabled);

        void showTitleTooShortError(boolean show);

        void finishActivity();

        void setDescription(@Nullable String description);

        void setCategories(@NonNull List<Pair<String, String>> list);

        void setOptions(@NonNull List<CategoryFilter> options);

        void setSelectedCurrency(int currencyPostion);

        void setTitle(@NonNull String title);

        void setPrice(@NonNull String price);

        void setCategoryImage(@Nullable String image);
    }

}
