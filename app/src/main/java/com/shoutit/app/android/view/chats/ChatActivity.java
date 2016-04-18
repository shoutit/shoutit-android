package com.shoutit.app.android.view.chats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.TextWatcherAdapter;
import com.shoutit.app.android.view.chats.chats_adapter.ChatsAdapter;
import com.shoutit.app.android.view.media.RecordMediaActivity;
import com.shoutit.app.android.view.shout.ShoutActivity;
import com.shoutit.app.android.view.shouts.selectshout.SelectShoutActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class ChatActivity extends BaseActivity implements Listener {

    private static final String ARGS_IS_SHOUT_CONVERSATION = "args_shout_conversation";
    private static final String ARGS_CONVERSATION_ID = "conversation_id";

    private static final int REQUEST_ATTACHMENT = 0;
    private static final int REQUEST_LOCATION = 1;
    private static final int SELECT_SHOUT_REQUEST_CODE = 2;

    private static final String TAG = ChatActivity.class.getCanonicalName();

    @Inject
    Picasso picasso;

    @Inject
    ChatsPresenter presenter;

    @Inject
    ChatsAdapter chatsAdapter;

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

    public static Intent newIntent(@Nonnull Context context, @NonNull String conversationId, boolean shoutConversation) {
        return new Intent(context, ChatActivity.class)
                .putExtra(ARGS_CONVERSATION_ID, conversationId)
                .putExtra(ARGS_IS_SHOUT_CONVERSATION, shoutConversation);
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
                    case R.id.chats_delete: {
                        deleteConversation();
                        return true;
                    }
                    default:
                        return false;
                }
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
                .subscribe(new Action1<TextViewAfterTextChangeEvent>() {
                    @Override
                    public void call(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
                        presenter.sendTyping();
                    }
                });

        mChatsMessageEdittext.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                sendButton.setEnabled(s.length() != 0);
            }
        });

        ChatsHelper.setOnClickHideListener(mMainLayout, mChatsAttatchmentsLayout);
        ChatsHelper.setOnClickHideListener(mChatsRecyclerview, mChatsAttatchmentsLayout);
        ChatsHelper.setOnClickHideListener(mChatsMessageEdittext, mChatsAttatchmentsLayout);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = getIntent();
        final String conversationId = intent.getStringExtra(ARGS_CONVERSATION_ID);
        final boolean isShoutConversation = intent.getExtras().getBoolean(ARGS_IS_SHOUT_CONVERSATION);
        final ChatActivityComponent component = DaggerChatActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .chatsActivityModule(new ChatsActivityModule(conversationId, isShoutConversation))
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
    }

    @Override
    public void showProgress(boolean show) {
        mChatsProgress.setVisibility(show ? View.VISIBLE : View.GONE);
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
    public void setShoutToolbarInfo(String title, String chatWithString) {
        mChatsToolbar.setTitle(title);
        mChatsToolbar.setSubtitle(chatWithString);
    }

    @Override
    public void setChatToolbatInfo(String chatWithString) {
        mChatsToolbar.setTitle(chatWithString);
    }

    @Override
    public void onVideoClicked(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/*");
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
        mChatsShoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ShoutActivity.newIntent(ChatActivity.this, id));
            }
        });
    }

    @Override
    public void onShoutClicked(String shoutId) {
        startActivity(ShoutActivity.newIntent(ChatActivity.this, shoutId));
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

    @OnClick(R.id.chats_attatchments_video)
    void videoClicked() {
        startActivityForResult(RecordMediaActivity.newIntent(this, true, true, true, false), REQUEST_ATTACHMENT);
    }

    @OnClick(R.id.chats_attatchments_photo)
    void photoClicked() {
        startActivityForResult(RecordMediaActivity.newIntent(this, true, false, true, false), REQUEST_ATTACHMENT);
    }

    @OnClick(R.id.chats_attatchments_shout)
    void shoutClicked() {
        startActivityForResult(SelectShoutActivity.newIntent(ChatActivity.this), SELECT_SHOUT_REQUEST_CODE);
    }

    @OnClick(R.id.chats_attatchments_location)
    void locationClicked() {
        try {
            startActivityForResult(new PlacePicker.IntentBuilder().build(this), REQUEST_LOCATION);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void deleteConversation() {
        new AlertDialog.Builder(ChatActivity.this)
                .setMessage(getString(R.string.chats_delete_conversatin))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        presenter.deleteShout();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}