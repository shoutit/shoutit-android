package com.shoutit.app.android.view.loginintro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.shoutit.app.android.R;
import com.shoutit.app.android.view.about.AboutActivity;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginIntroActivity extends AppCompatActivity {

    @Bind(R.id.activity_login_toolbar)
    Toolbar toolbar;

    @Nonnull
    public static Intent newIntent(Context from) {
        return new Intent(from, LoginIntroActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setUpActionBar();
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(null);
        toolbar.setNavigationIcon(R.drawable.ic_blue_arrow);
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

    @OnClick(R.id.activity_login_signup)
    public void singUpClick(){
        com.shoutit.app.android.view.signin.LoginActivity.
    }

    @OnClick(R.id.activity_login_feedback)
    public void onFeedbackClick() {
        UserVoice.launchContactUs(this);
    }

    @OnClick(R.id.activity_login_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(this);
    }

    @OnClick(R.id.activity_login_about)
    public void onAboutClick() {
        startActivity(new Intent(this, AboutActivity.class));
    }
}
