package com.shoutit.app.android.view.profile.page.edit;

import android.net.Uri;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.UpdatePage;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.FileHelper;
import com.shoutit.app.android.utils.ImageHelper;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

public class EditPagePresenter {

    private final UserPreferences mUserPreferences;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final PageDataProvider mPageDataProvider;
    private final boolean mIsLoggedIn;
    private final FileHelper mFileHelper;
    private final AmazonHelper mAmazonHelper;

    private Listener mListener;
    private CompositeSubscription mCompositeSubscription;
    private Page mPage;
    private String avatar;
    private String cover;

    @Inject
    public EditPagePresenter(UserPreferences userPreferences,
                             ApiService apiService,
                             @NetworkScheduler Scheduler networkScheduler,
                             @UiScheduler Scheduler uiScheduler,
                             PageDataProvider pageDataProvider,
                             boolean isLoggedIn,
                             FileHelper fileHelper,
                             AmazonHelper amazonHelper) {
        mUserPreferences = userPreferences;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mPageDataProvider = pageDataProvider;
        mIsLoggedIn = isLoggedIn;
        mFileHelper = fileHelper;
        mAmazonHelper = amazonHelper;
    }

    public void register(Listener listener) {
        mCompositeSubscription = new CompositeSubscription();
        mListener = listener;
        mListener.setProgress(true);
        mCompositeSubscription.add(mPageDataProvider.getPage()
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(page -> {
                    mPage = page;
                    mListener.setProgress(false);

                    final String name = page.getName();
                    final String about = page.getAbout();
                    final boolean published = page.isPublished();
                    final String description = page.getDescription();
                    final String phone = page.getPhone();
                    final String founded = page.getFounded();
                    final String impressum = page.getImpressum();
                    final String overview = page.getOverview();
                    final String mission = page.getMission();
                    final String generalInfo = page.getGeneralInfo();
                    final String image = page.getImage();
                    final String cover = page.getCover();

                    mListener.setName(name);
                    mListener.setAbout(about);
                    mListener.setPublished(published);
                    mListener.setDescription(description);
                    mListener.setPhone(phone);
                    mListener.setFounded(founded);
                    mListener.setImpressum(impressum);
                    mListener.setOverview(overview);
                    mListener.setMission(mission);
                    mListener.setGeneralInfo(generalInfo);
                    mListener.setCover(cover);
                    mListener.setAvatar(image);
                }, throwable -> {
                    mListener.setProgress(false);
                    mListener.error();
                }));
    }

    public void editFinished(EditData editData) {
        final UpdatePage newPage = new UpdatePage(editData.about,
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
                editData.published);

        mListener.setProgress(true);

        mCompositeSubscription.add(getPageObservable(newPage)
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(page -> {
                    mUserPreferences.setUserOrPage(page);
                    mListener.setProgress(false);
                    mListener.finish();
                }, throwable -> {
                    mListener.setProgress(false);
                    mListener.error();
                }));
    }

    private Observable<Page> getPageObservable(UpdatePage newPage) {
        final Observable<Page> pageObservable;
        if (!mIsLoggedIn) {
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
                        mListener.error();
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
                            mListener.error();
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
                        mListener.error();
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
                            mListener.error();
                        }
                    });
        }
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

        void setProgress(boolean show);

        void setAvatar(String url);

        void finish();

        void error();

        void setCover(String data);
    }

    public static class EditData {

        final String name;
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