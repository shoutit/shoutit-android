package com.shoutit.app.android.view.createshout.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.KeyboardHelper;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PermissionHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class LocationActivity extends BaseActivity {

    private static final int REQUEST_CODE_LOCATION = 0;
    private static final long TYPING_THRESHOLD_MS = 500;
    public static final String EXTRAS_USER_LOCATION = "extras_location";

    @Bind(R.id.location_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.location_toolbar)
    Toolbar toolbar;
    @Bind(R.id.location_search_et)
    EditText searchEditText;
    @Bind(R.id.location_query_progress_bar)
    ProgressBar queryProgressBar;
    @Bind(R.id.location_progress_bar)
    FrameLayout progressBar;

    @Inject
    LocationAdapter adapter;
    @Inject
    LocationPresenter presenter;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, LocationActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);

        setUpActionbar();

        askForLocationPermissions();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        RxTextView.textChangeEvents(searchEditText)
                .debounce(TYPING_THRESHOLD_MS, TimeUnit.MILLISECONDS)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getQuerySubject());

        presenter.getAllAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getQueryProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(queryProgressBar, View.INVISIBLE));

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        presenter.getLocationErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(this),
                        R.string.location_fetching_error));

        presenter.getUpdateLocationErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(this),
                        R.string.location_update_error));

        presenter.getUpdateUserObservable()
                .compose(this.<UserLocation>bindToLifecycle())
                .subscribe(new Action1<UserLocation>() {
                    @Override
                    public void call(UserLocation userLocation) {
                        KeyboardHelper.hideSoftKeyboard(LocationActivity.this);
                        setResult(Activity.RESULT_OK, new Intent().putExtra(EXTRAS_USER_LOCATION, userLocation));
                        finish();
                    }
                });

    }

    private void askForLocationPermissions() {
        PermissionHelper.checkPermissions(this, REQUEST_CODE_LOCATION,
                findViewById(android.R.id.content), R.string.permission_location_explanation,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
    }

    @SuppressLint("PrivateResource")
    private void setUpActionbar() {
        toolbar.setTitle(R.string.location_toolbar_title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(findViewById(android.R.id.content), R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
                presenter.refreshGpsLocation();
            } else {
                ColoredSnackBar.error(findViewById(android.R.id.content), R.string.permission_not_granted, Snackbar.LENGTH_SHORT);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        presenter.disconnectGoogleApi();
        super.onDestroy();
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
