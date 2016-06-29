package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.SuggestionsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.SuggestionsDao;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class PostSignupSecondPresenter {
    private static final int PAGE_SIZE = 6;

    private final Observable<List<BaseAdapterItem>> suggestedPagesObservable;
    private final Observable<List<BaseAdapterItem>> suggestedUsersObservable;
    private final Observable<Throwable> errorObservable;

    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<BaseProfile> itemListenedSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();
    private final Observable<Boolean> progressObservable;

    private Resources resources;

    public PostSignupSecondPresenter(@Nonnull SuggestionsDao suggestionsDao,
                                     @Nonnull final ApiService apiService,
                                     @Nonnull UserPreferences userPreferences,
                                     @NetworkScheduler final Scheduler networkScheduler,
                                     @UiScheduler final Scheduler uiScheduler,
                                     @Nonnull @ForActivity Resources resources) {

        this.resources = resources;

        final SuggestionsDao.SuggestionsPointer suggestionsPointer =
                new SuggestionsDao.SuggestionsPointer(PAGE_SIZE, userPreferences.getLocation());

        final Observable<ResponseOrError<SuggestionsResponse>> suggestionsRequestObservable = suggestionsDao
                .getSuggestionsObservable(suggestionsPointer)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<SuggestionsResponse>>behaviorRefCount());

        final Observable<SuggestionsResponse> successSuggestionsObservable = suggestionsRequestObservable
                .compose(ResponseOrError.<SuggestionsResponse>onlySuccess());

        suggestedUsersObservable = successSuggestionsObservable
                .map(new Func1<SuggestionsResponse, List<BaseProfile>>() {
                    @Override
                    public List<BaseProfile> call(SuggestionsResponse suggestionsResponse) {
                        return suggestionsResponse.getUsers();
                    }
                })
                .map(toAdapterItems());

        suggestedPagesObservable = successSuggestionsObservable
                .map(new Func1<SuggestionsResponse, List<BaseProfile>>() {
                    @Override
                    public List<BaseProfile> call(SuggestionsResponse suggestionsResponse) {
                        return suggestionsResponse.getPages();
                    }
                })
                .map(toAdapterItems());

        itemListenedSubject
                .zipWith(successSuggestionsObservable, new Func2<BaseProfile, SuggestionsResponse, BothParams<BaseProfile, SuggestionsResponse>>() {
                    @Override
                    public BothParams<BaseProfile, SuggestionsResponse> call(BaseProfile baseProfile, SuggestionsResponse suggestionsResponse) {
                        return BothParams.of(baseProfile, suggestionsResponse);
                    }
                })
                .flatMap(new Func1<BothParams<BaseProfile, SuggestionsResponse>, Observable<ResponseOrError<SuggestionsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<SuggestionsResponse>> call(BothParams<BaseProfile, SuggestionsResponse> bothParams) {
                        final BaseProfile baseProfile = bothParams.param1();
                        final SuggestionsResponse suggestionsResponse = bothParams.param2();

                        final boolean isListeningToProfile = baseProfile.isListening();

                        Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                        if (isListeningToProfile) {
                            listenRequestObservable = apiService.unlistenProfile(baseProfile.getUsername())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnNext(new Action1<ResponseBody>() {
                                        @Override
                                        public void call(ResponseBody responseBody) {
                                            unListenSuccess.onNext(baseProfile.getName());
                                        }
                                    })
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        } else {
                            listenRequestObservable = apiService.listenProfile(baseProfile.getUsername())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnNext(new Action1<ResponseBody>() {
                                        @Override
                                        public void call(ResponseBody responseBody) {
                                            listenSuccess.onNext(baseProfile.getName());
                                        }
                                    })
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        }

                        return listenRequestObservable
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<SuggestionsResponse>>() {
                                    @Override
                                    public ResponseOrError<SuggestionsResponse> call(ResponseOrError<ResponseBody> response) {
                                        if (response.isData()) {
                                            if (ProfileType.USER.equals(baseProfile.getType())) {
                                                return ResponseOrError.fromData(suggestionsResponse.withUpdatedUser(baseProfile.getListenedProfile()));
                                            } else {
                                                return ResponseOrError.fromData(suggestionsResponse.withUpdatedPage(baseProfile.getListenedProfile()));
                                            }
                                        } else {
                                            errorSubject.onNext(new Throwable());
                                            // On error return current user in order to select/deselect already deselected/selected item to listenProfile
                                            return ResponseOrError.fromData(suggestionsResponse);
                                        }
                                    }
                                });
                    }
                })
                .subscribe(suggestionsDao.getSuggestionUpdateObserver(suggestionsPointer));

        errorObservable = Observable.merge(
                suggestionsRequestObservable.compose(ResponseOrError.<SuggestionsResponse>onlyError()),
                errorSubject);

        progressObservable = suggestionsRequestObservable.map(Functions1.returnFalse())
                .startWith(true);

    }

    @Nonnull
    public Observable<String> getListenSuccessObservable() {
        return listenSuccess;
    }

    @Nonnull
    public Observable<String> getUnListenSuccessObservable() {
        return unListenSuccess;
    }

    @NonNull
    private Func1<List<BaseProfile>, List<BaseAdapterItem>> toAdapterItems() {
        return baseProfiles -> {
            if (!baseProfiles.isEmpty()) {
                final List<BaseAdapterItem> transform = Lists.transform(baseProfiles, new Function<BaseProfile, BaseAdapterItem>() {
                    @Nullable
                    @Override
                    public BaseAdapterItem apply(@Nullable BaseProfile input) {

                        return new SuggestionAdapterItem(input, itemListenedSubject);
                    }
                });

                return ImmutableList.copyOf(transform);
            } else {
                return ImmutableList.of(new NoDataTextAdapterItem(resources.getString(R.string.nothing_to_show)));
            }
        };
    }

    public Observable<List<BaseAdapterItem>> getSuggestedPagesObservable() {
        return suggestedPagesObservable;
    }

    public Observable<List<BaseAdapterItem>> getSuggestedUsersObservable() {
        return suggestedUsersObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public static class SuggestionAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final BaseProfile baseprofile;
        @Nonnull
        private final Observer<BaseProfile> itemListenedSubject;

        public SuggestionAdapterItem(@Nonnull BaseProfile baseprofile,
                                     @Nonnull Observer<BaseProfile> itemListenedSubject) {
            this.baseprofile = baseprofile;
            this.itemListenedSubject = itemListenedSubject;
        }

        public void onItemClicked() {
            itemListenedSubject.onNext(baseprofile);
        }

        @Nonnull
        public BaseProfile getBaseprofile() {
            return baseprofile;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SuggestionAdapterItem &&
                    baseprofile.getId().equals(((SuggestionAdapterItem) item).baseprofile.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SuggestionAdapterItem &&
                    baseprofile.equals(((SuggestionAdapterItem) item).baseprofile);
        }
    }
}
