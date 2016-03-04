package com.shoutit.app.android.view.editprofile;

import android.graphics.Bitmap;
import android.net.Uri;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Strings;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UpdateUserRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.constants.AmazonConstants;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.FileHelper;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.rx.Actions1;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func5;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class EditProfilePresenter {

    private final BehaviorSubject<String> nameSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> usernameSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> bioSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> websiteSubject = BehaviorSubject.create();
    private final BehaviorSubject<UserLocation> locationSubject = BehaviorSubject.create();
    private final BehaviorSubject<Uri> lastSelectedAvatarUri = BehaviorSubject.create();
    private final BehaviorSubject<Uri> lastSelectedCoverUri = BehaviorSubject.create();

    private final PublishSubject<Object> saveClickSubject = PublishSubject.create();
    private final PublishSubject<Boolean> progressSubject = PublishSubject.create();
    private final PublishSubject<Boolean> avatarProgressSubject = PublishSubject.create();
    private final PublishSubject<Boolean> coverProgressSubject = PublishSubject.create();
    private final PublishSubject<Object> uploadCoverSubject = PublishSubject.create();

    @Nonnull
    private final Observable<User> userObservable;
    @Nonnull
    private final Observable<String> avatarObservable;
    @Nonnull
    private final Observable<String> coverObservable;
    @Nonnull
    private final Observable<User> successObservable;
    @Nonnull
    private final Scheduler uiScheduler;

    @Inject
    public EditProfilePresenter(@Nonnull UserPreferences userPreferences,
                                @Nonnull final ApiService apiService,
                                @Nonnull final TransferUtility transferUtility,
                                @Nonnull final FileHelper fileHelper,
                                @Nonnull final AmazonHelper amazonHelper,
                                @Nonnull@NetworkScheduler final Scheduler networkScheduler,
                                @Nonnull @UiScheduler final Scheduler uiScheduler) {
        this.uiScheduler = uiScheduler;

        /** User Data **/
        userObservable = userPreferences
                .getUserObservable()
                .filter(Functions1.isNotNull())
                .compose(ObservableExtensions.<User>behaviorRefCount());

        avatarObservable = Observable.concat(
                userObservable
                        .map(new Func1<User, String>() {
                            @Override
                            public String call(User user) {
                                return user.getImage();
                            }
                        }).first(),
                lastSelectedAvatarUri);

        coverObservable = Observable.concat(
                userObservable
                        .map(new Func1<User, String>() {
                            @Override
                            public String call(User user) {
                                return user.getCover();
                            }
                        }).first(),
                lastSelectedCoverUri);

        /** Errors **/
        final Observable<Boolean> nameErrorObservable = nameSubject
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return Strings.isNullOrEmpty(s);
                    }
                });

        final Observable<Boolean> usernameErrorObservable = usernameSubject
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return Strings.isNullOrEmpty(s);
                    }
                });

        final Observable<Boolean> hasAnyErrorObservable = Observable.combineLatest(
                nameErrorObservable, usernameErrorObservable, new Func2<Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean error1, Boolean error2) {
                        return error1 || error2;
                    }
                });

        /** Last Data from inputs **/
        final Observable<UpdateUserRequest> combinedData = Observable.combineLatest(
                usernameSubject.startWith((String) null),
                nameSubject.startWith((String) null),
                bioSubject.startWith((String) null),
                websiteSubject.startWith((String) null),
                locationSubject.startWith((UserLocation) null),
                new Func5<String, String, String, String, UserLocation, UpdateUserRequest>() {
                    @Override
                    public UpdateUserRequest call(String username, String name, String bio, String website, UserLocation userLocation) {
                        return UpdateUserRequest.updateProfile(username, name, bio, website, userLocation);
                    }
                });

        /** Update request **/
        final Observable<ResponseOrError<User>> updateRequest = saveClickSubject
                .withLatestFrom(hasAnyErrorObservable, new Func2<Object, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Object ignore, Boolean hasAnyError) {
                        return hasAnyError;
                    }
                })
                .filter(Functions1.isFalse())
                .doOnNext(Actions1.progressOnNext(progressSubject, true))
                .switchMap(new Func1<Boolean, Observable<UpdateUserRequest>>() {
                    @Override
                    public Observable<UpdateUserRequest> call(Boolean ignore) {
                        return combinedData.first();
                    }
                })
                .switchMap(new Func1<UpdateUserRequest, Observable<ResponseOrError<User>>>() {
                    @Override
                    public Observable<ResponseOrError<User>> call(UpdateUserRequest updateUserRequest) {
                        return apiService.updateUser(updateUserRequest)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<User>toResponseOrErrorObservable());
                    }
                })
                .doOnNext(Actions1.progressOnNext(progressSubject, false))
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        successObservable = updateRequest
                .compose(ResponseOrError.<User>onlySuccess());

        /** Upload images **/
        final Observable<ResponseOrError<File>> coverFileToUploadObservable = lastSelectedCoverUri
                .doOnNext(Actions1.progressOnNext(coverProgressSubject, true))
                .subscribeOn(networkScheduler)
                .observeOn(networkScheduler)
                .switchMap(new Func1<Uri, Observable<File>>() {
                    @Override
                    public Observable<File> call(Uri imageUri) {
                        try {
                            final String tempFile = fileHelper.createTempFileAndStoreUri(imageUri);
                            final Bitmap bitmapToUpload = ImageHelper.prepareImageToUpload(tempFile, ImageHelper.MAX_COVER_SIZE);
                            return Observable.just(fileHelper.saveBitmapToTempFile(bitmapToUpload, "name"));
                        } catch (IOException e) {
                            // TODO
                            e.printStackTrace();
                            return Observable.error(new Throwable());
                        }
                    }
                })
                .compose(ResponseOrError.<File>toResponseOrErrorObservable())
                .compose(ObservableExtensions.<ResponseOrError<File>>behaviorRefCount());

        final Observable<ResponseOrError<String>> uploadCoverToAmazonObservable = coverFileToUploadObservable
                .compose(ResponseOrError.<File>onlySuccess())
                .filter(Functions1.isNotNull())
                .switchMap(new Func1<File, Observable<ResponseOrError<String>>>() {
                    @Override
                    public Observable<ResponseOrError<String>> call(File fileToUpload) {
                        return amazonHelper.uploadImageObservable(AmazonConstants.BUCKET_USER_URL, fileToUpload);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        uploadCoverToAmazonObservable
                .compose(ResponseOrError.<String>onlySuccess())
                .switchMap(new Func1<String, Observable<ResponseOrError<User>>>() {
                    @Override
                    public Observable<ResponseOrError<User>> call(String coverUrlToUpload) {
                        return apiService.updateUser(UpdateUserRequest.updateWithCoverUrl(coverUrlToUpload))
                                .compose(ResponseOrError.<User>toResponseOrErrorObservable());
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());
    }

    @Nonnull
    public Observer<Uri> getLastSelectedAvatarUriObserver() {
        return lastSelectedAvatarUri;
    }

    @Nonnull
    public Observer<Uri> getLastSelectedCoverUriObserver() {
        return lastSelectedCoverUri;
    }

    @Nonnull
    public Observable<User> getUserObservable() {
        return userObservable;
    }

    @Nonnull
    public Observable<String> getAvatarObservable() {
        return avatarObservable;
    }

    @Nonnull
    public Observable<String> getCoverObservable() {
        return coverObservable;
    }

    @Nonnull
    public Observer<String> getNameObserver() {
        return RxMoreObservers.ignoreCompleted(nameSubject);
    }

    @Nonnull
    public Observer<String> getUserNameObserver() {
        return RxMoreObservers.ignoreCompleted(usernameSubject);
    }

    @Nonnull
    public Observer<String> getBioObserver() {
        return RxMoreObservers.ignoreCompleted(bioSubject);
    }

    @Nonnull
    public Observer<String> getWebsiteObserver() {
        return RxMoreObservers.ignoreCompleted(websiteSubject);
    }

    @Nonnull
    public Observable<User> getSuccessObservable() {
        return successObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressSubject
                .observeOn(uiScheduler);
    }

    public void onSaveClicked() {
        saveClickSubject.onNext(null);
    }
}
