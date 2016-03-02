package com.shoutit.app.android.view.createshout.request;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreateRequestShoutRequest;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ResourcesHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class CreateRequestPresenter {

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

    private final Observable<UserLocation> mLocationObservable;
    private final Context mContext;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private Listener mListener;
    private UserLocation mUserLocation;
    private Subscription locationSubscription;
    private CompositeSubscription pendingSubscriptions = new CompositeSubscription();

    @Inject
    public CreateRequestPresenter(UserPreferences userPreferences,
                                  @ForActivity Context context,
                                  ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler,
                                  @UiScheduler Scheduler uiScheduler) {
        mContext = context;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
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

        getCurrencies();
    }

    private void getCurrencies() {
        mListener.setCurrenciesEnabled(false);
        mListener.showProgress();
        pendingSubscriptions.add(mApiService.getCurrencies()
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<List<Currency>>() {
                               @Override
                               public void call(@NonNull List<Currency> responseBody) {
                                   final ImmutableList<Pair<String, String>> list = ImmutableList.copyOf(
                                           Iterables.transform(responseBody,
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
                                   mListener.hideProgress();
                                   mListener.removeRetryCurrenciesListener();
                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {
                                   mListener.setCurrenciesEnabled(true);
                                   mListener.hideProgress();
                                   mListener.showCurrenciesError();
                                   mListener.setRetryCurrenciesListener();
                               }
                           }
                ));
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
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
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

    private boolean checkValidity(RequestData requestData) {
        final boolean erroredTitle = requestData.mDescription.length() < 6;
        mListener.showTitleTooShortError(erroredTitle);

        final boolean erroredBudget = Strings.isNullOrEmpty(requestData.mBudget);
        mListener.showEmptyPriceError(erroredBudget);

        return !erroredTitle || !erroredBudget;
    }

    public void updateLocation(@NonNull UserLocation userLocation) {
        locationSubscription.unsubscribe();
        setNewUserLocation(userLocation);
    }

    public void unregister() {
        mListener = null;
        pendingSubscriptions.unsubscribe();
    }

    public void retryCurrencies() {
        getCurrencies();
    }

    public interface Listener {

        RequestData getRequestData();

        void setLocation(@DrawableRes int flag, @NonNull String name);

        void showProgress();

        void hideProgress();

        void showError();

        void setCurrencies(@NonNull List<Pair<String, String>> list);

        void showCurrenciesError();

        void setCurrenciesEnabled(boolean enabled);

        void setRetryCurrenciesListener();

        void removeRetryCurrenciesListener();

        void showTitleTooShortError(boolean show);

        void showEmptyPriceError(boolean show);

        void finishActivity();
    }

}
