package com.shoutit.app.android.view.editprofile;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UpdateUserRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.FileHelper;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.rx.Actions1;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Func7;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class EditProfilePresenter {

    private final BehaviorSubject<String> firstNameSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> lastNameSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> usernameSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> bioSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> websiteSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> mobileSubject = BehaviorSubject.create();
    private final BehaviorSubject<UserLocation> locationSubject = BehaviorSubject.create();
    private final BehaviorSubject<UpdateUserRequest> lastCombinedData = BehaviorSubject.create();
    private final BehaviorSubject<Uri> lastSelectedAvatarUri = BehaviorSubject.create();
    private final BehaviorSubject<Uri> lastSelectedCoverUri = BehaviorSubject.create();

    private final PublishSubject<Object> saveClickSubject = PublishSubject.create();
    private final PublishSubject<Boolean> progressSubject = PublishSubject.create();

    @Nonnull
    private final Observable<String> avatarObservable;
    @Nonnull
    private final Observable<String> coverObservable;
    @Nonnull
    private final Observable<User> successObservable;
    @Nonnull
    private final Observable<Object> imageUploadToApiSuccessObservable;

    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final FileHelper fileHelper;
    @Nonnull
    private final AmazonHelper amazonHelper;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Observable<Boolean> avatarProgressObservable;
    @Nonnull
    private final Observable<Boolean> coverProgressObservable;
    @Nonnull
    private final Observable<Throwable> imageUploadError;
    @Nonnull
    private final Observable<Throwable> updateProfileError;
    @Nonnull
    private final Observable<Boolean> firstNameErrorObservable;
    @Nonnull
    private final Observable<Boolean> lastNameErrorObservable;
    @Nonnull
    private final Observable<Boolean> usernameErrorObservable;
    @Nonnull
    private final Observable<UserLocation> locationObservable;
    @Nonnull
    private final Observable<User> userInputsObservable;

    public EditProfilePresenter(@Nonnull final UserPreferences userPreferences,
                                @Nonnull final ApiService apiService,
                                @Nonnull final FileHelper fileHelper,
                                @Nonnull final AmazonHelper amazonHelper,
                                @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                @Nonnull @UiScheduler final Scheduler uiScheduler,
                                @Nullable final State state) {
        this.apiService = apiService;
        this.fileHelper = fileHelper;
        this.amazonHelper = amazonHelper;
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;

        if (state != null) {
            locationSubject.onNext(state.getUserLocation());
        }

        /** User Data **/
        final Observable<User> userObservable = userPreferences
                .getUserObservable()
                .filter(Functions1.isNotNull())
                .compose(ObservableExtensions.<User>behaviorRefCount());

        userInputsObservable = userObservable
                .first()
                .filter(new Func1<User, Boolean>() {
                    @Override
                    public Boolean call(User user) {
                        return state == null;
                    }
                });

        locationObservable = Observable.merge(
                userInputsObservable.map(new Func1<User, UserLocation>() {
                    @Override
                    public UserLocation call(User user) {
                        return user.getLocation();
                    }
                }),
                locationSubject);

        avatarObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getImage();
                    }
                });

        coverObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getCover();
                    }
                });

        /** Errors **/
        firstNameErrorObservable = firstNameSubject
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return Strings.isNullOrEmpty(s);
                    }
                });

        lastNameErrorObservable = lastNameSubject
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return Strings.isNullOrEmpty(s);
                    }
                });

        usernameErrorObservable = usernameSubject
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return Strings.isNullOrEmpty(s);
                    }
                });

        final Observable<Boolean> hasAnyErrorObservable = Observable.combineLatest(
                firstNameErrorObservable, lastNameErrorObservable, usernameErrorObservable, new Func3<Boolean, Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean error1, Boolean error2, Boolean error3) {
                        return error1 || error2 || error3;
                    }
                });

        /** Last Data from inputs **/
        Observable.combineLatest(
                usernameSubject.startWith((String) null),
                firstNameSubject.startWith((String) null),
                lastNameSubject.startWith((String) null),
                bioSubject.startWith((String) null),
                websiteSubject.startWith((String) null),
                mobileSubject.startWith((String) null),
                locationSubject.startWith((UserLocation) null),
                new Func7<String, String, String, String, String, String, UserLocation, UpdateUserRequest>() {
                    @Override
                    public UpdateUserRequest call(String username, String firstName, String lastName, String bio, String website, String mobile, UserLocation userLocation) {
                        return UpdateUserRequest.updateProfile(username, firstName, lastName, bio, website, mobile, userLocation);
                    }
                })
                .subscribe(lastCombinedData);

        /** Update profile data **/
        final Observable<ResponseOrError<User>> updateRequest = saveClickSubject
                .withLatestFrom(hasAnyErrorObservable, new Func2<Object, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Object ignore, Boolean hasAnyError) {
                        return hasAnyError;
                    }
                })
                .filter(Functions1.isFalse())
                .switchMap(new Func1<Boolean, Observable<UpdateUserRequest>>() {
                    @Override
                    public Observable<UpdateUserRequest> call(Boolean ignore) {
                        return lastCombinedData.first();
                    }
                })
                .doOnNext(Actions1.progressOnNext(progressSubject, true))
                .switchMap(updateUserInApi())
                .doOnNext(Actions1.progressOnNext(progressSubject, false))
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        successObservable = updateRequest
                .compose(ResponseOrError.<User>onlySuccess());

        /** Upload cover **/
        final Observable<ResponseOrError<File>> coverFileToUploadObservable = lastSelectedCoverUri
                .subscribeOn(networkScheduler)
                .switchMap(transformImage(ImageHelper.MAX_COVER_SIZE))
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<File>>behaviorRefCount());

        final Observable<ResponseOrError<String>> uploadCoverToAmazonObservable = coverFileToUploadObservable
                .compose(ResponseOrError.<File>onlySuccess())
                .filter(Functions1.isNotNull())
                .switchMap(uploadToAmazon())
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        final Observable<ResponseOrError<User>> uploadCoverToApiObservable = uploadCoverToAmazonObservable
                .compose(ResponseOrError.<String>onlySuccess())
                .map(new Func1<String, UpdateUserRequest>() {
                    @Override
                    public UpdateUserRequest call(String imageUrl) {
                        return UpdateUserRequest.updateWithCoverUrl(imageUrl);
                    }
                })
                .switchMap(updateUserInApi())
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        /** Upload avatar **/
        final Observable<ResponseOrError<File>> avatarFileToUploadObservable = lastSelectedAvatarUri
                .subscribeOn(networkScheduler)
                .switchMap(transformImage(ImageHelper.MAX_AVATAR_SIZE))
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<File>>behaviorRefCount());

        final Observable<ResponseOrError<String>> uploadAvatarToAmazonObservable = avatarFileToUploadObservable
                .compose(ResponseOrError.<File>onlySuccess())
                .filter(Functions1.isNotNull())
                .switchMap(uploadToAmazon())
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        final Observable<ResponseOrError<User>> uploadAvatarToApiObservable = uploadAvatarToAmazonObservable
                .compose(ResponseOrError.<String>onlySuccess())
                .map(new Func1<String, UpdateUserRequest>() {
                    @Override
                    public UpdateUserRequest call(String imageUrl) {
                        return UpdateUserRequest.updateWithAvatarUrl(imageUrl);
                    }
                })
                .switchMap(updateUserInApi())
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        /** Success API user update **/
        imageUploadToApiSuccessObservable = Observable.merge(
                uploadCoverToApiObservable.compose(ResponseOrError.<User>onlySuccess()),
                uploadAvatarToApiObservable.compose(ResponseOrError.<User>onlySuccess()),
                successObservable)
                .map(new Func1<User, Object>() {
                    @Override
                    public Object call(User user) {
                        userPreferences.saveUserAsJson(user);
                        return null;
                    }
                })
                .observeOn(uiScheduler);

        /** Progress **/
        avatarProgressObservable = Observable.merge(
                lastSelectedAvatarUri.map(Functions1.returnTrue()),
                avatarFileToUploadObservable.compose(ResponseOrError.<File>onlyError()).map(Functions1.returnFalse()),
                uploadAvatarToAmazonObservable.compose(ResponseOrError.<String>onlyError()).map(Functions1.returnFalse()),
                uploadAvatarToApiObservable.compose(ResponseOrError.<User>onlyError()).map(Functions1.returnFalse()),
                uploadAvatarToApiObservable.compose(ResponseOrError.<User>onlySuccess()).map(Functions1.returnFalse()))
                .observeOn(uiScheduler);

        coverProgressObservable = Observable.merge(
                lastSelectedCoverUri.map(Functions1.returnTrue()),
                coverFileToUploadObservable.compose(ResponseOrError.<File>onlyError()).map(Functions1.returnFalse()),
                uploadCoverToAmazonObservable.compose(ResponseOrError.<String>onlyError()).map(Functions1.returnFalse()),
                uploadCoverToApiObservable.compose(ResponseOrError.<User>onlyError()).map(Functions1.returnFalse()),
                uploadCoverToApiObservable.compose(ResponseOrError.<User>onlySuccess()).map(Functions1.returnFalse()))
                .observeOn(uiScheduler);

        /** Errors **/
        imageUploadError = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(avatarFileToUploadObservable),
                ResponseOrError.transform(uploadAvatarToAmazonObservable),
                ResponseOrError.transform(uploadAvatarToApiObservable),
                ResponseOrError.transform(coverFileToUploadObservable),
                ResponseOrError.transform(uploadCoverToAmazonObservable),
                ResponseOrError.transform(uploadCoverToApiObservable)))
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        updateProfileError = updateRequest.compose(ResponseOrError.<User>onlyError())
                .observeOn(uiScheduler);
    }

    @NonNull
    private Func1<UpdateUserRequest, Observable<ResponseOrError<User>>> updateUserInApi() {
        return new Func1<UpdateUserRequest, Observable<ResponseOrError<User>>>() {
            @Override
            public Observable<ResponseOrError<User>> call(UpdateUserRequest updateUserRequest) {
                return apiService.updateUser(updateUserRequest)
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.<User>toResponseOrErrorObservable());
            }
        };
    }

    @NonNull
    private Func1<File, Observable<ResponseOrError<String>>> uploadToAmazon() {
        return new Func1<File, Observable<ResponseOrError<String>>>() {
            @Override
            public Observable<ResponseOrError<String>> call(File fileToUpload) {
                return amazonHelper.uploadImageObservable(AmazonHelper.AmazonBucket.USER, fileToUpload)
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler);
            }
        };
    }

    @NonNull
    private Func1<Uri, Observable<ResponseOrError<File>>> transformImage(final int maxImageSize) {
        return new Func1<Uri, Observable<ResponseOrError<File>>>() {
            @Override
            public Observable<ResponseOrError<File>> call(Uri imageUri) {
                try {
                    final String tempFile = fileHelper.createTempFileAndStoreUri(imageUri);
                    final Bitmap bitmapToUpload = ImageHelper.prepareImageToUpload(tempFile, maxImageSize);
                    return fileHelper.saveBitmapToTempFileObservable(bitmapToUpload);
                } catch (IOException e) {
                    return Observable.just(ResponseOrError.<File>fromError(e));
                }
            }
        };
    }

    @Nonnull
    public Observable<Boolean> getFirstNameErrorObservable() {
        return saveClickSubject.flatMap(MoreFunctions1.returnObservableFirst(firstNameErrorObservable));
    }

    @Nonnull
    public Observable<Boolean> getLastNameErrorObservable() {
        return saveClickSubject.flatMap(MoreFunctions1.returnObservableFirst(lastNameErrorObservable));
    }

    @Nonnull
    public Observable<Boolean> getUsernameErrorObservable() {
        return saveClickSubject.flatMap(MoreFunctions1.returnObservableFirst(usernameErrorObservable));
    }

    @Nonnull
    public Observable<Throwable> getImageUploadError() {
        return imageUploadError;
    }

    @Nonnull
    public Observable<Throwable> getUpdateProfileError() {
        return updateProfileError;
    }

    @Nonnull
    public Observable<Boolean> getAvatarProgressObservable() {
        return avatarProgressObservable;
    }

    @Nonnull
    public Observable<Boolean> getCoverProgressObservable() {
        return coverProgressObservable;
    }

    @Nonnull
    public Observable<Object> getImageUploadToApiSuccessObservable() {
        return imageUploadToApiSuccessObservable;
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
        return userInputsObservable;
    }

    @Nonnull
    public Observable<UserLocation> getLocationObservable() {
        return locationObservable.observeOn(uiScheduler);
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
    public Observer<String> getFirstNameObserver() {
        return RxMoreObservers.ignoreCompleted(firstNameSubject);
    }

    @Nonnull
    public Observer<String> getLastNameObserver() {
        return RxMoreObservers.ignoreCompleted(lastNameSubject);
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

    public Observer<String> getMobileObserver() {
        return RxMoreObservers.ignoreCompleted(mobileSubject);
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

    public void onLocationChanged(UserLocation userLocation) {
        locationSubject.onNext(userLocation);
    }

    public static class State {
        private UserLocation userLocation;

        public State(UserLocation userLocation) {
            this.userLocation = userLocation;
        }

        public UserLocation getUserLocation() {
            return userLocation;
        }
    }
}