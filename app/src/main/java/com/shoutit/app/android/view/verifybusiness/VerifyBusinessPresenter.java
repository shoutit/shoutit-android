package com.shoutit.app.android.view.verifybusiness;

import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.VerifyBusinessRequest;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.FileHelper;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.Validators;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class VerifyBusinessPresenter {

    private final BehaviorSubject<Integer> lastSelectedPosition = BehaviorSubject.create();

    private final BiMap<Integer, Item> mediaItems = HashBiMap.create(ImmutableMap.of(
            0, new AddImageItem(),
            1, new BlankItem(),
            2, new BlankItem()
    ));

    private final FileHelper fileHelper;
    @NonNull
    private final AmazonHelper amazonHelper;
    @NonNull
    private final Scheduler uiScheduler;
    @NonNull
    private final Scheduler networkScheduler;
    private final Resources resources;
    @NonNull
    private final ApiService apiService;

    private Listener listener;
    @NonNull
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public VerifyBusinessPresenter(@NonNull FileHelper fileHelper,
                                   @NonNull AmazonHelper amazonHelper,
                                   @NonNull @UiScheduler Scheduler uiScheduler,
                                   @NonNull @NetworkScheduler Scheduler networkScheduler,
                                   @ForActivity Resources resources,
                                   @NonNull ApiService apiService) {

        this.fileHelper = fileHelper;
        this.amazonHelper = amazonHelper;
        this.uiScheduler = uiScheduler;
        this.networkScheduler = networkScheduler;
        this.resources = resources;
        this.apiService = apiService;
    }

    public void register(@NonNull Listener listener) {
        this.listener = listener;
        listener.setImages(mediaItems);
    }

    public void unregister() {
        listener = null;
        subscriptions.unsubscribe();
    }

    private int getFirstAvailablePositionAndCheck() {
        final Integer firstAvailablePosition = getFirstAvailablePosition();
        Preconditions.checkNotNull(firstAvailablePosition);
        return firstAvailablePosition;
    }

    private Integer getFirstAvailablePosition() {
        for (int i = 0; i < mediaItems.size(); i++) {
            if (mediaItems.get(i) instanceof AddImageItem) {
                return i;
            }
        }
        return null;
    }

    public void removeItem(int position) {
        removeItem(mediaItems.get(position));
    }

    private void removeItem(@NonNull Item mediaItem) {
        final Integer firstAvailablePosition = MoreObjects.firstNonNull(getFirstAvailablePosition(), mediaItems.values().size());
        final Integer position = mediaItems.inverse().get(mediaItem);

        for (int i = position + 1; i < firstAvailablePosition; i++) {
            final Item item = mediaItems.get(i);
            mediaItems.forcePut(i - 1, item);
        }
        mediaItems.forcePut(firstAvailablePosition - 1, new AddImageItem());

        for (int j = firstAvailablePosition; j < mediaItems.size(); j++) {
            mediaItems.put(j, new BlankItem());
        }

        listener.setImages(mediaItems);
    }

    public void swapImage(int position, String newUrl) {
        mediaItems.remove(position);
        mediaItems.forcePut(position, new ImageItem(newUrl));
        listener.setImages(mediaItems);
    }

    public void startImageChooser(int position) {
        lastSelectedPosition.onNext(position);
        listener.showProgress();
        listener.startImageChooser();
    }

    public void uploadImageToAmazon(Uri uri) {
        listener.showProgress();

        final Observable<ResponseOrError<File>> prepareFileToUpload = Observable.just(uri)
                .subscribeOn(networkScheduler)
                .switchMap(fileHelper.scaleAndCompressImage(ImageHelper.DEFAULT_MAX_IMAGE_SIZE))
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<File>>behaviorRefCount());

        final Observable<ResponseOrError<String>> uploadAvatarToAmazon = prepareFileToUpload
                .compose(ResponseOrError.<File>onlySuccess())
                .filter(Functions1.isNotNull())
                .switchMap(uploadToAmazon())
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        subscriptions.add(
                uploadAvatarToAmazon
                        .compose(ResponseOrError.onlySuccess())
                        .withLatestFrom(lastSelectedPosition, BothParams::of)
                        .subscribe(imageUrlWithItemPosition -> {
                            listener.hideProgress();
                            swapImage(imageUrlWithItemPosition.param2(), imageUrlWithItemPosition.param1());
                        })
        );

        subscriptions.add(
                Observable.merge(
                        prepareFileToUpload.compose(ResponseOrError.onlyError()),
                        uploadAvatarToAmazon.compose(ResponseOrError.onlyError()))
                        .subscribe(throwable -> {
                            listener.hideProgress();
                            listener.showError(throwable);
                        })
        );
    }

    public void submitForm(String name, String person, String number, String email) {
        if (!areDateValid(name, person, number, email)) {
            return;
        }

        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (Item item : mediaItems.values()) {
            if (item instanceof ImageItem) {
                builder.add(((ImageItem) item).media);
            }
        }

        listener.showProgress();

        subscriptions.add(
                apiService.verifyBusiness(new VerifyBusinessRequest(
                        name, email, person, number, builder.build()))
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable())
                        .subscribe(response -> {
                            listener.hideProgress();
                            if (response.isData()) {
                                listener.showSuccessAndFinish();
                            } else {
                                listener.showError(response.error());
                            }
                        })
        );
    }

    private boolean areDateValid(String name, String person, String number, String email) {
        final boolean isNameValid = Strings.isNullOrEmpty(name);
        listener.showNameError(isNameValid ? null : resources.getString(R.string.error_field_empty));

        final boolean isPersonValid = Strings.isNullOrEmpty(person);
        listener.showPersonError(isPersonValid ? null : resources.getString(R.string.error_field_empty));

        final boolean isNumberValid = Strings.isNullOrEmpty(number);
        listener.showNumberError(isNumberValid ? null : resources.getString(R.string.error_field_empty));

        final boolean isEmailValid = Validators.isEmailValid(email);
        listener.showEmailError(isEmailValid ? null : resources.getString(R.string.error_wrong_email));

        boolean hasAtLeastOneImage = false;
        for (Item item : mediaItems.values()) {
            if (item instanceof ImageItem && !TextUtils.isEmpty(((ImageItem) item).media)) {
                hasAtLeastOneImage = true;
            }
        }

        if (!hasAtLeastOneImage) {
            listener.showError(resources.getString(R.string.business_no_images_error));
        }

        return isNameValid && isPersonValid && isNumberValid && isEmailValid && hasAtLeastOneImage;
    }

    @NonNull
    private Func1<File, Observable<ResponseOrError<String>>> uploadToAmazon() {
        return fileToUpload
                -> amazonHelper.uploadUserImageObservable(fileToUpload)
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ResponseOrError.<String>toResponseOrErrorObservable());
    }

    public class ImageItem implements Item {

        @NonNull
        private final String media;

        public ImageItem(@NonNull String media) {
            this.media = media;
        }

        public void click() {
            showEditImageDialog(mediaItems.inverse().get(this));
        }

        @NonNull
        public String getMedia() {
            return media;
        }
    }

    public class BlankItem implements Item {

        @Override
        public void click() {
        }
    }

    public interface Item {
        void click();
    }

    public class AddImageItem implements Item {

        @Override
        public void click() {
            listener.startImageChooser(getFirstAvailablePositionAndCheck());
        }
    }

    private void showEditImageDialog(int position) {
        listener.showImageDialog(position);
    }

    public interface Listener {

        void setImages(@NonNull Map<Integer, Item> mediaElements);

        void startImageChooser(int position);

        void showProgress();

        void hideProgress();

        void showImageDialog(int position);

        void startImageChooser();

        void showError(Throwable throwable);

        void showError(String message);

        void showSuccessAndFinish();

        void showNameError(String error);

        void showPersonError(String error);

        void showNumberError(String error);

        void showEmailError(String error);
    }
}
