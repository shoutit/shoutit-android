package com.shoutit.app.android.view.conversations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConverstationsActivity extends BaseActivity {

    @Bind(R.id.conversation_toolbar)
    Toolbar mConversationToolbar;
    @Bind(R.id.conversation_recyclerview)
    RecyclerView mConversationRecyclerview;
    @Bind(R.id.conversation_progress)
    ProgressBar mConversationProgress;

    ConversationsPresenter presenter;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, ConverstationsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        ButterKnife.bind(this);


    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ConversationsActivityComponent component = DaggerConversationsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}