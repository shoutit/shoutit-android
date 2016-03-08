package com.shoutit.app.android.view.createshout.edit;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.CreateRequestShoutRequest;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.ShoutResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ResourcesHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.subscriptions.CompositeSubscription;

public class EditShoutPresenter {

    public static class RequestData {

        @NonNull
        private final String mDescription;
        @NonNull
        private final String mBudget;
        @NonNull
        private final String mCurrencyId;

        public RequestData(@NonNull String description, @NonNull String budget, @NonNull String currencyId) {
            mDescription = description;
            mBudget = budget;
            mCurrencyId = currencyId;
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

    private final Observable<UserLocation> mLocationObservable;
    private final Context mContext;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final String mShoutId;
    private List<Category> mCategories;
    private Listener mListener;
    private UserLocation mUserLocation;
    private Subscription locationSubscription;
    private final CompositeSubscription pendingSubscriptions = new CompositeSubscription();

    @Inject
    public EditShoutPresenter(UserPreferences userPreferences,
                              @ForActivity Context context,
                              ApiService apiService,
                              @NetworkScheduler Scheduler networkScheduler,
                              @UiScheduler Scheduler uiScheduler,
                              @Nullable String shoutId) {
        mContext = context;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mShoutId = shoutId;
        mLocationObservable = userPreferences.getLocationObservable()
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());
    }

    public void registerListener(@NonNull Listener listener) {
        mListener = listener;
        locationSubscription = mLocationObservable.subscribe(new Action1<UserLocation>() {
            @Override
            public void call(@NonNull UserLocation userLocation) {
                setNewUserLocation(userLocation);
            }
        });

        mListener.setCurrenciesEnabled(false);
        mListener.showProgress();

        if (mShoutId != null) {
            subscribeEditRequestShout();
        } else {
            subscribeEditCreateShout();
        }
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
                            // TODO category error
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
                            // TODO currency error
                        }

                        if (responseData.mShoutResponse != null) {
                            mListener.setTitle(responseData.mShoutResponse.getTitle());
                            mListener.setPrice(String.valueOf(responseData.mShoutResponse.getPrice()));
                            mListener.setLocation(ResourcesHelper.getResourceIdForName(
                                            responseData.mShoutResponse.getLocation().getCountry(), mContext),
                                    responseData.mShoutResponse.getLocation().getCity());
                        } else {
                            // TODO body error
                        }
                    }
                }));
    }

    private void subscribeEditCreateShout() {
        pendingSubscriptions.add(Observable.zip(
                mApiService.categories()
                        .onErrorResumeNext(Observable.<List<Category>>just(null)),
                mApiService.getCurrencies()
                        .onErrorResumeNext(Observable.<List<Currency>>just(null)),
                new Func2<List<Category>, List<Currency>, BothParams<List<Category>, List<Currency>>>() {
                    @Override
                    public BothParams<List<Category>, List<Currency>> call(List<Category> categories, List<Currency> currencies) {
                        return BothParams.of(categories, currencies);
                    }
                })
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<BothParams<List<Category>, List<Currency>>>() {
                    @Override
                    public void call(BothParams<List<Category>, List<Currency>> responseData) {
                        mListener.hideProgress();

                        if (responseData.param1() != null) {
                            categorySuccess(responseData.param1());
                        } else {
                            // TODO category error
                        }

                        if (responseData.param2() != null) {
                            currencySuccess(responseData.param2());
                        } else {
                            // TODO currency error
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
        pendingSubscriptions.add(mApiService.createShoutRequest(
                new CreateRequestShoutRequest(
                        requestData.mDescription,
                        new UserLocationSimple(mUserLocation.getLatitude(), mUserLocation.getLongitude()),
                        Double.parseDouble(requestData.mBudget), requestData.mCurrencyId))
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
                        mListener.showError();
                    }
                }));
    }

    private boolean checkValidity(@NonNull RequestData requestData) {
        final boolean erroredTitle = requestData.mDescription.length() < 6;
        mListener.showTitleTooShortError(erroredTitle);

        final boolean erroredBudget = Strings.isNullOrEmpty(requestData.mBudget);
        mListener.showEmptyPriceError(erroredBudget);

        return !erroredTitle && !erroredBudget;
    }

    public void updateLocation(@NonNull UserLocation userLocation) {
        locationSubscription.unsubscribe();
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

            final List<Pair<String, List<CategoryFilter.FilterValue>>> options = ImmutableList.copyOf(Iterables.transform(category.getFilters(), new Function<CategoryFilter, Pair<String, List<CategoryFilter.FilterValue>>>() {
                @Nullable
                @Override
                public Pair<String, List<CategoryFilter.FilterValue>> apply(@Nullable CategoryFilter input) {
                    assert input != null;
                    return Pair.create(input.getName(), input.getValues());
                }
            }));

            mListener.setOptions(options);
            mListener.setCategoryImage(category.getIcon());
        }
    }

    public interface Listener {

        RequestData getRequestData();

        void setLocation(@DrawableRes int flag, @NonNull String name);

        void showProgress();

        void hideProgress();

        void showError();

        void setCurrencies(@NonNull List<Pair<String, String>> list);

        void setCurrenciesEnabled(boolean enabled);

        void showTitleTooShortError(boolean show);

        void showEmptyPriceError(boolean show);

        void finishActivity();

        void setDescription(@Nullable String description);

        void setCategories(@NonNull List<Pair<String, String>> list);

        void setOptions(@NonNull List<Pair<String, List<CategoryFilter.FilterValue>>> options);

        void setSelectedCurrency(int currencyPostion);

        void setTitle(@NonNull String title);

        void setPrice(@NonNull String price);

        void setCategoryImage(@Nullable String image);
    }

}
