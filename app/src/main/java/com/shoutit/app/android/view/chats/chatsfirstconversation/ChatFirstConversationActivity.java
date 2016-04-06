package com.shoutit.app.android.view.chats.chatsfirstconversation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Preconditions;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.chats.chats_adapter.ChatsAdapter;
import com.shoutit.app.android.view.media.RecordMediaActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatFirstConversationActivity extends BaseActivity implements ChatsFirstConversationPresenter.Listener {

    private static final String ARGS_ID_FOR_CREATION = "args_id_for_creation";
    private static final String ARGS_IS_SHOUT_CONVERSATION = "args_shout_conversation";

    private static final int REQUEST_ATTACHMENT = 0;

    private static final int REQUEST_LOCATION = 1;
    private static final String TAG = ChatFirstConversationActivity.class.getCanonicalName();

    @Inject
    ChatsFirstConversationPresenter presenter;

    @Inject
    ChatsAdapter chatsAdapter;

    @Bind(R.id.chats_toolbar)
    Toolbar mChatsToolbar;
    @Bind(R.id.chats_shout_layout)
    LinearLayout mChatsShoutLayout;
    @Bind(R.id.chats_recyclerview)
    RecyclerView mChatsRecyclerview;
    @Bind(R.id.chats_message_edittext)
    EditText mChatsMessageEdittext;
    @Bind(R.id.chats_progress)
    ProgressBar mChatsProgress;

    @Bind(R.id.chats_attatchments_layout)
    FrameLayout mChatsAttatchmentsLayout;

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

        mChatsToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mChatsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mChatsToolbar.inflateMenu(R.menu.chats_menu);
        mChatsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.chats_attatchments_menu: {
                        final int visibility = mChatsAttatchmentsLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                        mChatsAttatchmentsLayout.setVisibility(visibility);
                        return true;
                    }
                    case R.id.chats_video_menu: {

                        return true;
                    }
                    default:
                        return false;
                }
            }
        });

        mChatsRecyclerview.setAdapter(chatsAdapter);
        mChatsRecyclerview.setLayoutManager(new MyLinearLayoutManager(this));

        presenter.register(this);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Bundle extras = getIntent().getExtras();
        final boolean isShoutConversation = extras.getBoolean(ARGS_IS_SHOUT_CONVERSATION);
        final String idForCreation = extras.getString(ARGS_ID_FOR_CREATION);
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

    }

    @Override
    public void showProgress(boolean show) {
        mChatsProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setData(@NonNull List<BaseAdapterItem> items) {
        chatsAdapter.call(items);
    }

    @Override
    public void error(Throwable throwable) {
        Log.e(TAG, "error", throwable);
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        presenter.unregister();
        super.onDestroy();
    }

    @OnClick(R.id.chats_attatchments_video)
    void videoClicked() {
        startActivityForResult(RecordMediaActivity.newIntent(this, true, true), REQUEST_ATTACHMENT);
    }

    @OnClick(R.id.chats_attatchments_photo)
    void photoClicked() {
        startActivityForResult(RecordMediaActivity.newIntent(this, true, false), REQUEST_ATTACHMENT);
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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}