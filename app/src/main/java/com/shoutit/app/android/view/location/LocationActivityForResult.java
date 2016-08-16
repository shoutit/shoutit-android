package com.shoutit.app.android.view.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.utils.KeyboardHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class LocationActivityForResult extends BaseDaggerActivity {

    public static final String EXTRAS_USER_LOCATION = "extras_location";

    @Inject
    LocationAdapter adapter;
    @Inject
    LocationForResultPresenter presenter;

    private LocationActivityDelegate mLocationActivityDelegate;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, LocationActivityForResult.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationActivityDelegate = new LocationActivityDelegate(this, presenter, adapter);
        mLocationActivityDelegate.onCreate();

        presenter.getUpdateLocationObservable()
                .compose(this.<UserLocation>bindToLifecycle())
                .subscribe(userLocation -> {
                    KeyboardHelper.hideSoftKeyboard(LocationActivityForResult.this);
                    setResult(Activity.RESULT_OK, new Intent().putExtra(EXTRAS_USER_LOCATION, userLocation));
                    finish();
                });
    }

    @Override
    protected void onResume() {
        mLocationActivityDelegate.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mLocationActivityDelegate.onPause();
        super.onPause();
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
        return null;
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
