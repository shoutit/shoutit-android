package com.shoutit.app.android.view.chats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.TextWatcherAdapter;
import com.shoutit.app.android.view.chats.chat_info.ChatInfoActivity;
import com.shoutit.app.android.view.chats.chats_adapter.ChatsAdapter;
import com.shoutit.app.android.view.chooseprofile.SelectProfileActivity;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.shout.ShoutActivity;
import com.shoutit.app.android.view.shouts.selectshout.SelectShoutActivity;
import com.shoutit.app.android.view.videoconversation.VideoConversationActivity;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.Constants;
import com.veinhorn.scrollgalleryview.VideoPlayerActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChatActivity extends BaseActivity implements Listener {

    private static final String ARGS_CONVERSATION_ID = "conversation_id";

    private static final int REQUEST_ATTACHMENT = 0;
    private static final int REQUEST_LOCATION = 1;
    private static final int SELECT_SHOUT_REQUEST_CODE = 2;
    private static final int SELECT_PROFILE_REQUEST_CODE = 3;
    private static final int INFO_REQUEST = 4;

    private static final String TAG = ChatActivity.class.getCanonicalName();

    @Inject
    Picasso picasso;

    @Inject
    ChatsPresenter presenter;

    @Inject
    ChatsAdapter chatsAdapter;

    @Inject
    UserPreferences userPreferences;

    @Bind(R.id.chats_toolbar)
    Toolbar mChatsToolbar;
    @Bind(R.id.chats_attatchments_layout)
    FrameLayout mChatsAttatchmentsLayout;
    @Bind(R.id.chats_recyclerview)
    RecyclerView mChatsRecyclerview;
    @Bind(R.id.chats_message_edittext)
    EditText mChatsMessageEdittext;
    @Bind(R.id.chats_progress)
    ProgressBar mChatsProgress;
    @Bind(R.id.chats_send_layout)
    LinearLayout inputContainer;

    @Bind(R.id.chats_shout_layout)
    View mChatsShoutLayout;
    @Bind(R.id.chats_shout_image)
    ImageView mChatsShoutImage;
    @Bind(R.id.chats_shout_layout_title)
    TextView mChatsShoutLayoutTitle;
    @Bind(R.id.chats_shout_layout_author_date)
    TextView mChatsShoutLayoutAuthorDate;
    @Bind(R.id.chats_shout_layout_type)
    TextView mChatsShoutLayoutType;
    @Bind(R.id.chats_shout_layout_price)
    TextView mChatsShoutLayoutPrice;

    @Bind(R.id.chats_main_layout)
    View mMainLayout;

    @Bind(R.id.chats_message_send_button)
    ImageButton sendButton;

    @Bind(R.id.conversations_empty)
    View emptyList;
    private String mConversationId;

    public static Intent newIntent(@Nonnull Context context, @NonNull String conversationId) {
        return new Intent(context, ChatActivity.class)
                .putExtra(ARGS_CONVERSATION_ID, conversationId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mConversationId = getConversationId();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // It doesn't work from xml
            inputContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        if (isFromDeepLink() && !userPreferences.isNormalUser()) {
            finish();
            startActivity(LoginIntroActivity.newIntent(this));
            return;
        }

        mChatsToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mChatsToolbar.setNavigationOnClickListener(view -> finish());
        mChatsToolbar.inflateMenu(R.menu.chats_menu);
        mChatsToolbar.setOnMenuItemClickListener(item -> {
            final int itemId = item.getItemId();
            switch (itemId) {
                case R.id.chats_attatchments_menu: {
                    final int visibility = mChatsAttatchmentsLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                    mChatsAttatchmentsLayout.setVisibility(visibility);
                    return true;
                }
                case R.id.chats_video_menu: {
                    presenter.getCalledPersonNameObservable()
                            .compose(ChatActivity.this.bindToLifecycle())
                            .subscribe(calledUserProfile -> {
                                startActivity(VideoConversationActivity.newIntent(
                                        calledUserProfile.getName(),
                                        calledUserProfile.getUsername(),
                                        calledUserProfile.getImage(),
                                        ChatActivity.this));
                            });
                    return true;
                }
                case R.id.chats_chat_information: {
                    startActivityForResult(ChatInfoActivity.newIntent(ChatActivity.this, mConversationId), INFO_REQUEST);
                    return true;
                }
                default:
                    return false;
            }
        });

        mChatsRecyclerview.setAdapter(chatsAdapter);
        mChatsRecyclerview.setLayoutManager(new MyLinearLayoutManager(this));

        RxRecyclerView.scrollEvents(mChatsRecyclerview)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) mChatsRecyclerview.getLayoutManager(), chatsAdapter))
                .subscribe(presenter.getRequestSubject());

        presenter.register(this);

        RxTextView.afterTextChangeEvents(mChatsMessageEdittext)
                .skip(1)
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(textViewAfterTextChangeEvent -> {
                    presenter.sendTyping();
                });

        sendButton.setEnabled(false);
        mChatsMessageEdittext.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                sendButton.setEnabled(s.length() != 0);
                mChatsAttatchmentsLayout.setVisibility(View.GONE);
            }
        });

        ChatsHelper.setOnClickHideListener(mMainLayout, mChatsAttatchmentsLayout);
        ChatsHelper.setOnClickHideListener(mChatsRecyclerview, mChatsAttatchmentsLayout);
        ChatsHelper.setOnClickHideListener(mChatsMessageEdittext, mChatsAttatchmentsLayout);
    }

    private String getConversationId() {
        final Intent intent = getIntent();
        final String conversationId;
        if (intent.hasExtra(ARGS_CONVERSATION_ID)) {
            conversationId = intent.getStringExtra(ARGS_CONVERSATION_ID);
        } else {
            conversationId = intent.getData().getQueryParameter("id");
        }
        checkNotNull(conversationId);
        return conversationId;
    }

    private boolean isFromDeepLink() {
        final String conversationId = getIntent().getStringExtra(ARGS_CONVERSATION_ID);
        return conversationId == null && getIntent().getData() != null;
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ChatActivityComponent component = DaggerChatActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .chatsActivityModule(new ChatsActivityModule(mConversationId))
                .build();
        component.inject(this);

        return component;
    }

    @OnClick(R.id.chats_message_send_button)
    void onSend() {
        final String text = mChatsMessageEdittext.getText().toString();
        presenter.postTextMessage(text);
        mChatsMessageEdittext.setText(null);
    }

    @Override
    public void emptyList() {
        mChatsRecyclerview.setVisibility(View.GONE);
        emptyList.setVisibility(View.VISIBLE);
    }

    @Override
    public void showProgress(boolean show) {
        mChatsProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        emptyList.setVisibility(View.GONE);
    }

    @Override
    public void
    setData(@NonNull List<BaseAdapterItem> items) {
        mChatsRecyclerview.setVisibility(View.VISIBLE);
        chatsAdapter.call(items);
        mChatsRecyclerview.scrollToPosition(items.size() - 1);
    }

    @Override
    public void error(Throwable throwable) {
        Log.e(TAG, "error", throwable);
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showVideoChatIcon() {
        final MenuItem item = mChatsToolbar.getMenu().findItem(R.id.chats_video_menu);
        if (item != null) {
            item.setVisible(true);
        }
    }

    @Override
    public void setToolbarInfo(String title, String subTitle) {
        mChatsToolbar.setTitle(title);
        mChatsToolbar.setSubtitle(subTitle);
    }

    @Override
    public void onVideoClicked(String url) {
        final Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(Constants.URL, url);
        startActivity(intent);
    }

    @Override
    public void onLocationClicked(double latitude, double longitude) {
        Uri uri = Uri.parse("geo:" + latitude + "," + longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onImageClicked(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        startActivity(intent);
    }

    @Override
    public void conversationDeleted() {
        finish();
    }

    public void setAboutShoutData(String title, String thumbnail, String type, String price, String authorAndTime, final String id) {
        mChatsShoutLayout.setVisibility(View.VISIBLE);
        picasso.load(thumbnail)
                .placeholder(R.drawable.ic_tag_placeholder)
                .error(R.drawable.ic_tag_placeholder)
                .centerCrop()
                .fit()
                .into(mChatsShoutImage);
        mChatsShoutLayoutType.setText(type);
        mChatsShoutLayoutTitle.setText(title);
        mChatsShoutLayoutPrice.setText(price);
        mChatsShoutLayoutAuthorDate.setText(authorAndTime);
        if (Strings.isNullOrEmpty(id)) {
            mChatsShoutLayout.setOnClickListener(v -> ColoredSnackBar.error(ColoredSnackBar.contentView(ChatActivity.this), getString(R.string.shout_deleted), Snackbar.LENGTH_SHORT));
        } else {
            mChatsShoutLayout.setOnClickListener(v -> startActivity(ShoutActivity.newIntent(ChatActivity.this, id)));
        }
    }

    @Override
    public void onShoutClicked(String shoutId) {
        if (Strings.isNullOrEmpty(shoutId)) {
            ColoredSnackBar.error(ColoredSnackBar.contentView(ChatActivity.this), getString(R.string.shout_deleted), Snackbar.LENGTH_SHORT).show();
        } else {
            startActivity(ShoutActivity.newIntent(ChatActivity.this, shoutId));
        }
    }

    @Override
    public void onProfileClicked(String userName) {
        startActivity(UserOrPageProfileActivity.newIntent(this, userName));
    }

    @Override
    public void hideAttatchentsMenu() {
        mChatsAttatchmentsLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        presenter.unregister();
        super.onDestroy();
    }

    @OnClick(R.id.chats_attatchments_profile)
    void onProfileClicked() {
        hideAttatchentsMenu();
        startActivityForResult(SelectProfileActivity.newIntent(this), SELECT_PROFILE_REQUEST_CODE);
    }

    @OnClick(R.id.chats_attatchments_media)
    void onMediaClicked() {
        hideAttatchentsMenu();
        startActivityForResult(RecordMediaActivity.newIntent(this, true, true, true, false, false), REQUEST_ATTACHMENT);
    }

    @OnClick(R.id.chats_attatchments_shout)
    void shoutClicked() {
        hideAttatchentsMenu();
        startActivityForResult(SelectShoutActivity.newIntent(ChatActivity.this), SELECT_SHOUT_REQUEST_CODE);
    }

    @OnClick(R.id.chats_attatchments_location)
    void locationClicked() {
        try {
            hideAttatchentsMenu();
            startActivityForResult(new PlacePicker.IntentBuilder().build(this), REQUEST_LOCATION);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_ATTACHMENT && resultCode == RESULT_OK) {

            final Bundle extras = data.getExtras();
            final boolean isVideo = extras.getBoolean(RecordMediaActivity.EXTRA_IS_VIDEO);
            final String media = extras.getString(RecordMediaActivity.EXTRA_MEDIA);
            checkNotNull(media);

            final String dialogText = getString(isVideo ?
                    R.string.chat_video_confirmation :
                    R.string.chat_picture_confirmation);

            showShareConfirmationDialog(dialogText, (dialog, which) -> presenter.addMedia(media, isVideo));
        } else if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            final LatLng latLng = place.getLatLng();

            final String dialogText = getString(R.string.chat_location_confirmation);

            showShareConfirmationDialog(dialogText, (dialog, which) -> presenter.sendLocation(latLng.latitude, latLng.longitude));
        } else if (requestCode == SELECT_SHOUT_REQUEST_CODE && resultCode == RESULT_OK) {

            final String dialogText = getString(R.string.chat_shout_confirmation);
            showShareConfirmationDialog(dialogText, (dialog, which) -> {
                final String shoutId = data.getStringExtra(SelectShoutActivity.RESULT_SHOUT_ID);
                presenter.sendShout(shoutId);
            });
        } else if (requestCode == SELECT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {

            final String profileId = data.getStringExtra(SelectProfileActivity.RESULT_PROFILE_ID);
            final String profileName = data.getStringExtra(SelectProfileActivity.RESULT_PROFILE_NAME);
            final String dialogText = getString(R.string.chat_profile_confirmation, profileName);

            showShareConfirmationDialog(dialogText, (dialog, which) -> presenter.sendProfile(profileId));
        } else if (requestCode == INFO_REQUEST && resultCode == RESULT_OK) {

            final boolean closeChat = data.getBooleanExtra(ChatInfoActivity.EXTRA_CLOSE_CHAT, false);
            if (closeChat) {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showShareConfirmationDialog(@Nonnull String text,
                                             @Nonnull DialogInterface.OnClickListener positiveButtonListener) {
        if (isFinishing()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.chat_dialog_title)
                .setMessage(text)
                .setPositiveButton(R.string.chat_dialog_attach, positiveButtonListener)
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}