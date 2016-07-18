package com.shoutit.app.android.view.listenings;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.TagsListResponse;
import com.shoutit.app.android.utils.ProfilesHelper;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class ListenTagsHalfPresenter {

    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();
    private final PublishSubject<TagDetail> listenTagSubject = PublishSubject.create();

    @Nonnull
    protected final ApiService apiService;
    private final Scheduler networkScheduler;
    private final Scheduler uiScheduler;

    @Inject
    public ListenTagsHalfPresenter(@Nonnull ApiService apiService,
                                   @NetworkScheduler Scheduler networkScheduler,
                                   @UiScheduler Scheduler uiScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;
    }

    @Nonnull
    public Observable<ResponseOrError<TagsListResponse>> listeningObservable(Observable<TagsListResponse> successRequest) {
        return listenTagSubject
                .withLatestFrom(successRequest, ProfilesHelper.TagToListenWithLastResponse::new)
                .switchMap(tagToListenWithLastResponse -> {

                    final boolean isListeningToTag = tagToListenWithLastResponse.getTagDetail().isListening();

                    Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                    if (isListeningToTag) {
                        listenRequestObservable = apiService.unlistenTag(tagToListenWithLastResponse.getTagDetail().getSlug())
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .doOnNext(responseBody -> unListenSuccess.onNext(tagToListenWithLastResponse.getTagDetail().getName()))
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    } else {
                        listenRequestObservable = apiService.listenTag(tagToListenWithLastResponse.getTagDetail().getSlug())
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .doOnNext(responseBody -> listenSuccess.onNext(tagToListenWithLastResponse.getTagDetail().getName()))
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    }

                    return listenRequestObservable
                            .map(response -> {
                                if (response.isData()) {
                                    return ResponseOrError.fromData(ProfilesHelper.updateLastResponseWithListenedTag(tagToListenWithLastResponse));
                                } else {
                                    errorSubject.onNext(new Throwable());
                                    // On error return current user in order to select/deselect already deselected/selected item
                                    return ResponseOrError.fromData(tagToListenWithLastResponse.getResponse());
                                }
                            });
                })
                .observeOn(uiScheduler);
    }

    @Nonnull
    public PublishSubject<Throwable> getErrorSubject() {
        return errorSubject;
    }

    @Nonnull
    public PublishSubject<String> getListenSuccess() {
        return listenSuccess;
    }

    @Nonnull
    public PublishSubject<String> getUnListenSuccess() {
        return unListenSuccess;
    }

    @Nonnull
    public PublishSubject<TagDetail> getListenTagSubject() {
        return listenTagSubject;
    }
}
