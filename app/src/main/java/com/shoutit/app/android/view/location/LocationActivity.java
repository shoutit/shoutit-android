package com.shoutit.app.android.view.location;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class LocationActivity extends BaseActivity {

    @Inject
    LocationAdapter adapter;
    @Inject
    LocationPresenter presenter;

    private LocationActivityDelegate mLocationActivityDelegate;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, LocationActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationActivityDelegate = new LocationActivityDelegate(this, presenter, adapter);
        mLocationActivityDelegate.onCreate();

        presenter.getUserUpdateSuccessObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    Toast.makeText(this, R.string.location_update_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!mLocationActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationActivityDelegate.onDestroy();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        LocationActivityComponent component = DaggerLocationActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
