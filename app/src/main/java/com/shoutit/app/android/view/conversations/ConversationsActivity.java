package com.shoutit.app.android.view.conversations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.view.chats.public_chat.CreatePublicChatActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationsActivity extends BaseDaggerActivity {

    @Bind(R.id.conversation_toolbar)
    Toolbar toolbar;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, ConversationsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        ButterKnife.bind(this);

        setUpToolbar();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_conversations_container, ConversationsPagerFragment.newInstance())
                    .commit();
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.conversation_title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(CreatePublicChatActivity.newIntent(ConversationsActivity.this));
                return true;
            }
        });
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
