package com.shoutit.app.android.view.chats.chatsfirstconversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.TextWatcherAdapter;
import com.shoutit.app.android.view.chats.ChatsHelper;
import com.shoutit.app.android.view.chats.chat_info.ChatInfoActivity;
import com.shoutit.app.android.view.chats.chats_adapter.ChatsAdapter;
import com.shoutit.app.android.view.chooseprofile.SelectProfileActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;
import com.shoutit.app.android.view.profile.ProfileIntentHelper;
import com.shoutit.app.android.view.shout.ShoutActivity;
import com.shoutit.app.android.view.shouts.selectshout.SelectShoutActivity;
import com.shoutit.app.android.view.videoconversation.OutgoingVideoCallActivity;
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
import rx.functions.Action1;

public class ChatFirstConversationActivity extends BaseActivity implements FirstConversationListener {

    private static final String ARGS_ID_FOR_CREATION = "args_id_for_creation";
    private static final String ARGS_IS_SHOUT_CONVERSATION = "args_shout_conversation";

    private static final int REQUEST_ATTACHMENT = 0;
    private static final int REQUEST_LOCATION = 1;
    private static final int SELECT_SHOUT_REQUEST_CODE = 2;
    private static final int SELECT_PROFILE_REQUEST_CODE = 3;
    private static final int INFO_REQUEST = 4;

    private static final String TAG = ChatFirstConversationActivity.class.getCanonicalName();

    @Inject
    Picasso picasso;

    @Inject
    ChatsFirstConversationPresenter presenter;

    @Inject
    ChatsAdapter chatsAdapter;

    @Bind(R.id.chats_toolbar)
    Toolbar mChatsToolbar;
    @Bind(R.id.chats_recyclerview)
    RecyclerView mChatsRecyclerview;
    @Bind(R.id.chats_message_edittext)
    EditText mChatsMessageEdittext;
    @Bind(R.id.chats_progress)
    ProgressBar mChatsProgress;
    @Bind(R.id.chats_attatchments_layout)
    FrameLayout mChatsAttatchmentsLayout;

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
    @Bind(R.id.chats_send_layout)
    LinearLayout inputContainer;

    @Bind(R.id.chats_main_layout)
    View mMainLayout;

    @Bind(R.id.chats_message_send_button)
    ImageButton sendButton;

    @Bind(R.id.conversations_empty)
    View emptyList;

    public static Intent newIntent(@Nonnull Context context, boolean shoutConversation, @NonNull String idForCreation) {
        return new Intent(context, ChatFirstConversationActivity.class)
                .putExtra(ARGS_IS_SHOUT_CONVERSATION, shoutConversation)
                .putExtra(ARGS_ID_FOR_CREATION, idForCreation);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // It doesn't work from xml
            inputContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        mChatsToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        mChatsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
                    presenter.calledPersonUsernameObservable()
                            .compose(ChatFirstConversationActivity.this.bindToLifecycle())
                            .subscribe(calledUserProfile -> {
                                startActivity(OutgoingVideoCallActivity.newIntent(
                                        calledUserProfile.getName(),
                                        calledUserProfile.getUsername(),
                                        calledUserProfile.getImage(),
                                        ChatFirstConversationActivity.this));
                            });
                    return true;
                }
                case R.id.chats_chat_information: {
                    startActivityForResult(ChatInfoActivity.newIntent(ChatFirstConversationActivity.this, Preconditions.checkNotNull(presenter.getId())), INFO_REQUEST);
                    return true;
                }
                default:
                    return false;
            }
        });

        mChatsRecyclerview.setAdapter(chatsAdapter);
        mChatsRecyclerview.setLayoutManager(new MyLinearLayoutManager(this));
        mChatsRecyclerview.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                final int position = params.getViewAdapterPosition();

                if (position == parent.getAdapter().getItemCount() - 1) {
                    outRect.bottom = getResources().getDimensionPixelSize(R.dimen.chat_last_item_decoration);
                }
            }
        });

        presenter.register(this);

        RxTextView.afterTextChangeEvents(mChatsMessageEdittext)
                .skip(1)
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(new Action1<TextViewAfterTextChangeEvent>() {
                    @Override
                    public void call(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
                        presenter.sendTyping();
                    }
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

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Bundle extras = getIntent().getExtras();
        final boolean isShoutConversation = extras.getBoolean(ARGS_IS_SHOUT_CONVERSATION);

        final String idForCreation = extras.getString(ARGS_ID_FOR_CREATION);
        Preconditions.checkNotNull(idForCreation);

        final ChatFirstConversationActivityComponent component = DaggerChatFirstConversationActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .chatsFirstConversationActivityModule(new ChatsFirstConversationActivityModule(isShoutConversation, idForCreation))
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
    public void setData(@NonNull List<BaseAdapterItem> items) {
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
    public void onVideoClicked(String url) {
        final Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(Constants.URL, url);
        startActivity(intent);
    }

    @Override
    public void onLocationClicked(double latitude, double longitude) {
        Uri uri = Uri.parse("geo:" + latitude + "," + longitude);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onImageClicked(String url) {
        startActivity(IntentHelper.singleImageGalleryIntent(this, url));
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
        mChatsShoutLayout.setOnClickListener(v -> {
            if (Strings.isNullOrEmpty(id)) {
                ColoredSnackBar.error(ColoredSnackBar.contentView(ChatFirstConversationActivity.this), getString(R.string.shout_deleted), Snackbar.LENGTH_SHORT).show();
            } else {
                startActivity(ShoutActivity.newIntent(ChatFirstConversationActivity.this, id));
            }
        });
    }

    @Override
    public void onShoutClicked(String shoutId) {
        if (Strings.isNullOrEmpty(shoutId)) {
            ColoredSnackBar.error(ColoredSnackBar.contentView(ChatFirstConversationActivity.this), getString(R.string.shout_deleted), Snackbar.LENGTH_SHORT).show();
        } else {
            startActivity(ShoutActivity.newIntent(ChatFirstConversationActivity.this, shoutId));
        }
    }

    @Override
    public void onProfileClicked(String userName, boolean isPage) {
        startActivity(ProfileIntentHelper.newIntent(this, userName, isPage));
    }

    @Override
    public void hideAttatchentsMenu() {
        mChatsAttatchmentsLayout.setVisibility(View.GONE);
    }

    @Override
    public void setToolbarInfo(String title, String subTitle) {
        mChatsToolbar.setTitle(title);
        mChatsToolbar.setSubtitle(subTitle);
    }

    @Override
    public void showVideoChatIcon() {
        final MenuItem item = mChatsToolbar.getMenu().findItem(R.id.chats_video_menu);
        if (item != null) {
            item.setVisible(true);
        }
    }

    @Override
    protected void onDestroy() {
        presenter.unregister();
        super.onDestroy();
    }

    @OnClick(R.id.chats_attatchments_profile)
    void profileClicked() {
        startActivityForResult(SelectProfileActivity.newIntent(this), SELECT_PROFILE_REQUEST_CODE);
    }

    @OnClick(R.id.chats_attatchments_media)
    void mediaClicked() {
        startActivityForResult(RecordMediaActivity.newIntent(this, true, true, true, false, false), REQUEST_ATTACHMENT);
    }

    @OnClick(R.id.chats_attatchments_shout)
    void shoutClicked() {
        startActivityForResult(SelectShoutActivity.newIntent(ChatFirstConversationActivity.this), SELECT_SHOUT_REQUEST_CODE);
    }

    @OnClick(R.id.chats_attatchments_location)
    void locationClicked() {
        try {
            startActivityForResult(new PlacePicker.IntentBuilder().build(this), REQUEST_LOCATION);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ATTACHMENT && resultCode == RESULT_OK) {
            final Bundle extras = data.getExtras();
            final boolean isVideo = extras.getBoolean(RecordMediaActivity.EXTRA_IS_VIDEO);
            final String media = extras.getString(RecordMediaActivity.EXTRA_MEDIA);
            Preconditions.checkNotNull(media);
            presenter.addMedia(media, isVideo);
        } else if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK) {
            final Place place = PlacePicker.getPlace(this, data);
            final LatLng latLng = place.getLatLng();
            presenter.sendLocation(latLng.latitude, latLng.longitude);
        } else if (requestCode == SELECT_SHOUT_REQUEST_CODE && resultCode == RESULT_OK) {
            final String shoutId = data.getStringExtra(SelectShoutActivity.RESULT_SHOUT_ID);
            presenter.sendShout(shoutId);
        } else if (requestCode == SELECT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            final String profileId = data.getStringExtra(SelectProfileActivity.RESULT_PROFILE_ID);
            presenter.sendProfile(profileId);
        } else if (requestCode == INFO_REQUEST && resultCode == RESULT_OK) {
            final boolean closeChat = data.getBooleanExtra(ChatInfoActivity.EXTRA_CLOSE_CHAT, false);
            if (closeChat) {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void showDeleteMenu(boolean show) {
        mChatsToolbar.getMenu().findItem(R.id.chats_overflow).setVisible(show);
    }

    @Override
    public void showChatInfoMenu(boolean show) {
        mChatsToolbar.getMenu().findItem(R.id.chats_chat_information).setVisible(show);
    }
}