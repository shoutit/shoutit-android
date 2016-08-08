package com.shoutit.app.android.view.createshout.request;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.facebook.CallbackManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.AppPreferences;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreateRequestShoutRequest;
import com.shoutit.app.android.api.model.CreateRequestShoutWithPriceRequest;
import com.shoutit.app.android.api.model.CreateShoutResponse;
import com.shoutit.app.android.api.model.Currency;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.facebook.FacebookHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

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
    @NonNull
    private final ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter;
    private final FacebookHelper facebookHelper;
    private final AppPreferences appPreferences;
    private Listener mListener;
    private UserLocation mUserLocation;
    private Subscription locationSubscription;
    private CompositeSubscription pendingSubscriptions = new CompositeSubscription();

    @Inject
    public CreateRequestPresenter(UserPreferences userPreferences,
                                  @ForActivity Context context,
                                  ApiService apiService,
                                  @NetworkScheduler Scheduler networkScheduler,
                                  @UiScheduler Scheduler uiScheduler,
                                  @NonNull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter,
                                  FacebookHelper facebookHelper,
                                  AppPreferences appPreferences) {
        mContext = context;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        this.shoutsGlobalRefreshPresenter = shoutsGlobalRefreshPresenter;
        this.facebookHelper = facebookHelper;
        this.appPreferences = appPreferences;
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
                                   mListener.setCurrencies(PriceUtils.transformCurrencyToPair(responseBody));
                                   mListener.hideProgress();
                                   mListener.removeRetryCurrenciesListener();
                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {
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

    public void confirmClicked(boolean publishToFacebook) {
        final RequestData requestData = mListener.getRequestData();
        if (!checkValidity(requestData)) return;

        mListener.showProgress();


        final Observable<CreateShoutResponse> observable;
        if (Strings.isNullOrEmpty(requestData.mBudget)) {
            observable = mApiService.createShoutRequest(new CreateRequestShoutRequest(
                    requestData.mDescription,
                    new UserLocationSimple(mUserLocation.getLatitude(), mUserLocation.getLongitude()), publishToFacebook));
        } else {
            observable = mApiService.createShoutRequest(new CreateRequestShoutWithPriceRequest(
                    requestData.mDescription,
                    new UserLocationSimple(mUserLocation.getLatitude(), mUserLocation.getLongitude()),
                    PriceUtils.getPriceInCents(requestData.mBudget), requestData.mCurrencyId, publishToFacebook));
        }

        pendingSubscriptions.add(observable
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<CreateShoutResponse>() {
                    @Override
                    public void call(CreateShoutResponse responseBody) {
                        mListener.hideProgress();
                        mListener.finishActivity(responseBody.getId(), responseBody.getWebUrl(), responseBody.getTitle());
                        appPreferences.increaseCreatedShouts();
                        shoutsGlobalRefreshPresenter.refreshShouts();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mListener.hideProgress();
                        mListener.showApiError(throwable);
                    }
                }));
    }

    public void askForFacebookPermissionIfNeeded(@Nonnull final Activity activity,
                                                 @Nonnull CallbackManager callbackManager) {
        mListener.showProgress();

        pendingSubscriptions.add(
                facebookHelper.askForPermissionIfNeeded(activity,
                        new String[]{FacebookHelper.PERMISSION_PUBLISH_ACTIONS}, callbackManager, true)
                        .observeOn(mUiScheduler)
                        .subscribe(new Action1<ResponseOrError<Boolean>>() {
                            @Override
                            public void call(ResponseOrError<Boolean> responseOrError) {
                                mListener.hideProgress();

                                if (responseOrError.isData()) {
                                    final Boolean isPermissionGranted = responseOrError.data();
                                    if (!isPermissionGranted) {
                                        mListener.uncheckFacebookCheckbox();
                                        mListener.showPermissionNotGranted();
                                    }
                                } else {
                                    mListener.uncheckFacebookCheckbox();
                                    mListener.showApiError(responseOrError.error());
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                mListener.hideProgress();
                                mListener.uncheckFacebookCheckbox();
                                mListener.showApiError(throwable);
                            }
                        })
        );

    }

    private boolean checkValidity(RequestData requestData) {
        final boolean erroredTitle = requestData.mDescription.length() < 6;
        mListener.showTitleTooShortError(erroredTitle);

        return !erroredTitle;
    }

    public void updateLocation(@NonNull UserLocation userLocation) {
        locationSubscription.unsubscribe();
        setNewUserLocation(userLocation);
    }

    public void onBudgetChanged(@NonNull String budget) {
        mListener.setCurrenciesEnabled(!Strings.isNullOrEmpty(budget));
    }

    public void unregister() {
        mListener = null;
        pendingSubscriptions.unsubscribe();
        locationSubscription.unsubscribe();
    }

    public void retryCurrencies() {
        getCurrencies();
    }

    public interface Listener {

        RequestData getRequestData();

        void setLocation(@DrawableRes int flag, @NonNull String name);

        void showProgress();

        void hideProgress();

        void showApiError(Throwable throwable);

        void setCurrencies(@NonNull List<PriceUtils.SpinnerCurrency> list);

        void showCurrenciesError();

        void setCurrenciesEnabled(boolean enabled);

        void setRetryCurrenciesListener();

        void removeRetryCurrenciesListener();

        void showTitleTooShortError(boolean show);

        void finishActivity(String id, String webUrl, String title);

        void uncheckFacebookCheckbox();

        void showPermissionNotGranted();
    }

}
