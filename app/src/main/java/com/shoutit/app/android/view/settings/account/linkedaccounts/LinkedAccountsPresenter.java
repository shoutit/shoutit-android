package com.shoutit.app.android.view.settings.account.linkedaccounts;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.LinkFacebookRequest;
import com.shoutit.app.android.api.model.LinkGplusRequest;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LinkedAccountsPresenter {

    private final static String ACCOUNT_FACEBOOK = "facebook";
    private final static String ACCOUNT_GOOGLE = "gplus";

    private final Observable<Throwable> linkgGoogleFailedObservable;
    @Nonnull
    private CompositeSubscription subscription = new CompositeSubscription();

    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> linkFacebookObservable;
    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> unlinkFacebookObservable;
    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> linkGoogleObservable;
    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> unlinkGoogleObservable;
    @Nonnull
    private Observable<String> facebookLinkInfoObservable;
    @Nonnull
    private Observable<String> googleLinkInfoObservable;
    @Nonnull
    private Observable<Throwable> errorObservable;
    @Nonnull
    private PublishSubject<String> linkFacebookSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Object> unlinkFacebookSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Object> clickFacebookSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Object> askForFbTokenSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Object> clickGoogleSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<String> linkGoogleSubject = PublishSubject.create();
    @Nonnull
    private PublishSubject<Object> unlinkGoogleSubject = PublishSubject.create();

    private Listener listener;

    @Inject
    public LinkedAccountsPresenter(@Nonnull final ApiService apiService,
                                   @Nonnull @UiScheduler final Scheduler uiScheduler,
                                   @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                   @Nonnull @ForActivity Resources resources,
                                   @Nonnull final UserPreferences userPreferences) {

        //noinspection ConstantConditions
        final String myUsername = userPreferences.getUserOrPage().getUsername();

        final Observable<BaseProfile> profileObservable = userPreferences.getPageOrUserObservable()
                .compose(ObservableExtensions.behaviorRefCount());

        facebookLinkInfoObservable = profileObservable
                .map(pageOrUser -> {
                    if (pageOrUser.getLinkedAccounts() != null) {
                        if (pageOrUser.getLinkedAccounts().getFacebook() != null) {
                            return resources.getString(R.string.linked_accounts_linked);
                        } else {
                            return resources.getString(R.string.linked_accounts_not_linked);
                        }
                    } else return null;
                });

        googleLinkInfoObservable = profileObservable
                .map(pageOrUser -> {
                    if (pageOrUser.getLinkedAccounts() != null) {
                        if (pageOrUser.getLinkedAccounts().getGplus() != null) {
                            return resources.getString(R.string.linked_accounts_linked);
                        } else {
                            return resources.getString(R.string.linked_accounts_not_linked);
                        }
                    } else return null;
                });
        /**
         * Facebook
         */
        linkFacebookObservable = linkFacebookSubject
                .map(new Func1<String, LinkFacebookRequest>() {
                    @Override
                    public LinkFacebookRequest call(final String token) {
                        return new LinkFacebookRequest(ACCOUNT_FACEBOOK, token);
                    }
                })
                .filter(Functions1.isNotNull())
                .switchMap(request -> apiService.linkFacebook(myUsername, request)
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable()))
                .compose(ObservableExtensions.behaviorRefCount());

        unlinkFacebookObservable = unlinkFacebookSubject
                .switchMap(new Func1<Object, Observable<ResponseOrError<ApiMessageResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ApiMessageResponse>> call(final Object o) {
                        return apiService.unlinkFacebook(myUsername, new LinkFacebookRequest(ACCOUNT_FACEBOOK))
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.toResponseOrErrorObservable());
                    }
                }).compose(ObservableExtensions.behaviorRefCount());

        subscription.add(clickFacebookSubject
                .map(o -> {
                    final BaseProfile userOrPage = userPreferences.getUserOrPage();
                    if (userOrPage.getLinkedAccounts() != null) {
                        if (userOrPage.getLinkedAccounts().getFacebook() != null) {
                            listener.unlinkFacebookDialog();
                        } else {
                            askForFbTokenSubject.onNext(new Object());
                        }
                    }
                    return null;
                }).subscribe());
        /**
         * Google
         */
        linkGoogleObservable = linkGoogleSubject
                .switchMap(token -> apiService.linkGoogle(myUsername, new LinkGplusRequest(ACCOUNT_GOOGLE, token))
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable()))
                .compose(ObservableExtensions.behaviorRefCount());


        unlinkGoogleObservable = unlinkGoogleSubject
                .switchMap(new Func1<Object, Observable<ResponseOrError<ApiMessageResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ApiMessageResponse>> call(final Object o) {
                        return apiService.unlinkGoogle(myUsername, new LinkGplusRequest(ACCOUNT_GOOGLE))
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.toResponseOrErrorObservable());
                    }
                }).compose(ObservableExtensions.behaviorRefCount());

        subscription.add(clickGoogleSubject
                .map(o -> {
                    final BaseProfile profile = userPreferences.getUserOrPage();
                    if (profile.getLinkedAccounts() != null) {
                        if (profile.getLinkedAccounts().getGplus() != null) {
                            listener.unlinkGoogleDialog();
                        } else {
                            listener.triggerSignInGoogle();
                        }
                    }
                    return null;
                }).subscribe());

        errorObservable = Observable.merge(
                linkFacebookObservable.compose(ResponseOrError.onlyError()),
                linkGoogleObservable.compose(ResponseOrError.onlyError()),
                unlinkFacebookObservable.compose(ResponseOrError.onlyError()),
                unlinkGoogleObservable.compose(ResponseOrError.onlyError()));

        linkgGoogleFailedObservable = linkGoogleObservable
                .compose(ResponseOrError.onlyError());
    }

    public void register(@Nonnull final Listener listener) {
        this.listener = listener;
    }

    public void unsubscribe() {
        subscription.unsubscribe();
    }

    public Observable<Throwable> getLinkgGoogleFailedObservable() {
        return linkgGoogleFailedObservable;
    }

    @Nonnull
    public PublishSubject<Object> unlinkFacebookSubject() {
        return unlinkFacebookSubject;
    }

    @Nonnull
    public PublishSubject<Object> unlinkGoogleSubject() {
        return unlinkGoogleSubject;
    }

    @Nonnull
    Observer<String> linkFacebookSubject() {
        return linkFacebookSubject;
    }

    @Nonnull
    Observer<String> linkGoogleSubject() {
        return linkGoogleSubject;
    }

    @Nonnull
    PublishSubject<Object> askForFbTokenObservable() {
        return askForFbTokenSubject;
    }

    @Nonnull
    public Observer<Object> clickGoogleSubject() {
        return RxMoreObservers.ignoreCompleted(clickGoogleSubject);
    }

    @Nonnull
    Observer<Object> clickFacebookSubject() {
        return RxMoreObservers.ignoreCompleted(clickFacebookSubject);
    }

    @Nonnull
    public Observable<String> unlinkGoogleObservable() {
        return unlinkGoogleObservable
                .compose(ResponseOrError.onlySuccess())
                .map(ApiMessageResponse::getSuccess);
    }

    @Nonnull
    public Observable<String> linkGoogleObservable() {
        return linkGoogleObservable
                .compose(ResponseOrError.onlySuccess())
                .map(ApiMessageResponse::getSuccess);
    }

    @Nonnull
    public Observable<String> linkFacebookObservable() {
        return linkFacebookObservable
                .compose(ResponseOrError.onlySuccess())
                .map(ApiMessageResponse::getSuccess);
    }

    @Nonnull
    public Observable<String> unlinkFacebookObservable() {
        return unlinkFacebookObservable
                .compose(ResponseOrError.onlySuccess())
                .map(ApiMessageResponse::getSuccess);
    }

    @Nonnull
    public Observable<String> facebookLinkedInfoObservable() {
        return facebookLinkInfoObservable;
    }

    @Nonnull
    public Observable<String> googleLinkedInfoObservable() {
        return googleLinkInfoObservable;
    }

    @Nonnull
    public Observable<Throwable> errorObservable() {
        return errorObservable
                .filter(Functions1.isNotNull());
    }

    public interface Listener {

        void triggerSignInGoogle();

        void unlinkFacebookDialog();

        void unlinkGoogleDialog();
    }
}
