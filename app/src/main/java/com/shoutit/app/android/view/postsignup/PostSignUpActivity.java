package com.shoutit.app.android.view.postsignup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.postsignup.interests.PostSignUpInterestsFragment;
import com.shoutit.app.android.view.postsignup.users.PostSignUpUsersFragment;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostSignUpActivity extends BaseDaggerActivity {

    @Bind(R.id.post_signup_next_tv)
    TextView nextButtonTv;
    @Bind(R.id.post_signup_done_tv)
    TextView doneButtonTv;
    @Bind(R.id.signups_toolbar)
    Toolbar toolbar;

    @Inject
    PostSignUpBus bus;

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

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                showDoneButton();
            } else {
                showNextButton();
            }
        });

        bus.getInterestsUploadedObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    showUsersFragment();
                    showDoneButton();
                });
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
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @OnClick(R.id.post_signup_next_tv)
    public void onNextClicked() {
        bus.nextClicked();
    }

    @OnClick(R.id.post_signup_done_tv)
    public void onDoneClicked() {
        ActivityCompat.finishAffinity(this);
        startActivity(MainActivity.newIntent(this));
    }

    private void showDoneButton() {
        nextButtonTv.setVisibility(View.GONE);
        doneButtonTv.setVisibility(View.VISIBLE);
    }

    private void showNextButton() {
        nextButtonTv.setVisibility(View.VISIBLE);
        doneButtonTv.setVisibility(View.GONE);
    }
    public void enableNextButton(boolean enable) {
        nextButtonTv.setEnabled(enable);
        nextButtonTv.setTextColor(ContextCompat.getColor(
                this, enable ? R.color.colorPrimary : R.color.post_signup_next_disabled));
    }


    public void showUsersFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right)
                .replace(R.id.post_signup_container, PostSignUpUsersFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
