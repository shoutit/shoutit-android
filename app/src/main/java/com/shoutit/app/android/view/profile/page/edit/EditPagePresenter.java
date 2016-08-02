package com.shoutit.app.android.view.profile.page.edit;

import android.net.Uri;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.UpdatePage;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.FileHelper;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.PreferencesHelper;

import java.io.File;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

public class EditPagePresenter {

    private final UserPreferences mUserPreferences;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    @Nonnull
    private final ProfilesDao mProfilesDao;
    private final FileHelper mFileHelper;
    private final AmazonHelper mAmazonHelper;

    private Listener mListener;
    private CompositeSubscription mCompositeSubscription;
    private Page mPage;
    private String avatar;
    private String cover;
    private UserLocation mLocation;
    private final String pageToEditUsername;
    private final boolean isMyProfile;

    @Inject
    public EditPagePresenter(UserPreferences userPreferences,
                             ApiService apiService,
                             @NetworkScheduler Scheduler networkScheduler,
                             @UiScheduler Scheduler uiScheduler,
                             @Nonnull final ProfilesDao profilesDao,
                             @Nonnull String pageToEditUsername,
                             @Nonnull PreferencesHelper preferencesHelper,
                             FileHelper fileHelper,
                             AmazonHelper amazonHelper) {
        mUserPreferences = userPreferences;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mProfilesDao = profilesDao;
        mFileHelper = fileHelper;
        mAmazonHelper = amazonHelper;

        isMyProfile = preferencesHelper.isMyProfile(pageToEditUsername);
        if (isMyProfile) {
            pageToEditUsername = BaseProfile.ME;
        }

        this.pageToEditUsername = pageToEditUsername;
    }

    public void register(Listener listener) {

        final Observable<ResponseOrError<BaseProfile>> profileRequest = mProfilesDao
                .getProfileObservable(pageToEditUsername)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .compose(ObservableExtensions.behaviorRefCount());

        mCompositeSubscription = new CompositeSubscription();
        mListener = listener;
        mListener.setProgress(true);

        mCompositeSubscription.add(profileRequest
                .compose(ResponseOrError.onlySuccess())
                .subscribe(page -> {
                    mPage = (Page) page;
                    mListener.setProgress(false);

                    mListener.setName(mPage.getName());
                    mListener.setUsername(mPage.getUsername());
                    mListener.setWebsite(mPage.getWebsite());
                    mListener.setAbout(mPage.getAbout());
                    mListener.setPublished(mPage.isPublished());
                    mListener.setDescription(mPage.getDescription());
                    mListener.setPhone(mPage.getPhone());
                    mListener.setFounded(mPage.getFounded());
                    mListener.setImpressum(mPage.getImpressum());
                    mListener.setOverview(mPage.getOverview());
                    mListener.setMission(mPage.getMission());
                    mListener.setGeneralInfo(mPage.getGeneralInfo());
                    mListener.setCover(mPage.getCover());
                    mListener.setAvatar(mPage.getImage());
                    mListener.setUpLocation(mPage.getLocation());
                }));

        mCompositeSubscription.add(profileRequest
                .compose(ResponseOrError.onlyError())
                .subscribe(throwable -> {
                    mListener.setProgress(false);
                    mListener.error(throwable);
                }));
    }

    public void editFinished(EditData editData) {
        final UpdatePage newPage = new UpdatePage(
                editData.name,
                editData.username,
                editData.website,
                editData.about,
                editData.description,
                editData.phone,
                editData.founded,
                editData.impressum,
                editData.overview,
                editData.mission,
                editData.generalInfo,
                cover != null ? cover : mPage.getCover(),
                avatar != null ? avatar : mPage.getImage(),
                mPage.isVerified(),
                editData.published,
                mLocation != null ? mLocation : mPage.getLocation());

        mListener.setProgress(true);

        mCompositeSubscription.add(getPageUpdateObservable(newPage)
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(page -> {
                    mProfilesDao.getProfileDao(page.getUsername())
                            .updatedProfileLocallyObserver().onNext(ResponseOrError.fromData(page));
                    // In case of edited username
                    mProfilesDao.getProfileDao(pageToEditUsername)
                            .updatedProfileLocallyObserver().onNext(ResponseOrError.fromData(page));

                    mListener.setProgress(false);
                    mListener.finishAndSetResult();
                }, throwable -> {
                    mListener.setProgress(false);
                    mListener.error(throwable);
                }));
    }

    private Observable<Page> getPageUpdateObservable(UpdatePage newPage) {
        final Observable<Page> pageObservable;
        if (!mUserPreferences.isLoggedInAsPage()) {
            pageObservable = mApiService.updatePage(mPage.getId(), newPage);
        } else {
            pageObservable = mApiService.updatePage(newPage);
        }
        return pageObservable;
    }

    public void unregister() {
        mCompositeSubscription.unsubscribe();
    }

    public void avatarChosen(Optional<Uri> uriOptional) {
        if (uriOptional.isPresent()) {
            mListener.setProgress(true);
            final Observable<ResponseOrError<File>> fileObservable = mFileHelper.scaleAndCompressImage(ImageHelper.MAX_AVATAR_SIZE, uriOptional.get())
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .compose(ObservableExtensions.behaviorRefCount());

            fileObservable
                    .compose(ResponseOrError.onlyError())
                    .subscribe(throwable -> {
                        mListener.setProgress(false);
                        mListener.error(throwable);
                    });

            fileObservable
                    .compose(ResponseOrError.onlySuccess())
                    .switchMap(mAmazonHelper.uploadImageToAmazonFunction(mNetworkScheduler, mUiScheduler))
                    .subscribe(fileResponseOrError -> {
                        mListener.setProgress(false);
                        if (fileResponseOrError.isData()) {
                            avatar = fileResponseOrError.data();
                            mListener.setAvatar(avatar);
                        } else {
                            mListener.imageUploadError();
                        }
                    });
        }
    }

    public void coverChosen(Optional<Uri> uriOptional) {
        if (uriOptional.isPresent()) {
            mListener.setProgress(true);
            final Observable<ResponseOrError<File>> fileObservable = mFileHelper.scaleAndCompressImage(ImageHelper.MAX_COVER_SIZE, uriOptional.get())
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .compose(ObservableExtensions.behaviorRefCount());

            fileObservable
                    .compose(ResponseOrError.onlyError())
                    .subscribe(throwable -> {
                        mListener.setProgress(false);
                        mListener.error(throwable);
                    });

            fileObservable
                    .compose(ResponseOrError.onlySuccess())
                    .switchMap(mAmazonHelper.uploadImageToAmazonFunction(mNetworkScheduler, mUiScheduler))
                    .subscribe(fileResponseOrError -> {
                        mListener.setProgress(false);
                        if (fileResponseOrError.isData()) {
                            cover = fileResponseOrError.data();
                            mListener.setCover(cover);
                        } else {
                            mListener.imageUploadError();
                        }
                    });
        }
    }

    public void onLocationChanged(UserLocation userLocation) {
        mLocation = userLocation;
        mListener.setUpLocation(userLocation);
    }

    public interface Listener {

        void setGeneralInfo(String generalInfo);

        void setMission(String mission);

        void setOverview(String overview);

        void setImpressum(String impressum);

        void setFounded(String founded);

        void setPhone(String phone);

        void setDescription(String description);

        void setPublished(boolean published);

        void setAbout(String about);

        void setName(String name);

        void setUsername(String username);

        void setWebsite(String website);

        void setProgress(boolean show);

        void setAvatar(String url);

        void finishAndSetResult();

        void error(Throwable throwable);

        void setCover(String data);

        void setUpLocation(UserLocation location);

        void imageUploadError();
    }

    public static class EditData {

        final String name;
        final String username;
        final String website;
        final String about;
        final boolean published;
        final String description;
        final String phone;
        final String founded;
        final String impressum;
        final String overview;
        final String mission;
        final String generalInfo;

        public EditData(String name,
                        String username,
                        String website,
                        String about,
                        boolean published,
                        String description,
                        String phone,
                        String founded,
                        String impressum,
                        String overview,
                        String mission,
                        String generalInfo) {
            this.name = name;
            this.username = username;
            this.website = website;
            this.about = about;
            this.published = published;
            this.description = description;
            this.phone = phone;
            this.founded = founded;
            this.impressum = impressum;
            this.overview = overview;
            this.mission = mission;
            this.generalInfo = generalInfo;
        }
    }
}