package com.shoutit.app.android.view.chats.chat_info;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.view.chats.public_chat.DaggerCreatePublicChatActivityComponent;
import com.shoutit.app.android.view.createshout.location.LocationActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatInfoActivity extends BaseActivity implements ChatInfoPresenter.CreatePublicChatView {

    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;


    @Bind(R.id.create_chat_progress)
    FrameLayout mCreateChatProgress;
    @Bind(R.id.create_chat_avatar)
    ImageView mCreateChatAvatar;
    @Bind(R.id.create_chat_subject)
    EditText mCreateChatSubject;
    @Bind(R.id.create_chat_location_flag)
    ImageView mCreateChatLocationFlag;
    @Bind(R.id.create_chat_location_name)
    TextView mCreateChatLocationName;
    @Bind(R.id.create_chat_toolbar)
    Toolbar mToolbar;

    @Inject
    Picasso picasso;
    @Inject
    ImageCaptureHelper mImageCaptureHelper;
    @Inject
    ChatInfoPresenter mCreatePublicChatPresenter;

    public static Intent newIntent(Context context) {
        return new Intent(context, ChatInfoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_chat_activity);
        ButterKnife.bind(this);

        mToolbar.setTitle(R.string.create_public_chat_title);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCreatePublicChatPresenter.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCreatePublicChatPresenter.unregister();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ChatInfoComponent build = DaggerCreatePublicChatActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        build.inject(this);
        return build;
    }

    @Override
    public void showProgress(boolean show) {
        mCreateChatProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setLocation(@DrawableRes int flag, @NonNull String location) {
        final Resources resources = getResources();
        final Bitmap bitmap = BitmapFactory.decodeResource(resources, flag);
        final RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
        roundedBitmapDrawable.setCircular(true);
        mCreateChatLocationFlag.setImageDrawable(roundedBitmapDrawable);
        mCreateChatLocationName.setText(location);
    }

    @Override
    public void setImage(@Nullable Uri imageUrl) {
        picasso.load(imageUrl)
                .centerCrop()
                .fit()
                .into(mCreateChatAvatar);
    }

    @Override
    public void startSelectLocationActivity() {
        startActivityForResult(LocationActivity.newIntent(this), REQUEST_LOCATION);
    }

    @Override
    public void startSelectImageActivity() {
        final Optional<Intent> selectOrCaptureImageIntent = mImageCaptureHelper.createSelectOrCaptureImageIntent();
        if (selectOrCaptureImageIntent.isPresent()) {
            startActivityForResult(selectOrCaptureImageIntent.get(), REQUEST_IMAGE);
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), "Error", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION) {
            mCreatePublicChatPresenter.onLocationActivityFinished(resultCode, data);
        } else if (requestCode == REQUEST_IMAGE) {
            mCreatePublicChatPresenter.onImageActivityFinished(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void subjectEmptyError() {
        mCreateChatSubject.setError(getString(R.string.create_public_chat_error));
    }

    @Override
    public ChatInfoPresenter.CreatePublicChatData getData() {
        return new ChatInfoPresenter.CreatePublicChatData(mCreateChatSubject.getText().toString(), false, false);
    }

    @Override
    public void createRequestError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.create_public_chat_error, Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.create_chat_location_change)
    void changeLocation() {
        mCreatePublicChatPresenter.selectLocationClicked();
    }

    @OnClick(R.id.create_chat_create)
    void createChat() {
        mCreatePublicChatPresenter.createClicked();
    }

    @OnClick(R.id.create_chat_avatar)
    void selectAvatar() {
        if (!PermissionHelper.checkPermissions(this, REQUEST_CODE_PERMISSION, ColoredSnackBar.contentView(this),
                R.string.permission_location_explanation,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})) {
            return;
        }

        mCreatePublicChatPresenter.selectImageClicked();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
