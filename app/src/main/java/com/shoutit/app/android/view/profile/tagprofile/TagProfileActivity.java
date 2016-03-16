package com.shoutit.app.android.view.profile.tagprofile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profile.ProfileActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class TagProfileActivity extends ProfileActivity {

    public static Intent newIntent(@Nonnull Context context, @Nonnull String categorySlug) {
        return new Intent(context, TagProfileActivity.class)
                .putExtra(KEY_PROFILE_ID, categorySlug);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TagProfilePresenter presenter = (TagProfilePresenter) ((TagProfileActivityComponent) getActivityComponent()).getPresenter();

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String categorySlug) {
                        startActivityForResult(
                                TagProfileActivity.newIntent(TagProfileActivity.this, categorySlug),
                                REQUEST_PROFILE_OPENED_FROM_PROFILE);
                    }
                });
    }

    @Override
    protected int getAvatarPlaceholder() {
        return R.drawable.ic_tag_avatar;
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        final String userName = checkNotNull(intent.getStringExtra(KEY_PROFILE_ID));

        final TagProfileActivityComponent component = DaggerTagProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .tagProfileActivityModule(new TagProfileActivityModule(userName))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
