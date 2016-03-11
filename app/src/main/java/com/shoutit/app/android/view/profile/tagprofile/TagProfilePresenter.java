package com.shoutit.app.android.view.profile.tagprofile;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.TagsDao;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class TagProfilePresenter {

    private final PublishSubject<TagDetail> onListenActionClickedSubject = PublishSubject.create();
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();

    public TagProfilePresenter(TagsDao tagsDao, ShoutsDao shoutsDao, @Nonnull String tagName,
                               @UiScheduler final Scheduler uiScheduler, @NetworkScheduler final Scheduler networkScheduler,
                               final ApiService apiService) {

        final Observable<ResponseOrError<TagDetail>> tagRequestObservable = tagsDao.getTagObservable(tagName)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<TagDetail>>behaviorRefCount());

        final Observable<ResponseOrError<TagDetail>> updatedTagWithListeningToProfile = onListenActionClickedSubject
                .throttleFirst(1, TimeUnit.SECONDS)
                .switchMap(new Func1<TagDetail, Observable<ResponseOrError<TagDetail>>>() {
                    @Override
                    public Observable<ResponseOrError<TagDetail>> call(final TagDetail tagDetail) {
                        final Observable<ResponseOrError<ResponseBody>> request;
                        if (tagDetail.isListening()) {
                            request = apiService.unlistenTag(tagDetail.getName())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        } else {
                            request = apiService.listenTag(tagDetail.getName())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        }

                        return request.map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<User>>() {
                            @Override
                            public ResponseOrError<User> call(ResponseOrError<ResponseBody> response) {
                                if (response.isData()) {
                                    return ResponseOrError.fromData(tagDetail.toLisenedTag());
                                } else {
                                    errorSubject.onNext(new Throwable());
                                    // On error return current user in order to select/deselect already deselected/selected 'listenProfile' icon
                                    return ResponseOrError.fromData(tagDetail.toListenerTag());
                                }
                            }
                        });
                    }
                });

        userProfilePresenter.getUserUpdatesObservable()
                .subscribe(profilesDao.getProfileDao(userName).updatedProfileLocallyObserver());

        final Observable<User> userSuccessObservable = userRequestObservable.compose(ResponseOrError.<User>onlySuccess())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (User.ME.equals(userName)) {
                            userPreferences.saveUserAsJson(user);
                        }
                    }
                });
    }
}
