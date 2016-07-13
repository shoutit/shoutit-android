package com.shoutit.app.android.view.profile.user.editprofile;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
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
import com.shoutit.app.android.utils.rx.RxUtils;
import com.shoutit.app.android.view.location.LocationActivityForResult;
import com.shoutit.app.android.view.location.LocationHelper;
import com.shoutit.app.android.widget.GenderSpinnerAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Calendar;
import java.util.Date;

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
    @Bind(R.id.edit_profile_first_name_et)
    EditText firstNameEt;
    @Bind(R.id.edit_profile_first_name_til)
    TextInputLayout firstNameInput;
    @Bind(R.id.edit_profile_last_name_til)
    TextInputLayout lastNameInput;
    @Bind(R.id.edit_profile_last_name_et)
    EditText lastNameEt;
    @Bind(R.id.edit_profile_username_et)
    EditText usernameEt;
    @Bind(R.id.edit_profile_username_til)
    TextInputLayout usernameInput;
    @Bind(R.id.edit_profile_bio_et)
    EditText bioEt;
    @Bind(R.id.edit_profile_bio_til)
    TextInputLayout bioInput;
    @Bind(R.id.edit_profile_flag_iv)
    ImageView flagIv;
    @Bind(R.id.edit_profile_location_tv)
    TextView locationTv;
    @Bind(R.id.edit_profile_website_et)
    EditText websiteEt;
    @Bind(R.id.edit_profile_website_til)
    TextInputLayout websiteInput;
    @Bind(R.id.edit_profile_mobile_et)
    EditText mobileEt;
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
    @Bind(R.id.edit_profile_gender_spinner)
    Spinner genderSpinner;
    @Bind(R.id.edit_profile_birthday_tv)
    TextView birthDayTv;

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

        final String[] genders = getResources().getStringArray(R.array.genders);
        final GenderSpinnerAdapter genderAdapter = new GenderSpinnerAdapter(
                this, R.layout.spinner_layout, android.R.layout.simple_dropdown_item_1line, genders);
        genderSpinner.setAdapter(genderAdapter);

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

        RxTextView.textChangeEvents(firstNameEt)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getFirstNameObserver());

        RxTextView.textChangeEvents(lastNameEt)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getLastNameObserver());

        RxTextView.textChangeEvents(usernameEt)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getUserNameObserver());

        RxTextView.textChangeEvents(bioEt)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getBioObserver());

        RxTextView.textChangeEvents(websiteEt)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getWebsiteObserver());

        RxTextView.textChangeEvents(mobileEt)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getMobileObserver());

        RxUtils.spinnerItemClicks(genderSpinner)
                .map(onItemClickEvent -> {
                    final int adapterPosition = onItemClickEvent.position;
                    switch (adapterPosition) {
                        case 0:
                            return User.Gender.MALE.getGenderInApi();
                        case 1:
                            return User.Gender.FEMALE.getGenderInApi();
                        case 2:
                            return User.Gender.OTHER.getGenderInApi();
                        case 3:
                            return User.Gender.NOT_SPECIFIED.getGenderInApi();
                        default:
                            throw new RuntimeException("Unknown gender");
                    }
                })
                .compose(bindToLifecycle())
                .subscribe(presenter.getGenderObserver());

        RxView.clicks(birthDayTv)
                .compose(bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        presenter.showDatePicker();
                    }
                });

        presenter.getShowDatePickerObservable()
                .compose(bindToLifecycle())
                .subscribe(this::showDatePicker);

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
                        startActivityForResult(LocationActivityForResult.newIntent(
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

        presenter.getFirstNameErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.setOrEraseError(firstNameInput, getString(R.string.error_field_empty)));

        presenter.getLastNameErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.setOrEraseError(lastNameInput, getString(R.string.error_field_empty)));

        presenter.getUsernameErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.setOrEraseError(usernameInput, getString(R.string.error_field_empty)));

        presenter.getShowCompleteProfileDialogObservable()
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    showCompleteProfileDialog();
                });

        presenter.getBirthdayObservable()
                .compose(bindToLifecycle())
                .subscribe(birthDay -> {
                    birthDayTv.setText(birthDay);
                });
    }

    private void showDatePicker(long initDate) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(initDate));

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year, monthOfYear, dayOfMonth);
                presenter.birthdayChanged(calendar.getTimeInMillis());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Calendar.DAY_OF_MONTH)
                .show();
    }

    private void showCompleteProfileDialog() {
        if (isFinishing()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage(R.string.edit_profile_complete_profile_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @NonNull
    private Action1<UserLocation> setLocation() {
        return new Action1<UserLocation>() {
            @Override
            public void call(UserLocation userLocation) {
                LocationHelper.setupLocation(userLocation, EditProfileActivity.this, locationTv, flagIv, picasso);
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
        return user -> {
            firstNameEt.setText(user.getFirstName());
            lastNameEt.setText(user.getLastName());
            usernameEt.setText(user.getUsername());
            bioEt.setText(user.getBio());
            websiteEt.setText(user.getWebsite());
            mobileEt.setText(user.getMobile());
            final String gender = user.getGender();
            genderSpinner.setSelection(getAdapterPositionForGender(gender));
            birthDayTv.setText(user.getBirthday());
        };
    }

    private int getAdapterPositionForGender(@Nullable String gender) {
        if (TextUtils.isEmpty(gender)) {
            return 3;
        } else if (gender.equals(User.Gender.MALE.getGenderInApi())){
            return 0;
        }  else if (gender.equals(User.Gender.FEMALE.getGenderInApi())){
            return 1;
        } else if (gender.equals(User.Gender.OTHER.getGenderInApi())){
            return 2;
        } else {
            throw new RuntimeException("Unknwon gender");
        }
    }

    @NonNull
    private Action1<String> loadCover() {
        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                picasso.load(coverUrl)
                        .noPlaceholder()
                        .fit()
                        .centerCrop()
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
                        .noPlaceholder()
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
                R.string.permission_location_explanation,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})) {
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
                final UserLocation userLocation = LocationHelper.getLocationFromIntent(data);
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
            PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_LOCATION, lastLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void finish() {
        setResult(RESULT_OK, null);
        super.finish();
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
