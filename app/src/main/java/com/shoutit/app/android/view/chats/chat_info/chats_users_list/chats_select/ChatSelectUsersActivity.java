package com.shoutit.app.android.view.chats.chat_info.chats_users_list.chats_select;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.ChatUsersListAdapter;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.ChatUsersListComponent;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.ChatUsersListModule;
import com.shoutit.app.android.view.chats.chat_info.chats_users_list.DaggerChatUsersListComponent;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatSelectUsersActivity extends BaseActivity implements ChatSelectUsersPresenter.Listener {

    private static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";

    @Bind(R.id.chat_participant_toolbar)
    Toolbar mChatParticipantToolbar;
    @Bind(R.id.chat_participant_recyclerview)
    RecyclerView mChatParticipantRecyclerview;
    @Bind(R.id.base_progress)
    View mProgress;

    @Inject
    ChatUsersListAdapter adapter;

    @Inject
    ChatSelectUsersPresenter mChatParticipantsPresenter;

    @Inject
    SelectDialog dialog;

    public static Intent newIntent(Context context, String conversationId) {
        return new Intent(context, ChatSelectUsersActivity.class)
                .putExtra(EXTRA_CONVERSATION_ID, conversationId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_participants_activity);
        ButterKnife.bind(this);

        mChatParticipantToolbar.setTitle(R.string.chat_select_users_title);
        mChatParticipantToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        mChatParticipantToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mChatParticipantRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mChatParticipantRecyclerview.setAdapter(adapter);

        mChatParticipantsPresenter.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatParticipantsPresenter.unregister();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        final ChatUsersListComponent build = DaggerChatUsersListComponent.builder()
                .appComponent(App.getAppComponent(getApplication()))
                .activityModule(new ActivityModule(this))
                .chatUsersListModule(new ChatUsersListModule(conversationId))
                .build();
        build.inject(this);
        return build;
    }

    @Override
    public void error() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.create_public_chat_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setData(List<BaseAdapterItem> profileItems) {
        adapter.call(profileItems);
    }

    @Override
    public void showProgress(boolean show) {
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showDialog(String id, String name) {
        dialog.show(id, name, mChatParticipantsPresenter);
    }

    @Override
    public void finishScreen() {
        finish();
    }
}
