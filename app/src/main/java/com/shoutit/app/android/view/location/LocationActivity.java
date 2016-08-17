package com.shoutit.app.android.view.location;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LocationActivity extends BaseDaggerActivity {

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

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
