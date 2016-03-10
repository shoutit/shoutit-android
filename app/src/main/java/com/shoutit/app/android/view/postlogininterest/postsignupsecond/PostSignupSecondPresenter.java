package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.SuggestionsResponse;
import com.shoutit.app.android.dao.SuggestionsDao;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class PostSignupSecondPresenter {
    private static final int PAGE_SIZE = 6;

    private final Observable<List<BaseAdapterItem>> suggestedPagesObservable;
    private final Observable<List<BaseAdapterItem>> suggestedUsersObservable;

    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<BaseProfile> itemListenedSubject = PublishSubject.create();

    public PostSignupSecondPresenter(@Nonnull SuggestionsDao suggestionsDao,
                                     @Nonnull final ApiService apiService,
                                     @Nonnull UserPreferences userPreferences,
                                     @NetworkScheduler final Scheduler networkScheduler,
                                     @UiScheduler final Scheduler uiScheduler) {

        final SuggestionsDao.SuggestionsPointer suggestionsPointer =
                new SuggestionsDao.SuggestionsPointer(PAGE_SIZE, userPreferences.getLocation());

        final Observable<ResponseOrError<SuggestionsResponse>> suggestionsRequestObservable = suggestionsDao
                .getSuggestionsObservable(suggestionsPointer)
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
                .throttleFirst(1, TimeUnit.SECONDS)
                .withLatestFrom(successSuggestionsObservable, new Func2<BaseProfile, SuggestionsResponse, BothParams<BaseProfile, SuggestionsResponse>>() {
                    @Override
                    public BothParams<BaseProfile, SuggestionsResponse> call(BaseProfile baseProfile, SuggestionsResponse suggestionsResponse) {
                        return BothParams.of(baseProfile, suggestionsResponse);
                    }
                })
                .switchMap(new Func1<BothParams<BaseProfile, SuggestionsResponse>, Observable<ResponseOrError<SuggestionsResponse>>>() {
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
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        } else {
                            listenRequestObservable = apiService.listenProfile(baseProfile.getUsername())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        }

                        return listenRequestObservable
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<SuggestionsResponse>>() {
                                    @Override
                                    public ResponseOrError<SuggestionsResponse> call(ResponseOrError<ResponseBody> response) {
                                        if (response.isData()) {
                                            if (ProfileType.USER.equals(baseProfile.getType())) {
                                                return ResponseOrError.fromData(suggestionsResponse.withUpdatedUser(baseProfile));
                                            } else {
                                                return ResponseOrError.fromData(suggestionsResponse.withUpdatedPage(baseProfile));
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

    }

    @NonNull
    private Func1<List<BaseProfile>, List<BaseAdapterItem>> toAdapterItems() {
        return new Func1<List<BaseProfile>, List<BaseAdapterItem>>() {
            @Override
            public List<BaseAdapterItem> call(List<BaseProfile> baseProfiles) {
                final List<BaseAdapterItem> transform = Lists.transform(baseProfiles, new Function<BaseProfile, BaseAdapterItem>() {
                    @Nullable
                    @Override
                    public BaseAdapterItem apply(@Nullable BaseProfile input) {
                        return new PostSignupListenItem(input, itemListenedSubject);
                    }
                });

                return ImmutableList.copyOf(transform);
            }
        };
    }

    public Observable<List<BaseAdapterItem>> getSuggestedPagesObservable() {
        return suggestedPagesObservable;
    }

    public Observable<List<BaseAdapterItem>> getSuggestedUsersObservable() {
        return suggestedUsersObservable;
    }

    public static class PostSignupListenItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final BaseProfile baseprofile;
        @Nonnull
        private final Observer<BaseProfile> itemListenedSubject;

        public PostSignupListenItem(@Nonnull BaseProfile baseprofile,
                                    @Nonnull Observer<BaseProfile> itemListenedSubject) {
            this.baseprofile = baseprofile;
            this.itemListenedSubject = itemListenedSubject;
        }

        @Nonnull
        public Observer<BaseProfile> getItemListenedSubject() {
            return itemListenedSubject;
        }

        public void onItemClicked() {
            itemListenedSubject.onNext(baseprofile);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof  PostSignupListenItem &&
                    baseprofile.getId().equals(((PostSignupListenItem) item).baseprofile.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof  PostSignupListenItem &&
                    baseprofile.equals(((PostSignupListenItem) item).baseprofile);
        }
    }
}
