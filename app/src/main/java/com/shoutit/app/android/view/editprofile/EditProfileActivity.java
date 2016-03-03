package com.shoutit.app.android.view.editprofile;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

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

    @Inject
    EditProfilePresenter presenter;
    @Inject
    Picasso picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        setUpToolbar();

        presenter.getUserObservable()
                .compose(this.<User>bindToLifecycle())
                .subscribe(setUserData());

        RxTextView.textChangeEvents(nameEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getNameObserver());

        RxTextView.textChangeEvents(usernameEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getUserNameObserver());

        RxTextView.textChangeEvents(bioEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getBioObserver());

        RxTextView.textChangeEvents(websiteEt)
                .compose(this.<TextViewTextChangeEvent>bindToLifecycle())
                .map(MoreFunctions1.mapTextChangeEventToString())
                .subscribe(presenter.getWebsiteObserver());
    }

    @NonNull
    private Action1<User> setUserData() {
        return new Action1<User>() {
            @Override
            public void call(User user) {
                final Target avatarTarget = PicassoHelper.getRoundedBitmapWithStrokeTarget(
                        avatarIv, getResources().getDimensionPixelSize(R.dimen.profile_avatar_stroke),
                        false, getResources().getDimensionPixelSize(R.dimen.profile_avatar_radius));
                picasso.load(user.getImage())
                        .fit()
                        .centerCrop()
                        .into(avatarTarget);

                picasso.load(user.getCover())
                        .fit()
                        .centerCrop()
                        .into(coverIv);

                nameEt.setText(user.getName());
                usernameEt.setText(user.getUsername());
                bioEt.setText(user.getBio());
                websiteEt.setText(user.getWebUrl());

                final UserLocation userLocation = user.getLocation();
                if (userLocation != null) {
                    locationTv.setText(getString(R.string.edit_profile_country,
                            Strings.nullToEmpty(userLocation.getCity()),
                            Strings.nullToEmpty(userLocation.getCountry())));

                    final Optional<Integer> countryResId = ResourcesHelper
                            .getCountryResId(EditProfileActivity.this, userLocation);
                    final Target flagTarget = PicassoHelper
                            .getRoundedBitmapTarget(EditProfileActivity.this, flagIv);
                    if (countryResId.isPresent()) {
                        picasso.load(countryResId.get())
                                .into(flagTarget);
                    }
                }
            }
        };
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
