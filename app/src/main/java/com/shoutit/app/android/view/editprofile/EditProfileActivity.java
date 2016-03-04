package com.shoutit.app.android.view.editprofile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.FileHelper;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class EditProfileActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_FOR_AVATAR = 1;
    private static final int CAPTURE_IMAGE_FOR_COVER = 2;
    private static final String KEY_CAPTURE_IMAGE_FOR_AVATAR = "avatar";
    private static final String KEY_CAPTURE_IMAGE_FOR_COVER = "cover";

    @Bind(R.id.edit_profile_toolbar)
    Toolbar toolbar;
    @Bind(R.id.edit_profile_cover_iv)
    ImageView coverIv;
    @Bind(R.id.edit_profile_name_et)
    EditText nameEt;
    @Bind(R.id.edit_profile_name_til)
    TextInputLayout nameInput;
    @Bind(R.id.edit_profile_username_et)
    EditText usernameEt;
    @Bind(R.id.edit_profile_username_til)
    TextInputLayout usernameTil;
    @Bind(R.id.edit_profile_bio_et)
    EditText bioEt;
    @Bind(R.id.edit_profile_bio_til)
    TextInputLayout bioTil;
    @Bind(R.id.edit_profile_flag_iv)
    ImageView flagIv;
    @Bind(R.id.edit_profile_location_tv)
    TextView locationTv;
    @Bind(R.id.edit_profile_website_et)
    EditText websiteEt;
    @Bind(R.id.edit_profile_website_til)
    TextInputLayout websiteTil;
    @Bind(R.id.edit_profile_avatar_iv)
    ImageView avatarIv;
    @Bind(R.id.edit_profile_cover_selector)
    View coverSelectorView;
    @Bind(R.id.edit_profile_selector_view)
    View avatarSelectorView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    EditProfilePresenter presenter;
    @Inject
    Picasso picasso;
    @Inject
    ImageCaptureHelper coverCaptureHelper;
    @Inject
    ImageCaptureHelper avatarCaptureHelper;
    @Inject
    FileHelper fileHelper;

    private String lastSelectedAvatarPath;
    private String lastSelectedCoverPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        setUpToolbar();

        if (savedInstanceState == null) {
            lastSelectedAvatarPath = savedInstanceState.getString(KEY_CAPTURE_IMAGE_FOR_AVATAR);
            presenter.getLastSelectedAvatarUriObserver().onNext(lastSelectedAvatarPath);
            lastSelectedCoverPath = savedInstanceState.getString(KEY_CAPTURE_IMAGE_FOR_COVER);
            presenter.getLastSelectedCoverUriObserver().onNext(lastSelectedCoverPath);
        }

        presenter.getUserObservable()
                .compose(this.<User>bindToLifecycle())
                .subscribe(setUserData());

        presenter.getAvatarObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(loadAvatar());

        presenter.getCoverObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(loadCover());

        RxTextView.textChangeEvents(nameEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getNameObserver());

        RxTextView.textChangeEvents(usernameEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getUserNameObserver());

        RxTextView.textChangeEvents(bioEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getBioObserver());

        RxTextView.textChangeEvents(websiteEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getWebsiteObserver());

        RxView.clicks(avatarSelectorView)
                .compose(bindToLifecycle())
                .subscribe(new CaptureImageAction(CAPTURE_IMAGE_FOR_AVATAR));

        RxView.clicks(coverSelectorView)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new CaptureImageAction(CAPTURE_IMAGE_FOR_COVER));

        presenter.getSuccessObservable()
                .compose(this.<User>bindToLifecycle())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        finish();
                    }
                });

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.edit_profile_menu_save:
                presenter.onSaveClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @NonNull
    private Action1<User> setUserData() {
        return new Action1<User>() {
            @Override
            public void call(User user) {
                nameEt.setText(user.getName());
                usernameEt.setText(user.getUsername());
                bioEt.setText(user.getBio());
                websiteEt.setText(user.getWebUrl());

                final UserLocation userLocation = user.getLocation();
                if (userLocation != null) {
                    locationTv.setText(getString(R.string.edit_profile_country,
                            Strings.nullToEmpty(userLocation.getCity()),
                            Strings.nullToEmpty(userLocation.getCountry())));

                    final Optional<Integer> countryResId = ResourcesHelper
                            .getCountryResId(EditProfileActivity.this, userLocation);
                    final Target flagTarget = PicassoHelper
                            .getRoundedBitmapTarget(EditProfileActivity.this, flagIv);
                    if (countryResId.isPresent()) {
                        picasso.load(countryResId.get())
                                .into(flagTarget);
                    }
                }
            }
        };
    }

    @NonNull
    private Action1<String> loadCover() {
        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                final Target avatarTarget = PicassoHelper.getRoundedBitmapWithStrokeTarget(
                        avatarIv, getResources().getDimensionPixelSize(R.dimen.profile_avatar_stroke),
                        false, getResources().getDimensionPixelSize(R.dimen.profile_avatar_radius));
                picasso.load(coverUrl)
                        .fit()
                        .centerCrop()
                        .into(avatarTarget);
            }
        };
    }

    @NonNull
    private Action1<String> loadAvatar() {
        return new Action1<String>() {
            @Override
            public void call(String avatarUri) {
                picasso.load(avatarUri)
                        .fit()
                        .centerCrop()
                        .into(coverIv);
            }
        };
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.edit_profile_toolbar_title);
    }

    private void captureImage(int requestCode) {
        if (!PermissionHelper.checkPermissions(this, PERMISSION_REQUEST_CODE, ColoredSnackBar.contentView(this),
                R.string.permission_location_explanation, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            return;
        }

        final Optional<Intent> captureIntent;
        if (requestCode == CAPTURE_IMAGE_FOR_AVATAR) {
            captureIntent = avatarCaptureHelper.createSelectOrCaptureImageIntent();
        } else if (requestCode == CAPTURE_IMAGE_FOR_COVER) {
            captureIntent = coverCaptureHelper.createSelectOrCaptureImageIntent();
        } else {
            throw new RuntimeException("Unkonwn request code: " + requestCode);
        }

        if (captureIntent.isPresent()) {
            startActivityForResult(captureIntent.get(), requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Optional<Uri> uri;

        switch (requestCode) {
            case CAPTURE_IMAGE_FOR_AVATAR:
                uri = avatarCaptureHelper.onResult(resultCode, data);
                if (uri.isPresent()) {
                    try {
                        presenter.getLastSelectedAvatarUriObserver().onNext(uri.get());
                    } catch (IOException e) {
                        presenter.imageChooseErrorObserver().onNext(null);
                    }
                }
                break;
            case CAPTURE_IMAGE_FOR_COVER:
                uri = coverCaptureHelper.onResult(resultCode, data);
                if (uri.isPresent()) {
                    try {
                        presenter.getLastSelectedAvatarUriObserver().onNext(uri.get());
                    } catch (IOException e) {
                        presenter.imageChooseErrorObserver().onNext(null);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class CaptureImageAction implements Action1<Object> {
        private final int type;

        public CaptureImageAction(int captureType) {
            this.type = captureType;
        }

        @Override
        public void call(Object o) {
            captureImage(type);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(ColoredSnackBar.contentView(this),
                        R.string.permission_granted, Snackbar.LENGTH_SHORT);
            } else {
                ColoredSnackBar.success(ColoredSnackBar.contentView(this),
                        R.string.permission_not_granted, Snackbar.LENGTH_SHORT);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        avatarCaptureHelper.onSaveInstanceState(outState, KEY_CAPTURE_IMAGE_FOR_AVATAR);
        coverCaptureHelper.onSaveInstanceState(outState, KEY_CAPTURE_IMAGE_FOR_COVER);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final EditProfileActivityComponent component = DaggerEditProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
