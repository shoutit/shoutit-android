package com.shoutit.app.android.view.invitefriends.contactsfriends;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.api.model.UploadContactsRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class ContactsFriendsPresenter extends BaseProfileListPresenter {

    private final PublishSubject<Object> fetchLocalContactSubject = PublishSubject.create();

    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<BaseProfileListDao> daoObservable;
    private final Observable<ApiMessageResponse> successFetchContacts;

    public ContactsFriendsPresenter(@Nonnull PhoneContactsHelper phoneContactsHelper,
                                    @Nonnull ApiService apiService,
                                    @Nonnull @NetworkScheduler Scheduler networkScheduler,
                                    @Nonnull @UiScheduler Scheduler uiScheduler,
                                    @Nonnull ProfilesDao dao,
                                    @Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                    @Nonnull UserPreferences userPreferences,
                                    @Nonnull String placeholderText) {
        super(listeningHalfPresenter, uiScheduler, placeholderText, userPreferences);

        daoObservable = Observable.just(dao.getContactsDao(User.ME));

        final Observable<ResponseOrError<ApiMessageResponse>> uploadContactsRequest =
                fetchLocalContactSubject
                        .switchMap(initFetch -> Observable.just(phoneContactsHelper.getAllPhoneContacts()))
                        .subscribeOn(networkScheduler)
                        .filter(MoreFunctions1.listNotEmpty())
                        .map(UploadContactsRequest::new)
                        .switchMap(uploadContactsRequestBody -> apiService.uploadContacts(User.ME, uploadContactsRequestBody)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.toResponseOrErrorObservable()))
                        .compose(ObservableExtensions.behaviorRefCount());

        successFetchContacts = uploadContactsRequest.compose(ResponseOrError.onlySuccess());

        errorObservable = uploadContactsRequest.compose(ResponseOrError.onlyError());

        progressObservable = uploadContactsRequest.map(Functions1.returnFalse())
                .startWith(true);

        init();
    }

    public Observable<ApiMessageResponse> getSuccessFetchContacts() {
        return successFetchContacts;
    }

    @Nonnull
    @Override
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }

    @Nonnull
    @Override
    public Observable<Boolean> getProgressObservable() {
        return progressObservable.mergeWith(super.getProgressObservable());
    }

    @Nonnull
    @Override
    public Observable<Throwable> getErrorObservable() {
        return super.getErrorObservable().mergeWith(errorObservable);
    }

    public void fetchContacts() {
        fetchLocalContactSubject.onNext(null);
    }
}
