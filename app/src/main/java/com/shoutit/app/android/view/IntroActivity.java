package com.shoutit.app.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.SystemUIUtils;
import com.shoutit.app.android.view.login.LoginActivity;
import com.uservoice.uservoicesdk.UserVoice;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
        SystemUIUtils.setFullscreen(this);

    }

    @OnClick(R.id.activity_intro_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(this);
    }

    @OnClick(R.id.activity_intro_login_btn)
    public void onLoginClick() {
        startActivity(LoginActivity.newIntent(this));
    }
}
