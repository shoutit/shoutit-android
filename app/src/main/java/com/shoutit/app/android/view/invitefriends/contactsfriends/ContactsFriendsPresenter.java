package com.shoutit.app.android.view.invitefriends.contactsfriends;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.UploadContactsRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.profileslist.ProfileListAdapterItem;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ContactsFriendsPresenter implements ProfilesListPresenter {

    @Nonnull
    private final ProfilesDao dao;
    @Nonnull
    private final ListeningHalfPresenter listeningHalfPresenter;

    private final PublishSubject<Object> fetchLocalContactSubject = PublishSubject.create();
    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final PublishSubject<BaseProfile> profileListenedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> adapterItems;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;

    @Inject
    public ContactsFriendsPresenter(@Nonnull PhoneContactsHelper phoneContactsHelper,
                                    @Nonnull ApiService apiService,
                                    @Nonnull @NetworkScheduler Scheduler networkScheduler,
                                    @Nonnull @UiScheduler Scheduler uiScheduler,
                                    @ForActivity Resources resources,
                                    @Nonnull ProfilesDao dao,
                                    @Nonnull ListeningHalfPresenter listeningHalfPresenter) {

        this.dao = dao;
        this.listeningHalfPresenter = listeningHalfPresenter;

        final Observable<ResponseOrError<ResponseBody>> uploadContactsRequest =
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

        final Observable<ResponseBody> successContactsUpload = uploadContactsRequest
                .compose(ResponseOrError.onlySuccess());

        final Observable<ResponseOrError<ProfilesListResponse>> fetchContactsRequest =
                Observable.merge(Observable.just(null), successContactsUpload)
                        .switchMap(successfullyUploaded -> dao.getContactsDao(User.ME)
                                .getProfilesObservable()
                                .observeOn(uiScheduler))
                        .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ProfilesListResponse> successFetchContactsRequest = fetchContactsRequest
                .compose(ResponseOrError.onlySuccess());

        adapterItems = successFetchContactsRequest
                .map(new Func1<ProfilesListResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ProfilesListResponse profilesListResponse) {
                        if (profilesListResponse.getResults().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(
                                    new NoDataTextAdapterItem(resources.getString(R.string.facebook_friends_no_friends)));
                        } else {
                            return ImmutableList.copyOf(Lists.transform(profilesListResponse.getResults(), new Function<BaseProfile, BaseAdapterItem>() {
                                @Nullable
                                @Override
                                public BaseAdapterItem apply(BaseProfile profile) {
                                    return new ProfileListAdapterItem(profile, openProfileSubject, profileListenedSubject);
                                }
                            }));
                        }
                    }
                });

        listeningHalfPresenter
                .listeningObservable(successFetchContactsRequest)
                .subscribe(dao.getContactsDao(User.ME).updatedProfileLocallyObserver());

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(uploadContactsRequest),
                        ResponseOrError.transform(fetchContactsRequest)))
                .filter(Functions1.isNotNull())
                .mergeWith(listeningHalfPresenter.getErrorSubject());

        progressObservable = Observable.merge(
                successContactsUpload.map(Functions1.returnTrue()),
                successFetchContactsRequest.map(Functions1.returnFalse()),
                errorObservable.map(Functions1.returnFalse()))
                .startWith(true);
    }

    @Nonnull
    @Override
    public Observable<String> getListenSuccessObservable() {
        return listeningHalfPresenter.getListenSuccess();
    }

    @Nonnull
    @Override
    public Observable<String> getUnListenSuccessObservable() {
        return listeningHalfPresenter.getUnListenSuccess();
    }

    @Override
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Override
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Override
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItems;
    }

    @Override
    public Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    @Override
    public void refreshData() {
        dao.getContactsDao(User.ME)
                .getRefreshSubject()
                .onNext(null);
    }

    @Override
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(dao.getContactsDao(User.ME)
                .getLoadMoreShoutsObserver());
    }

    public void fetchContacts() {
        fetchLocalContactSubject.onNext(null);
    }
}
