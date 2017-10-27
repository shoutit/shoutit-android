package com.shoutit.app.android.view.profile.page.edit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.ToolbarUtils;
import com.shoutit.app.android.view.location.LocationActivityForResult;
import com.shoutit.app.android.view.location.LocationHelper;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.google.common.base.Preconditions.checkNotNull;

public class EditPageActivity extends BaseActivity implements EditPagePresenter.Listener {

    private static final String EXTRA_USERNAME = "extra_username";
    @Bind(R.id.edit_page_flag_iv)
    ImageView mEditPageFlagIv;
    @Bind(R.id.edit_page_location_tv)
    TextView mEditPageLocationTv;

    public static Intent newIntent(Context context, @Nonnull String username) {
        return new Intent(context, EditPageActivity.class).putExtra(EXTRA_USERNAME, username);
    }

    private static final int REQUEST_CODE_CAPTURE_IMAGE_FOR_AVATAR = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE_FOR_COVER = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;
    private static final int REQUEST_CODE_LOCATION = 3;

    @Inject
    EditPagePresenter mEditPagePresenter;

    @Inject
    ImageCaptureHelper coverCaptureHelper;
    @Inject
    ImageCaptureHelper avatarCaptureHelper;
    @Inject
    Picasso mPicasso;

    @Bind(R.id.edit_page_cover_iv)
    ImageView mEditPageCoverIv;
    @Bind(R.id.edit_page_cover_photo_icon_iv)
    ImageView mEditPageCoverPhotoIconIv;
    @Bind(R.id.edit_page_cover_progressbar)
    ProgressBar mEditPageCoverProgressbar;
    @Bind(R.id.edit_page_cover_container)
    FrameLayout mEditPageCoverContainer;
    @Bind(R.id.edit_page_name_et)
    EditText mEditPageNameEt;
    @Bind(R.id.edit_page_name_til)
    TextInputLayout mEditPageNameTil;
    @Bind(R.id.edit_page_username_et)
    EditText mUsernameEt;
    @Bind(R.id.edit_page_username_til)
    TextInputLayout mUsernameTil;
    @Bind(R.id.edit_page_website_et)
    EditText mWebsiteEt;
    @Bind(R.id.edit_page_website_til)
    TextInputLayout mWebsiteTil;
    @Bind(R.id.edit_page_about_et)
    EditText mEditPageAboutEt;
    @Bind(R.id.edit_page_about_til)
    TextInputLayout mEditPageAboutTil;
    @Bind(R.id.edit_page_published)
    CheckBox mEditPagePublished;
    @Bind(R.id.edit_page_description_et)
    EditText mEditPageDescriptionEt;
    @Bind(R.id.edit_page_description_til)
    TextInputLayout mEditPageDescriptionTil;
    @Bind(R.id.edit_page_phone_et)
    EditText mEditPagePhoneEt;
    @Bind(R.id.edit_page_phone_til)
    TextInputLayout mEditPagePhoneTil;
    @Bind(R.id.edit_page_founded_et)
    EditText mEditPageFoundedEt;
    @Bind(R.id.edit_page_founded_til)
    TextInputLayout mEditPageFoundedTil;
    @Bind(R.id.edit_page_impressum_et)
    EditText mEditPageImpressumEt;
    @Bind(R.id.edit_page_impressum_til)
    TextInputLayout mEditPageImpressumTil;
    @Bind(R.id.edit_page_overview_et)
    EditText mEditPageOverviewEt;
    @Bind(R.id.edit_page_overview_til)
    TextInputLayout mEditPageOverviewTil;
    @Bind(R.id.edit_page_mission_et)
    EditText mEditPageMissionEt;
    @Bind(R.id.edit_page_mission_til)
    TextInputLayout mEditPageMissionTil;
    @Bind(R.id.edit_page_general_info_et)
    EditText mEditPageGeneralInfoEt;
    @Bind(R.id.edit_page_general_info_til)
    TextInputLayout mEditPageGeneralInfoTil;
    @Bind(R.id.edit_page_avatar_iv)
    ImageView mEditPageAvatarIv;
    @Bind(R.id.edit_page_avatar_photo_icon_iv)
    ImageView mEditPageAvatarPhotoIconIv;
    @Bind(R.id.edit_page_avatar_progressbar)
    ProgressBar mEditPageAvatarProgressbar;
    @Bind(R.id.edit_page_toolbar)
    Toolbar mEditPageToolbar;
    @Bind(R.id.base_progress)
    FrameLayout mBaseProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_page);
        ButterKnife.bind(this);

        ToolbarUtils.setupToolbar(mEditPageToolbar, R.string.edit_page_title, this);

        mEditPagePresenter.register(this);

        mEditPageToolbar.inflateMenu(R.menu.menu_edit_profile);
        mEditPageToolbar.setOnMenuItemClickListener(item -> {
            mEditPagePresenter.editFinished(new EditPagePresenter.EditData(
                    mEditPageNameEt.getText().toString(),
                    mUsernameEt.getText().toString(),
                    mWebsiteEt.getText().toString(),
                    mEditPageAboutEt.getText().toString(),
                    mEditPagePublished.isChecked(),
                    mEditPageDescriptionEt.getText().toString(),
                    mEditPagePhoneEt.getText().toString(),
                    mEditPageFoundedEt.getText().toString(),
                    mEditPageImpressumEt.getText().toString(),
                    mEditPageOverviewEt.getText().toString(),
                    mEditPageMissionEt.getText().toString(),
                    mEditPageGeneralInfoEt.getText().toString()));
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditPagePresenter.unregister();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String username = checkNotNull(getIntent().getStringExtra(EXTRA_USERNAME));

        final EditPageActivityComponent build = DaggerEditPageActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .editPageActivityModule(new EditPageActivityModule(username))
                .build();
        build.inject(this);
        return build;
    }

    @Override
    public void setGeneralInfo(String generalInfo) {
        mEditPageGeneralInfoEt.setText(generalInfo);
    }

    @Override
    public void setMission(String mission) {
        mEditPageMissionEt.setText(mission);
    }

    @Override
    public void setOverview(String overview) {
        mEditPageOverviewEt.setText(overview);
    }

    @Override
    public void setImpressum(String impressum) {
        mEditPageImpressumEt.setText(impressum);
    }

    @Override
    public void setFounded(String founded) {
        mEditPageFoundedEt.setText(founded);
    }

    @Override
    public void setPhone(String phone) {
        mEditPagePhoneEt.setText(phone);
    }

    @Override
    public void setDescription(String description) {
        mEditPageDescriptionEt.setText(description);
    }

    @Override
    public void setPublished(boolean published) {
        mEditPagePublished.setChecked(published);
    }

    @Override
    public void setAbout(String about) {
        mEditPageAboutEt.setText(about);
    }

    @Override
    public void setName(String name) {
        mEditPageNameEt.setText(name);
    }

    @Override
    public void setUsername(String username) {
        mUsernameEt.setText(username);
    }

    @Override
    public void setWebsite(String website) {
        mWebsiteEt.setText(website);
    }

    @Override
    public void setProgress(boolean show) {
        mBaseProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setAvatar(String url) {
        mPicasso.load(url)
                .noPlaceholder()
                .error(R.drawable.default_page)
                .centerCrop()
                .fit()
                .into(mEditPageAvatarIv);
    }

    @Override
    public void finishAndSetResult() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void error(Throwable throwable) {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), throwable).show();
    }

    @Override
    public void setCover(String data) {
        mPicasso.load(data)
                .centerCrop()
                .fit()
                .into(mEditPageCoverIv);
    }

    @Override
    public void setUpLocation(UserLocation location) {
        LocationHelper.setupLocation(location, EditPageActivity.this, mEditPageLocationTv, mEditPageFlagIv, mPicasso);
    }

    @Override
    public void imageUploadError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_image_upload, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE_FOR_AVATAR:
                mEditPagePresenter.avatarChosen(avatarCaptureHelper.onResult(resultCode, data));
                break;
            case REQUEST_CODE_CAPTURE_IMAGE_FOR_COVER:
                mEditPagePresenter.coverChosen(avatarCaptureHelper.onResult(resultCode, data));
                break;
            case REQUEST_CODE_LOCATION:
                final UserLocation userLocation = LocationHelper.getLocationFromIntent(data);
                mEditPagePresenter.onLocationChanged(userLocation);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.edit_page_selector_view)
    void selectAvatar() {
        if (!checkPermission()) return;

        final Optional<Intent> captureIntent = avatarCaptureHelper.createSelectOrCaptureImageIntent();

        if (captureIntent.isPresent()) {
            startActivityForResult(captureIntent.get(), REQUEST_CODE_CAPTURE_IMAGE_FOR_AVATAR);
        }
    }

    @OnClick(R.id.edit_page_cover_selector)
    void selectCover() {
        if (!checkPermission()) return;

        final Optional<Intent> captureIntent = coverCaptureHelper.createSelectOrCaptureImageIntent();

        if (captureIntent.isPresent()) {
            startActivityForResult(captureIntent.get(), REQUEST_CODE_CAPTURE_IMAGE_FOR_COVER);
        }
    }

    private boolean checkPermission() {
        return PermissionHelper.checkPermissions(this, REQUEST_CODE_PERMISSION, ColoredSnackBar.contentView(this),
                R.string.permission_location_explanation,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
    }

    @OnClick(R.id.edit_page_change_location_tv)
    void startSelectLocation() {
        startActivityForResult(LocationActivityForResult.newIntent(
                EditPageActivity.this),
                REQUEST_CODE_LOCATION);
    }
}