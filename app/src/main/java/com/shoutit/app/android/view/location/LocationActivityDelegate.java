package com.shoutit.app.android.view.location;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PermissionHelper;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LocationActivityDelegate {

    private static final long TYPING_THRESHOLD_MS = 500;
    private static final int REQUEST_CODE_LOCATION = 0;

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
    private final RxAppCompatActivity mActivity;
    private final ILocationPresenter mPresenter;
    private final LocationAdapter mAdapter;

    public LocationActivityDelegate(RxAppCompatActivity activity, ILocationPresenter presenter, LocationAdapter adapter) {
        mActivity = activity;
        mPresenter = presenter;
        mAdapter = adapter;
    }

    public void onCreate() {
        mActivity.setContentView(R.layout.activity_location);

        ButterKnife.bind(mActivity);
        setUpActionbar();

        askForLocationPermissions();

        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(mAdapter);

        RxTextView.textChangeEvents(searchEditText)
                .debounce(TYPING_THRESHOLD_MS, TimeUnit.MILLISECONDS)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(mActivity.<String>bindToLifecycle())
                .subscribe(mPresenter.getQuerySubject());

        mPresenter.getAllAdapterItemsObservable()
                .compose(mActivity.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(mAdapter);

        mPresenter.getQueryProgressObservable()
                .compose(mActivity.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(queryProgressBar, View.INVISIBLE));

        mPresenter.getProgressObservable()
                .compose(mActivity.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        mPresenter.getLocationErrorObservable()
                .compose(mActivity.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(mActivity),
                        R.string.location_fetching_error));

        mPresenter.getUpdateLocationErrorObservable()
                .compose(mActivity.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(mActivity),
                        R.string.location_update_error));
    }

    public void onDestroy() {
        mPresenter.disconnectGoogleApi();
    }

    @SuppressLint("PrivateResource")
    private void setUpActionbar() {
        toolbar.setTitle(R.string.location_toolbar_title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(view -> mActivity.finish());
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(mActivity.findViewById(android.R.id.content), R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
                mPresenter.refreshGpsLocation();
            } else {
                ColoredSnackBar.error(mActivity.findViewById(android.R.id.content), R.string.permission_not_granted, Snackbar.LENGTH_SHORT);
            }
            return true;
        } else {
            return false;
        }
    }

    private void askForLocationPermissions() {
        PermissionHelper.checkPermissions(mActivity, REQUEST_CODE_LOCATION,
                mActivity.findViewById(android.R.id.content), R.string.permission_location_explanation,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
    }

}