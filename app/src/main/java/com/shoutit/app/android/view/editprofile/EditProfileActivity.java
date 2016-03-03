package com.shoutit.app.android.view.editprofile;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EditProfileActivity extends BaseActivity {

    @Bind(R.id.edit_profile_toolbar)
    Toolbar toolbar;
    @Bind(R.id.edit_profile_cover_iv)
    ImageView coverIv;
    @Bind(R.id.edit_profile_name_et)
    EditText nameEt;
    @Bind(R.id.edit_profile_name_til)
    TextInputLayout nameInput;
    @Bind(R.id.edit_profile_username_et)
    EditText usernameEt;
    @Bind(R.id.edit_profile_username_til)
    TextInputLayout usernameTil;
    @Bind(R.id.edit_profile_bio_et)
    EditText bioEt;
    @Bind(R.id.edit_profile_bio_til)
    TextInputLayout bioTil;
    @Bind(R.id.edit_profile_flag_iv)
    ImageView flagIv;
    @Bind(R.id.edit_profile_location_tv)
    TextView locationTv;
    @Bind(R.id.edit_profile_website_et)
    EditText websiteEt;
    @Bind(R.id.edit_profile_website_til)
    TextInputLayout websiteTil;
    @Bind(R.id.edit_profile_avatar_iv)
    ImageView avatarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        setUpToolbar();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.edit_profile_toolbar_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.edit_profile_menu_save:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final EditProfileActivityComponent component = DaggerEditProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
