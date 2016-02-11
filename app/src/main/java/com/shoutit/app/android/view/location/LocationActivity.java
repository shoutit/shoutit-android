package com.shoutit.app.android.view.location;

import android.Manifest;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PermissionHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;

public class LocationActivity extends BaseActivity {

    private static final int REQUEST_CODE_LOCATION = 2;

    @Bind(R.id.location_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.location_toolbar)
    Toolbar toolbar;
    @Bind(R.id.location_search_et)
    EditText searchEditText;
    @Bind(R.id.location_progress_bar)
    ProgressBar progressBar;

    @Inject
    LocationAdapter adapter;
    @Inject
    LocationPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_location);

        setUpActionbar();

        askForLocationPermissions();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        RxTextView.textChangeEvents(searchEditText)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getQuerySubject());

        presenter.getAllAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

    }

    private void askForLocationPermissions() {
        PermissionHelper.checkPermissions(this, REQUEST_CODE_LOCATION,
                findViewById(android.R.id.content), R.string.permission_location_explanation,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION});
    }

    private void setUpActionbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.location_toolbar_title);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(findViewById(android.R.id.content), R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
                presenter.getCurrentLocation();
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
