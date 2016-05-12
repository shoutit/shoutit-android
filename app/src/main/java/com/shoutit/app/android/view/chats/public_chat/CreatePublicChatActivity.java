package com.shoutit.app.android.view.chats.public_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.view.createshout.location.LocationActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreatePublicChatActivity extends BaseActivity implements CreatePublicChatPresenter.CreatePublicChatView {

    private static final int REQUEST_LOCATION = 0;

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
    @Bind(R.id.create_chat_location_change)
    Button mCreateChatLocationChange;
    @Bind(R.id.create_chat_location_create)
    Button mCreateChatLocationCreate;

    @Inject
    Picasso picasso;
    @Inject
    ImageCaptureHelper mImageCaptureHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_chat_activity);
        ButterKnife.bind(this);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void showProgress(boolean show) {
        mCreateChatProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setLocation(@DrawableRes int flag, @NonNull String location) {
        mCreateChatLocationFlag.setImageResource(flag);
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
            startActivityForResult(selectOrCaptureImageIntent.get(), REQUEST_LOCATION);
        }
    }

    @Override
    public void subjectEmptyError() {
        mCreateChatSubject.setError("error");
    }

    @Override
    public CreatePublicChatPresenter.CreatePublicChatData getData() {
        return new CreatePublicChatPresenter.CreatePublicChatData(mCreateChatSubject.getText().toString(), false, false);
    }

    @Override
    public void createRequestError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), "Error", Snackbar.LENGTH_SHORT).show();
    }
}
