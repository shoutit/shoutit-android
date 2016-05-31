package com.shoutit.app.android.view.invitefriends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.common.base.Preconditions;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.postlogininterest.postsignupsecond.PostSignupPagesFragment;
import com.shoutit.app.android.view.postlogininterest.postsignupsecond.PostSignupSecondActivityModule;
import com.shoutit.app.android.view.postlogininterest.postsignupsecond.PostSignupUserFragment;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserSuggestionActivity extends BaseActivity {

    private static final String EXTRA_TYPE = "extra_type";

    public static final int PAGES = 0;
    public static final int USERS = 1;

    public static Intent newUserIntent(Context context) {
        return newIntent(context, USERS);
    }

    public static Intent newPagesIntent(Context context) {
        return newIntent(context, PAGES);
    }

    private static Intent newIntent(Context context, int type) {
        return new Intent(context, UserSuggestionActivity.class)
                .putExtra(EXTRA_TYPE, type);
    }

    @Bind(R.id.suggestions_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_suggestion_activity);
        ButterKnife.bind(this);

        final int type = getIntent().getIntExtra(EXTRA_TYPE, -1);
        Preconditions.checkArgument(type >= 0);

        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mToolbar.setTitle(getToolbarTitle(type));

        if (savedInstanceState == null) {
            final Fragment fragment;
            switch (type) {
                case PAGES: {
                    fragment = PostSignupPagesFragment.newInstance();
                    break;
                }
                case USERS: {
                    fragment = PostSignupUserFragment.newInstance();
                    break;
                }
                default:
                    throw new RuntimeException("no switch case for " + type);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.user_suggestion_fragment_contaienr, fragment)
                    .commit();
        }
    }

    private String getToolbarTitle(int type) {
        switch (type) {
            case PAGES: {
                return getString(R.string.user_suggestion_pages);
            }
            case USERS: {
                return getString(R.string.user_suggestion_users);
            }
            default:
                throw new RuntimeException("no switch case for " + type);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        return DaggerUserSuggestionsActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .postSignupSecondActivityModule(new PostSignupSecondActivityModule())
                .appComponent(App.getAppComponent(getApplication()))
                .build();
    }
}