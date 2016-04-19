package com.shoutit.app.android.view.profile.tagprofile;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

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
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.RelatedTagsResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.TagsDao;
import com.shoutit.app.android.model.TagShoutsPointer;
import com.shoutit.app.android.view.profile.ProfileAdapterItems;
import com.shoutit.app.android.view.profile.ProfilePresenter;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.subsearch.SubSearchActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.ArrayList;
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
import rx.functions.Func3;
import rx.observers.Observers;
import rx.subjects.PublishSubject;

public class TagProfilePresenter implements ProfilePresenter {
    private static final int SHOUTS_PAGE_SIZE = 4;
    private static final int MAX_RELATED_TAGS = 3;

    private final PublishSubject<TagDetail> onListenActionClickedSubject = PublishSubject.create();
    private final PublishSubject<ListenedTagWithRelatedTags> onListenRelatedTagClickedSubject = PublishSubject.create();
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<Object> moreMenuOptionClickedSubject = PublishSubject.create();
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();
    private final PublishSubject<String> profileToOpenSubject = PublishSubject.create();
    private final PublishSubject<String> showAllShoutsSubject = PublishSubject.create();
    private final PublishSubject<Object> shareInitSubject = PublishSubject.create();
    private final PublishSubject<Object> searchMenuItemClickSubject = PublishSubject.create();

    private final Observable<String> shareObservable;
    private final Observable<String> avatarObservable;
    private final Observable<String> coverUrlObservable;
    private final Observable<String> toolbarTitleObservable;
    private final Observable<String> toolbarSubtitleObservable;
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Intent> searchMenuItemClickObservable;

    private final boolean isLoggedInAsNormalUser;
    private final TagsDao tagsDao;
    @Nonnull
    private final String tagName;


    public TagProfilePresenter(TagsDao tagsDao, final ShoutsDao shoutsDao, @Nonnull final String tagName,
                               @UiScheduler final Scheduler uiScheduler, @NetworkScheduler final Scheduler networkScheduler,
                               final ApiService apiService, @ForActivity final Context context,
                               UserPreferences userPreferences) {
        this.tagsDao = tagsDao;
        this.tagName = tagName;
        isLoggedInAsNormalUser = userPreferences.isNormalUser();

        /** Base Tag **/
        final Observable<ResponseOrError<TagDetail>> tagRequestObservable = tagsDao.getTagObservable(tagName)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<TagDetail>>behaviorRefCount());

        onListenActionClickedSubject
                .throttleFirst(500, TimeUnit.MILLISECONDS)
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

        final Observable<ProfileAdapterItems.TagInfoAdapterItem> tagAdapterItem = successTagRequestObservable
                .map(new Func1<TagDetail, ProfileAdapterItems.TagInfoAdapterItem>() {
                    @Override
                    public ProfileAdapterItems.TagInfoAdapterItem call(TagDetail tagDetail) {
                        return new ProfileAdapterItems.TagInfoAdapterItem(tagDetail, isLoggedInAsNormalUser,
                                actionOnlyForLoggedInUserSubject, onListenActionClickedSubject, moreMenuOptionClickedSubject);
                    }
                });

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
        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequestObservable = userPreferences
                .getLocationObservable()
                .first()
                .filter(Functions1.isNotNull())
                .switchMap(new Func1<UserLocation, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(UserLocation userLocation) {
                        return shoutsDao.getTagsShoutsObservable(new TagShoutsPointer(SHOUTS_PAGE_SIZE, tagName, userLocation.getLocationPointer()))
                                .observeOn(uiScheduler);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> shoutsSuccessResponse = shoutsRequestObservable
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

        /** Related Tags **/
        final Observable<ResponseOrError<RelatedTagsResponse>> relatedTagsRequest = tagsDao
                .getRelatedTagsObservable(tagName)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<RelatedTagsResponse>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> successRelatedTags = relatedTagsRequest
                .compose(ResponseOrError.<RelatedTagsResponse>onlySuccess())
                .map(new Func1<RelatedTagsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(RelatedTagsResponse relatedTagsResponse) {
                        final List<TagDetail> tags = relatedTagsResponse.getResults();
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        if (tags != null) {
                            final int tagsNumberToDisplay = Math.min(tags.size(), MAX_RELATED_TAGS);
                            for (int i = 0; i < tagsNumberToDisplay; i++) {
                                builder.add(getRelatedTagdapterItemForPosition(i, tags.get(i), relatedTagsResponse, tagsNumberToDisplay));
                            }
                        }

                        return builder.build();
                    }
                });

        onListenRelatedTagClickedSubject
                .throttleFirst(1, TimeUnit.SECONDS)
                .switchMap(new Func1<ListenedTagWithRelatedTags, Observable<ResponseOrError<RelatedTagsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<RelatedTagsResponse>> call(final ListenedTagWithRelatedTags listenedTagWithRelatedTags) {
                        final TagDetail tagDetail = listenedTagWithRelatedTags.getTagInSection();

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

                        return request.map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<RelatedTagsResponse>>() {
                            @Override
                            public ResponseOrError<RelatedTagsResponse> call(ResponseOrError<ResponseBody> response) {
                                if (response.isData()) {
                                    return ResponseOrError.fromData(updateRelatedTagsWithListenings(listenedTagWithRelatedTags));
                                } else {
                                    errorSubject.onNext(new Throwable());
                                    // On error return current tag in order to select/deselect already deselected/selected 'listenTagProfile' icon
                                    return ResponseOrError.fromData(listenedTagWithRelatedTags.getRelatedTagsResponse());
                                }
                            }
                        });
                    }
                })
                .subscribe(tagsDao.getUpdatedRelatedTagsObserver(tagName));


        /** All adapter items **/
        allAdapterItemsObservable = Observable.combineLatest(
                tagAdapterItem,
                successRelatedTags,
                shoutsSuccessResponse.startWith(ImmutableList.<List<BaseAdapterItem>>of()),
                new Func3<ProfileAdapterItems.TagInfoAdapterItem, List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ProfileAdapterItems.TagInfoAdapterItem tagItem,
                                                      List<BaseAdapterItem> relatedTags,
                                                      List<BaseAdapterItem> shouts) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        builder.add(tagItem);

                        if (!relatedTags.isEmpty()) {
                            builder.add(new HeaderAdapterItem(context.getString(R.string.tag_profile_related_interests).toUpperCase()));
                            builder.addAll(relatedTags);
                        }

                        if (!shouts.isEmpty()) {
                            builder.add(new HeaderAdapterItem(
                                    context.getString(R.string.tag_profile_shouts, tagItem.getTagDetail().getName()).toUpperCase()))
                            .addAll(shouts)
                            .add(new ProfileAdapterItems.SeeAllUserShoutsAdapterItem(
                                    showAllShoutsSubject, tagName));
                        }

                        return builder.build();
                    }
                });

        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsRequestObservable),
                ResponseOrError.transform(relatedTagsRequest),
                ResponseOrError.transform(tagRequestObservable)))
                .mergeWith(errorSubject)
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        /** Progress **/
        progressObservable = Observable.merge(tagRequestObservable, errorObservable)
                .map(Functions1.returnFalse())
                .startWith(true)
                .observeOn(uiScheduler);

        /** Menu actions **/
        shareObservable = shareInitSubject
                .withLatestFrom(successTagRequestObservable, new Func2<Object, TagDetail, String>() {
                    @Override
                    public String call(Object o, TagDetail tagDetail) {
                        return tagDetail.getWebUrl();
                    }
                });

        searchMenuItemClickObservable = searchMenuItemClickSubject
                .withLatestFrom(successTagRequestObservable, new Func2<Object, TagDetail, Intent>() {
                    @Override
                    public Intent call(Object o, TagDetail tagDetail) {
                        return SubSearchActivity.newIntent(context,
                                SearchPresenter.SearchType.TAG_PROFILE, tagDetail.getUsername(),
                                tagDetail.getName());
                    }
                });

    }

    private ProfileAdapterItems.RelatedTagAdapterItem getRelatedTagdapterItemForPosition(int position, TagDetail tag,
                                                                                         RelatedTagsResponse relatedTagsResponse,
                                                                                         int tagsNumberToDisplay) {
        if (position == 0) {
            return new ProfileAdapterItems.RelatedTagAdapterItem(true, false, tag, relatedTagsResponse,
                    onListenRelatedTagClickedSubject, profileToOpenSubject, actionOnlyForLoggedInUserSubject, isLoggedInAsNormalUser, tagsNumberToDisplay == 1);
        } else if (position == tagsNumberToDisplay - 1) {
            return new ProfileAdapterItems.RelatedTagAdapterItem(false, true, tag, relatedTagsResponse,
                    onListenRelatedTagClickedSubject, profileToOpenSubject, actionOnlyForLoggedInUserSubject, isLoggedInAsNormalUser, false);
        } else {
            return new ProfileAdapterItems.RelatedTagAdapterItem(false, false, tag, relatedTagsResponse,
                    onListenRelatedTagClickedSubject, profileToOpenSubject, actionOnlyForLoggedInUserSubject, isLoggedInAsNormalUser, false);
        }
    }

    @Nonnull
    private RelatedTagsResponse updateRelatedTagsWithListenings(@Nonnull ListenedTagWithRelatedTags listenedTagWithRelatedTags) {
        final List<TagDetail> tags = listenedTagWithRelatedTags.getRelatedTagsResponse().getResults();
        final TagDetail tagToUpdate = listenedTagWithRelatedTags.getTagInSection();

        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).getName().equals(tagToUpdate.getName())) {
                final List<TagDetail> updatedTags = new ArrayList<>(tags);
                updatedTags.set(i, tagToUpdate.toListenedTag());

                return new RelatedTagsResponse(updatedTags);
            }
        }

        return new RelatedTagsResponse(tags);
    }

    @Nonnull
    public Observable<Intent> getSearchMenuItemClickObservable() {
        return searchMenuItemClickObservable;
    }

    public void onSearchMenuItemClicked() {
        searchMenuItemClickSubject.onNext(null);
    }

    @Nonnull
    @Override
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    @Override
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getAvatarObservable() {
        return avatarObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getCoverUrlObservable() {
        return coverUrlObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getToolbarTitleObservable() {
        return toolbarTitleObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getToolbarSubtitleObservable() {
        return toolbarSubtitleObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getShareObservable() {
        return shareObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedSubject;
    }

    @Nonnull
    @Override
    public Observable<String> getProfileToOpenObservable() {
        return profileToOpenSubject;
    }

    @Nonnull
    @Override
    public Observer<Object> getShareInitObserver() {
        return shareInitSubject;
    }

    @Nonnull
    @Override
    public Observable<Object> getMoreMenuOptionClickedSubject() {
        return moreMenuOptionClickedSubject;
    }

    @Nonnull
    @Override
    public Observable<String> getSeeAllShoutsObservable() {
        return showAllShoutsSubject;
    }

    @Override
    public void refreshProfile() {
        tagsDao.refreshRelatedTags(tagName);
        tagsDao.refreshTag(tagName);
    }

    @Nonnull
    @Override
    public Observer<String> sendReportObserver() {
        return Observers.empty();
    }

    @NonNull
    @Override
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    @Override
    public Observable<Object> getActionOnlyForLoggedInUserObservable() {
        return actionOnlyForLoggedInUserSubject;
    }

    public static class ListenedTagWithRelatedTags {

        @Nonnull
        private final RelatedTagsResponse relatedTagsResponse;
        @Nonnull
        private final TagDetail tagInSection;

        public ListenedTagWithRelatedTags(@Nonnull RelatedTagsResponse relatedTagsResponse,
                                          @Nonnull TagDetail tagInSection) {
            this.relatedTagsResponse = relatedTagsResponse;
            this.tagInSection = tagInSection;
        }

        @Nonnull
        public RelatedTagsResponse getRelatedTagsResponse() {
            return relatedTagsResponse;
        }

        @Nonnull
        public TagDetail getTagInSection() {
            return tagInSection;
        }
    }
}
