package com.shoutit.app.android.view.profile.tagprofile;

import android.content.Context;

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
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.model.TagShoutsPointer;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class TagProfilePresenter {
    private static final int SHOUTS_PAGE_SIZE = 4;

    private final PublishSubject<TagDetail> onListenActionClickedSubject = PublishSubject.create();
    private PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();

    private final Observable<String> avatarObservable;
    private final Observable<String> coverUrlObservable;
    private final Observable<String> toolbarTitleObservable;
    private final Observable<String> toolbarSubtitleObservable;

    public TagProfilePresenter(TagsDao tagsDao, final ShoutsDao shoutsDao, @Nonnull final String tagName,
                               @UiScheduler final Scheduler uiScheduler, @NetworkScheduler final Scheduler networkScheduler,
                               final ApiService apiService, @ForActivity final Context context,
                               UserPreferences userPreferences) {

        final Observable<ResponseOrError<TagDetail>> tagRequestObservable = tagsDao.getTagObservable(tagName)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<TagDetail>>behaviorRefCount());

        onListenActionClickedSubject
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

                        return request.map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<TagDetail>>() {
                            @Override
                            public ResponseOrError<TagDetail> call(ResponseOrError<ResponseBody> response) {
                                if (response.isData()) {
                                    return ResponseOrError.fromData(tagDetail.toListenedTag());
                                } else {
                                    errorSubject.onNext(new Throwable());
                                    // On error return current user in order to select/deselect already deselected/selected 'listenProfile' icon
                                    return ResponseOrError.fromData(tagDetail.toListenedTag());
                                }
                            }
                        });
                    }
                })
                .subscribe(tagsDao.getUpdatedTagObserver(tagName));

        final Observable<TagDetail> successTagRequestObservable = tagRequestObservable
                .compose(ResponseOrError.<TagDetail>onlySuccess());

        /** Header Data **/
        avatarObservable = successTagRequestObservable
                .map(new Func1<TagDetail, String>() {
                    @Override
                    public String call(TagDetail user) {
                        return user.getImage();
                    }
                });

        coverUrlObservable = successTagRequestObservable
                .map(new Func1<TagDetail, String>() {
                    @Override
                    public String call(TagDetail user) {
                        return user.getCover();
                    }
                });

        toolbarTitleObservable = successTagRequestObservable
                .map(new Func1<TagDetail, String>() {
                    @Override
                    public String call(TagDetail user) {
                        return user.getName();
                    }
                });

        toolbarSubtitleObservable = successTagRequestObservable
                .map(new Func1<TagDetail, String>() {
                    @Override
                    public String call(TagDetail user) {
                        return context.getResources().getString(R.string.profile_subtitle, user.getListenersCount());
                    }
                });

        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> shoutsObservable = userPreferences
                .getLocationObservable().first()
                .switchMap(new Func1<UserLocation, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(UserLocation userLocation) {
                        return shoutsDao.getTagsShoutsObservable(new TagShoutsPointer(SHOUTS_PAGE_SIZE, tagName, userLocation.getLocationPointer()))
                                .observeOn(uiScheduler);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> shoutsSuccessResponse = shoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<Shout>>() {
                    @Override
                    public List<Shout> call(ShoutsResponse response) {
                        return response.getShouts();
                    }
                })
                .filter(Functions1.isNotNull())
                .map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items = Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(@Nullable Shout shout) {
                                return new ShoutAdapterItem(shout, context, shoutSelectedSubject);
                            }
                        });

                        return ImmutableList.copyOf(items);
                    }
                });

    }
}
