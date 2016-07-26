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
import com.shoutit.app.android.api.model.FacebookPage;
import com.shoutit.app.android.api.model.LinkFacebookPageRequest;
import com.shoutit.app.android.api.model.LinkFacebookRequest;
import com.shoutit.app.android.api.model.LinkGplusRequest;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.facebook.FacebookHelper;
import com.shoutit.app.android.facebook.FacebookPages;
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
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> linkFacebookObservable;
    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> unlinkFacebookObservable;
    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> linkGoogleObservable;
    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> unlinkGoogleObservable;
    @Nonnull
    private Observable<ResponseOrError<ApiMessageResponse>> unlinkFacebookPageObservable;
    @Nonnull
    private Observable<String> facebookLinkInfoObservable;
    @Nonnull
    private Observable<String> googleLinkInfoObservable;
    @Nonnull
    private final Observable<String> facebookPageLinkInfoObservable;
    @Nonnull
    private Observable<Throwable> errorObservable;
    @Nonnull
    private Observable<Object> pagesListEmptyObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<FacebookPages> pagesListSuccessObservable;
    @Nonnull
    private final Observable<ResponseOrError<ApiMessageResponse>> linkFacebookPageObservable;

    private PublishSubject<String> linkFacebookSubject = PublishSubject.create();
    private PublishSubject<Object> unlinkFacebookSubject = PublishSubject.create();
    private PublishSubject<Object> clickFacebookSubject = PublishSubject.create();
    private PublishSubject<Object> clickFacebookPageSubject = PublishSubject.create();
    private PublishSubject<Object> askForFbTokenSubject = PublishSubject.create();
    private PublishSubject<Object> askForPagesPermissions = PublishSubject.create();
    private PublishSubject<Object> clickGoogleSubject = PublishSubject.create();
    private PublishSubject<String> linkGoogleSubject = PublishSubject.create();
    private PublishSubject<Object> unlinkGoogleSubject = PublishSubject.create();
    private PublishSubject<Object> unlinkFacebookPageSubject = PublishSubject.create();
    private PublishSubject<FacebookPages.FacebookPage> linkFacebookPageSubject = PublishSubject.create();
    private PublishSubject<String> fetchPagesListSubject = PublishSubject.create();

    private Listener listener;

    @Inject
    public LinkedAccountsPresenter(@Nonnull final ApiService apiService,
                                   @Nonnull @UiScheduler final Scheduler uiScheduler,
                                   @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                   @Nonnull @ForActivity Resources resources,
                                   @Nonnull final UserPreferences userPreferences,
                                   @Nonnull FacebookHelper facebookHelper) {

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

        facebookPageLinkInfoObservable = profileObservable
                .map(pageOrUser -> {
                    if (pageOrUser.getLinkedAccounts() != null) {
                        if (pageOrUser.getLinkedAccounts().getFacebookPage() != null) {
                            return pageOrUser.getLinkedAccounts().getFacebookPage().getName();
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

        subscriptions.add(clickFacebookSubject
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

        subscriptions.add(clickGoogleSubject
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

        /** Facebook Pages **/
        subscriptions.add(clickFacebookPageSubject
                .map(o -> {
                    final BaseProfile profile = userPreferences.getUserOrPage();
                    if (profile.getLinkedAccounts().getFacebookPage() != null) {
                        listener.unLinkFacebookPageDialog();
                    } else {
                        askForPagesPermissions.onNext(null);
                    }
                    return null;
                }).subscribe());

        unlinkFacebookPageObservable = unlinkFacebookPageSubject
                .switchMap(o -> {
                    final FacebookPage facebookPage = userPreferences.getUserOrPage().getLinkedAccounts().getFacebookPage();
                    return apiService.unlinkFacebookPage(myUsername, new LinkFacebookPageRequest(facebookPage.getFacebookId()))
                            .subscribeOn(networkScheduler)
                            .observeOn(uiScheduler)
                            .compose(ResponseOrError.toResponseOrErrorObservable());
                }).compose(ObservableExtensions.behaviorRefCount());

        final Observable<ResponseOrError<FacebookPages>> pagesObservable = fetchPagesListSubject
                .switchMap(o -> facebookHelper.getPagesListObservable()
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable()))
                .compose(ObservableExtensions.behaviorRefCount());

        pagesListEmptyObservable = pagesObservable
                .compose(ResponseOrError.onlySuccess())
                .filter(facebookPages -> facebookPages.getData().isEmpty())
                .map(Functions1.toObject());

        pagesListSuccessObservable = pagesObservable
                .compose(ResponseOrError.onlySuccess())
                .filter(facebookPages -> !facebookPages.getData().isEmpty());

        linkFacebookPageObservable = linkFacebookPageSubject
                .switchMap(facebookPage -> apiService.linkFacebookPage(myUsername, new LinkFacebookPageRequest(facebookPage.getId()))
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable())).compose(ObservableExtensions.behaviorRefCount());

        /****/

        progressObservable = Observable.merge(
                Observable.merge(
                        askForPagesPermissions,
                        fetchPagesListSubject,
                        linkFacebookSubject,
                        unlinkFacebookSubject,
                        linkGoogleSubject,
                        unlinkGoogleSubject,
                        linkFacebookPageSubject,
                        unlinkFacebookPageSubject)
                        .map(Functions1.returnTrue()),
                Observable.merge(
                        pagesObservable,
                        linkFacebookObservable,
                        unlinkFacebookObservable,
                        linkGoogleObservable,
                        unlinkGoogleObservable,
                        unlinkFacebookPageObservable,
                        linkFacebookPageObservable)
                        .map(Functions1.returnFalse()));

        errorObservable = Observable.merge(
                linkFacebookObservable.compose(ResponseOrError.onlyError()),
                linkGoogleObservable.compose(ResponseOrError.onlyError()),
                unlinkFacebookObservable.compose(ResponseOrError.onlyError()),
                unlinkGoogleObservable.compose(ResponseOrError.onlyError()),
                unlinkFacebookPageObservable.compose(ResponseOrError.onlyError()),
                linkFacebookPageObservable.compose(ResponseOrError.onlyError()),
                pagesObservable.compose(ResponseOrError.onlyError()))
                .filter(Functions1.isNotNull());

        linkgGoogleFailedObservable = linkGoogleObservable
                .compose(ResponseOrError.onlyError());
    }

    public void register(@Nonnull final Listener listener) {
        this.listener = listener;
    }

    public void unsubscribe() {
        subscriptions.unsubscribe();
    }

    @Nonnull
    public Observable<FacebookPages> getPagesListSuccessObservable() {
        return pagesListSuccessObservable;
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
    public PublishSubject<Object> unlinkFacebookPageSubject() {
        return unlinkFacebookPageSubject;
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
    Observable<Object> askForFbTokenObservable() {
        return askForFbTokenSubject;
    }

    @Nonnull
    Observable<Object> askForPagesPermissionsObservable() {
        return askForPagesPermissions;
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
    public Observer<Object> clickFacebookPageSubject() {
        return RxMoreObservers.ignoreCompleted(clickFacebookPageSubject);
    }

    @Nonnull
    public Observable<String> apiMessageObservable() {
        return Observable.merge(
                unlinkGoogleObservable,
                linkGoogleObservable,
                unlinkFacebookObservable,
                linkFacebookObservable,
                linkFacebookPageObservable,
                unlinkFacebookPageObservable)
                .compose(ResponseOrError.onlySuccess())
                .map(ApiMessageResponse::getSuccess);
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<Object> getPagesListEmptyObservable() {
        return pagesListEmptyObservable;
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
    public Observable<String> getFacebookPageLinkInfoObservable() {
        return facebookPageLinkInfoObservable;
    }

    @Nonnull
    public Observable<Throwable> errorObservable() {
        return errorObservable
                .filter(Functions1.isNotNull());
    }

    public void linkFacebookPageSubject(FacebookPages.FacebookPage facebookPage) {
        linkFacebookPageSubject.onNext(facebookPage);
    }

    public void showPagesList() {
        fetchPagesListSubject.onNext(null);
    }

    public interface Listener {

        void triggerSignInGoogle();

        void unlinkFacebookDialog();

        void unlinkGoogleDialog();

        void unLinkFacebookPageDialog();
    }
}
