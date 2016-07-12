package com.shoutit.app.android.view.profile.page.edit;

import android.net.Uri;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Page;

import javax.inject.Inject;

import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

public class EditPagePresenter {

    private final UserPreferences mUserPreferences;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final PageDataProvider mPageDataProvider;

    private Listener mListener;
    private CompositeSubscription mCompositeSubscription;
    private Page mPage;

    @Inject
    public EditPagePresenter(UserPreferences userPreferences,
                             ApiService apiService,
                             @NetworkScheduler Scheduler networkScheduler,
                             @UiScheduler Scheduler uiScheduler,
                             PageDataProvider pageDataProvider) {
        mUserPreferences = userPreferences;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mPageDataProvider = pageDataProvider;
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
                }, throwable -> {
                    mListener.setProgress(false);
                    mListener.error();
                }));
    }

    public void editFinished(EditData editData) {
        //TODO verification
        final Page newPage = new Page(mPage.getId(),
                mPage.getType(),
                mPage.getUsername(),
                mPage.getName(),
                mPage.getFirstName(),
                mPage.getLastName(),
                mPage.isActivated(),
                mPage.getImage(),
                mPage.getCover(),
                mPage.isListening(),
                mPage.getListenersCount(),
                mPage.getLocation(),
                mPage.getStats(),
                mPage.isOwner(),
                mPage.getEmail(),
                mPage.getAdmin(),
                editData.about,
                editData.description,
                editData.phone,
                editData.founded,
                editData.impressum,
                editData.overview,
                editData.mission,
                editData.generalInfo,
                mPage.isVerified(),
                editData.published);

        mListener.setProgress(true);
        mCompositeSubscription.add(mApiService.updatePage(mPage.getId(), newPage)
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

    public void unregister() {
        mCompositeSubscription.unsubscribe();
    }

    public void avatarChosen(Optional<Uri> uriOptional) {

        // TODO
    }

    public void coverChosen(Optional<Uri> uriOptional) {
        // TODO
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

        void finish();

        void error();
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