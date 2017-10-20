package com.shoutit.app.android.view.chats.chat_info;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.view.ReportDialog;
import com.shoutit.app.android.view.chats.chat_info.chats_participants.ChatParticipantsActivity;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_blocked.ChatBlockedUsersActivity;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_select.ChatSelectUsersActivity;
import com.shoutit.app.android.view.chats.chat_media_gallery.ChatMediaGalleryActivity;
import com.shoutit.app.android.view.chats.chat_shouts.ChatShoutsActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatInfoActivity extends BaseActivity implements ChatInfoPresenter.ChatInfoView {

    private static final int REQUEST_IMAGE = 0;
    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_CLOSE_CHAT = "extra_close_chat";

    @Inject
    Picasso picasso;
    @Inject
    ImageCaptureHelper mImageCaptureHelper;
    @Inject
    ChatInfoPresenter mChatInfoPresenter;
    @Bind(R.id.chat_info_toolbar)
    Toolbar mChatInfoToolbar;
    @Bind(R.id.chat_info_avatar)
    ImageView mChatInfoAvatar;
    @Bind(R.id.chat_info_subject_edittext)
    EditText mChatInfoSubject;
    @Bind(R.id.chat_info_subject_textview)
    TextView mChatInfoSubjectTextView;
    @Bind(R.id.chat_info_shouts_number)
    TextView mChatInfoShoutsNumber;
    @Bind(R.id.chat_info_media_number)
    TextView mChatInfoMediaNumber;
    @Bind(R.id.chat_info_participants)
    TextView mChatInfoParticipants;
    @Bind(R.id.chat_info_blocked_number)
    TextView mChatInfoBlockedNumber;
    @Bind(R.id.chat_info_exit_chat)
    Button mChatInfoEditChat;
    @Bind(R.id.chat_info_progress)
    FrameLayout mChatInfoProgress;
    @Bind(R.id.chat_info_subject_layout)
    View mChatInfoSubjectLayout;
    @Bind(R.id.chat_info_chat_created_by)
    TextView mChatInfoChatCreatedBy;
    @Bind(R.id.chat_info_chat_created_at)
    TextView mChatInfoChatCreatedAt;
    @Bind(R.id.chat_info_edit_save)
    Button mChatInfoEditSave;

    private String conversationId;

    public static Intent newIntent(@NonNull Context context, @NonNull String conversationId) {
        Preconditions.checkNotNull(conversationId);
        return new Intent(context, ChatInfoActivity.class)
                .putExtra(EXTRA_CONVERSATION_ID, conversationId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        Preconditions.checkNotNull(conversationId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_info_activity);
        ButterKnife.bind(this);

        mChatInfoToolbar.setTitle(R.string.chat_info_title);
        mChatInfoToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        mChatInfoToolbar.setNavigationOnClickListener(view -> finish());
        mChatInfoToolbar.inflateMenu(R.menu.chat_info_menu);
        mChatInfoToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.chat_info_add_person: {
                    startActivity(ChatSelectUsersActivity.newIntent(ChatInfoActivity.this, conversationId));
                    return true;
                }
                case R.id.chats_info_report: {
                    ReportDialog.show(ChatInfoActivity.this, reportBody -> mChatInfoPresenter.sendReport(reportBody));
                }
                default:
                    return false;
            }
        });

        mChatInfoPresenter.register(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mChatInfoPresenter.refreshCounts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatInfoPresenter.unregister();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ChatInfoComponent build = DaggerChatInfoComponent.builder()
                .activityModule(new ActivityModule(this))
                .chatInfoModule(new ChatInfoModule(conversationId))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        build.inject(this);
        return build;
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
    public void showProgress(boolean show) {
        mChatInfoProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setImage(@android.support.annotation.Nullable Uri imageUrl) {
        picasso.load(imageUrl)
                .centerCrop()
                .fit()
                .into(mChatInfoAvatar);
    }

    @Override
    public void setSubject(@Nonnull String subject) {
        mChatInfoSubject.setText(subject);
        mChatInfoSubjectTextView.setText(subject);
    }

    @Override
    public void startSelectImageActivity() {
        final Optional<Intent> selectOrCaptureImageIntent = mImageCaptureHelper.createSelectOrCaptureImageIntent();
        if (selectOrCaptureImageIntent.isPresent()) {
            startActivityForResult(selectOrCaptureImageIntent.get(), REQUEST_IMAGE);
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.chat_info_select_image_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            mChatInfoPresenter.onImageActivityFinished(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void subjectEmptyError() {
        mChatInfoSubject.setError(getString(R.string.create_public_chat_error));
    }

    @Override
    public String getSubject() {
        return mChatInfoSubject.getText().toString();
    }

    @Override
    public void finishScreen(boolean closeChat) {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_CLOSE_CHAT, closeChat));
        finish();
    }

    @Override
    public void editRequestError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.chat_info_edit_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setShoutsCount(int count) {
        mChatInfoShoutsNumber.setText(String.valueOf(count));
    }

    @Override
    public void setMediaCount(int count) {
        mChatInfoMediaNumber.setText(String.valueOf(count));
    }

    @Override
    public void setParticipantsCount(int count) {
        mChatInfoParticipants.setText(String.valueOf(count));
    }

    @Override
    public void setBlockedCount(int count) {
        mChatInfoBlockedNumber.setText(String.valueOf(count));
    }

    @Override
    public void loadConversationError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.chat_info_load_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void isAdmin(boolean isAdmin) {
        mChatInfoToolbar.getMenu().findItem(R.id.chat_info_add_person).setVisible(isAdmin);
        mChatInfoSubject.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        mChatInfoSubjectTextView.setVisibility(!isAdmin ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showSubject(boolean show) {
        mChatInfoSubjectLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        mChatInfoEditSave.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setChatCreatedBy(@NonNull String createdBy) {
        mChatInfoChatCreatedBy.setText(createdBy);
    }

    @Override
    public void setChatCreatedAt(@NonNull String chatCreatedAt) {
        mChatInfoChatCreatedAt.setText(chatCreatedAt);
    }

    @Override
    public void exitChatError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.chat_info_chat_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showReport(boolean show) {
        mChatInfoToolbar.getMenu().findItem(R.id.chats_info_overflow).setVisible(show);
    }

    @Override
    public void reportSent() {
        ColoredSnackBar.success(ColoredSnackBar.contentView(this), R.string.chat_info_report_success, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void reportError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.chat_info_report_error, Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.chat_info_edit_save)
    void saveClick() {
        mChatInfoPresenter.saveClicked();
    }

    @OnClick(R.id.chat_info_exit_chat)
    void exitClick() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mChatInfoPresenter.exitChatClicked();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setMessage(getString(R.string.chat_info_exit_question))
                .create();
        alertDialog.show();
    }

    @OnClick(R.id.chat_info_avatar)
    void avatarClick() {
        if (!PermissionHelper.checkSelectImagePermission(this, REQUEST_CODE_PERMISSION)) {
            return;
        }

        mChatInfoPresenter.selectImageClicked();
    }

    @OnClick(R.id.chat_info_shouts_cell)
    public void onShoutsClick() {
        startActivity(ChatShoutsActivity.newIntent(this, conversationId));
    }

    @OnClick(R.id.chat_info_media_cell)
    public void onMediaClick() {
        startActivity(ChatMediaGalleryActivity.newIntent(this, conversationId));
    }

    @OnClick(R.id.chat_info_participants_layouts)
    void clickParticipants() {
        startActivity(ChatParticipantsActivity.newIntent(this, conversationId));
    }

    @OnClick(R.id.chat_info_blocked_layouts)
    void clickBlocked() {
        startActivity(ChatBlockedUsersActivity.newIntent(this, conversationId));
    }
}
