package com.shoutit.app.android.view.postlogininterest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.view.postlogininterest.postsignupsecond.PostSignupSecondActivity;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostSignUpActivity extends BaseDaggerActivity {

    @Bind(R.id.post_signup_next_done_tv)
    TextView nextDoneTv;
    @Bind(R.id.signups_toolbar)
    Toolbar toolbar;

    @Nonnull
    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, PostSignUpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_signup);
        ButterKnife.bind(this);

        setUpActionbar();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.post_signup_container, PostSignUpInterestsFragment.newInstance())
                    .commit();
        }
    }

    private void setUpActionbar() {
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @OnClick(R.id.post_signup_next_done_tv)
    public void onNextClicked() {
        startActivity(PostSignupSecondActivity.newIntent(this));
    }

    public void enableNextButton(boolean enable) {
        nextDoneTv.setEnabled(enable);
        nextDoneTv.setTextColor(ContextCompat.getColor(
                this, enable ? R.color.colorPrimary : R.color.post_signup_next_disabled));
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
