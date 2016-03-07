package com.shoutit.app.android.view.editprofile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.utils.rx.Actions1;
import com.shoutit.app.android.view.createshout.location.LocationActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class EditProfileActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final int REQUEST_CODE_LOCATION = 2;
    private static final int REQUEST_CODE_CAPTURE_IMAGE_FOR_AVATAR = 3;
    private static final int REQUEST_CODE_CAPTURE_IMAGE_FOR_COVER = 4;
    private static final String KEY_LOCATION = "key_location";

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
    TextInputLayout usernameInput;
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
    @Bind(R.id.edit_profile_change_location_tv)
    TextView changeLocationTv;
    @Bind(R.id.edit_profile_cover_photo_icon_iv)
    ImageView coverPhotoIconIv;
    @Bind(R.id.edit_profile_cover_progressbar)
    ProgressBar coverProgressbar;
    @Bind(R.id.edit_profile_avatar_photo_icon_iv)
    ImageView avatarPhotoIconIv;
    @Bind(R.id.edit_profile_avatar_progressbar)
    ProgressBar avatarProgressbar;


    @Inject
    EditProfilePresenter presenter;
    @Inject
    Picasso picasso;
    @Inject
    ImageCaptureHelper coverCaptureHelper;
    @Inject
    ImageCaptureHelper avatarCaptureHelper;

    private UserLocation lastLocation;


    public static Intent newIntent(Context context) {
        return new Intent(context, EditProfileActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        setUpToolbar();

        presenter.getUserObservable()
                .compose(this.<User>bindToLifecycle())
                .subscribe(setUserData());

        presenter.getLocationObservable()
                .compose(this.<UserLocation>bindToLifecycle())
                .subscribe(setLocation());

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
                .subscribe(new CaptureImageAction(REQUEST_CODE_CAPTURE_IMAGE_FOR_AVATAR));

        RxView.clicks(coverSelectorView)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new CaptureImageAction(REQUEST_CODE_CAPTURE_IMAGE_FOR_COVER));

        RxView.clicks(changeLocationTv)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startActivityForResult(LocationActivity.newIntent(
                                EditProfileActivity.this),
                                REQUEST_CODE_LOCATION);
                    }
                });

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

        presenter.getAvatarProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean showProgress) {
                        avatarProgressbar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
                        avatarPhotoIconIv.setVisibility(showProgress ? View.GONE : View.VISIBLE);
                    }
                });

        presenter.getCoverProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean showProgress) {
                        coverProgressbar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
                        coverPhotoIconIv.setVisibility(showProgress ? View.GONE : View.VISIBLE);
                    }
                });

        presenter.getImageUploadToApiSuccessObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Toast.makeText(EditProfileActivity.this, R.string.edit_profile_success, Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.getImageUploadError()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this), R.string.error_image_upload));

        presenter.getUpdateProfileError()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getNameErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.setOrEraseError(nameInput, getString(R.string.error_field_empty)));

        presenter.getUsernameErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.setOrEraseError(usernameInput, getString(R.string.error_field_empty)));
    }

    @NonNull
    private Action1<UserLocation> setLocation() {
        return new Action1<UserLocation>() {
            @Override
            public void call(UserLocation userLocation) {
                lastLocation = userLocation;
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
        };
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
            }
        };
    }

    @NonNull
    private Action1<String> loadCover() {
        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                picasso.load(coverUrl)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.pattern_placeholder)
                        .error(R.drawable.pattern_placeholder)
                        .into(coverIv);
            }
        };
    }

    @NonNull
    private Action1<String> loadAvatar() {
        final int strokeSize = getResources().getDimensionPixelSize(R.dimen.profile_avatar_stroke);
        final int corners = getResources().getDimensionPixelSize(R.dimen.profile_avatar_radius);

        return new Action1<String>() {
            @Override
            public void call(String url) {
                picasso.load(url)
                        .placeholder(R.drawable.ic_rect_avatar_placeholder)
                        .error(R.drawable.ic_rect_avatar_placeholder)
                        .fit()
                        .centerCrop()
                        .transform(PicassoHelper.roundedWithStrokeTransformation(strokeSize, false, corners, "ProfileAvatar"))
                        .into(avatarIv);
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
        if (!PermissionHelper.checkPermissions(this, REQUEST_CODE_PERMISSION, ColoredSnackBar.contentView(this),
                R.string.permission_location_explanation, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            return;
        }

        final Optional<Intent> captureIntent;
        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE_FOR_AVATAR) {
            captureIntent = avatarCaptureHelper.createSelectOrCaptureImageIntent();
        } else if (requestCode == REQUEST_CODE_CAPTURE_IMAGE_FOR_COVER) {
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

        if (resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE_FOR_AVATAR:
                uri = avatarCaptureHelper.onResult(resultCode, data);
                if (uri.isPresent()) {
                    presenter.getLastSelectedAvatarUriObserver().onNext(uri.get());
                }
                break;
            case REQUEST_CODE_CAPTURE_IMAGE_FOR_COVER:
                uri = coverCaptureHelper.onResult(resultCode, data);
                if (uri.isPresent()) {
                    presenter.getLastSelectedCoverUriObserver().onNext(uri.get());
                }
                break;
            case REQUEST_CODE_LOCATION:
                final UserLocation userLocation = (UserLocation) data.getSerializableExtra(LocationActivity.EXTRAS_USER_LOCATION);
                presenter.onLocationChanged(userLocation);
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
        if (requestCode == REQUEST_CODE_PERMISSION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(ColoredSnackBar.contentView(this),
                        R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
            } else {
                ColoredSnackBar.error(ColoredSnackBar.contentView(this),
                        R.string.permission_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_LOCATION, lastLocation);
        super.onSaveInstanceState(outState);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            lastLocation = (UserLocation) savedInstanceState.getSerializable(KEY_LOCATION);
        }

        final EditProfileActivityComponent component = DaggerEditProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .editProfileActivityModule(new EditProfileActivityModule(savedInstanceState == null ?
                        null : new EditProfilePresenter.State(lastLocation)))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
