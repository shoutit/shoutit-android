package com.shoutit.app.android.view.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity {

    private static final String ARGS_CONVERSATION_ID = "conversation_id";
    ChatsPresenter presenter;
    private String mConversationId;

    public static Intent newIntent(@Nonnull Context context, @NonNull String conversationId) {
        return new Intent(context, ChatActivity.class)
                .putExtra(ARGS_CONVERSATION_ID, conversationId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mConversationId = getIntent().getStringExtra(ARGS_CONVERSATION_ID);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ChatActivityComponent component = DaggerChatActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}