package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.home.HomeGridSpacingItemDecoration;
import com.shoutit.app.android.view.home.HomeLinearSpacingItemDecoration;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiscoverShoutsActivity extends BaseActivity {

    private static final String DISCOVER_ID = "discover_id";
    private static final String DISCOVER_NAME = "discover_name";

    @Bind(R.id.shouts_activity_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.shouts_progress)
    ProgressBar mProgress;

    @Bind(R.id.shouts_layout_btn)
    CheckBox mShoutsCheckBox;

    @Bind(R.id.shouts_toolbar)
    Toolbar mToolbar;

    @Inject
    DiscoverShoutsAdapter mShoutsAdapter;

    @Inject
    DiscoverShoutsPresenter mShoutsPresenter;

    private HomeGridSpacingItemDecoration gridViewItemDecoration;
    private HomeLinearSpacingItemDecoration linearViewItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shouts_activity);

        ButterKnife.bind(this);

        final Bundle bundle = Preconditions.checkNotNull(getIntent().getExtras());
        final String name = bundle.getString(DISCOVER_NAME);

        mToolbar.setTitle(getString(R.string.discover_shouts_title, name));
        mToolbar.inflateMenu(R.menu.shouts_menu);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        gridViewItemDecoration = new HomeGridSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.home_grid_side_spacing));

        linearViewItemDecoration = new HomeLinearSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.home_linear_side_spacing));
        setGridLayoutManager();

        mRecyclerView.setAdapter(mShoutsAdapter);

        mShoutsPresenter.getSuccessObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(mShoutsAdapter);

        mShoutsPresenter.getFailObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this), R.string.discover_shouts_error));

        mShoutsPresenter.getProgressVisible()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(mProgress));

        RxRecyclerView.scrollEvents(mRecyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) mRecyclerView.getLayoutManager(), mShoutsAdapter))
                .subscribe(mShoutsPresenter.getLoadMoreObserver());

        mShoutsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    setLinearLayoutManager();
                } else {
                    setGridLayoutManager();
                }
            }
        });
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String discoverId = getIntent().getStringExtra(DISCOVER_ID);
        final DiscoverShoutsActivityComponent activityComponent = DaggerDiscoverShoutsActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .discoverShoutsActivityModule(new DiscoverShoutsActivityModule(discoverId))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        activityComponent.inject(this);
        return activityComponent;
    }

    private void setLinearLayoutManager() {
        mRecyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        mRecyclerView.removeItemDecoration(gridViewItemDecoration);
        mRecyclerView.addItemDecoration(linearViewItemDecoration);
        mRecyclerView.setAdapter(mShoutsAdapter);
        mShoutsAdapter.switchLayoutManager(true);
    }

    private void setGridLayoutManager() {
        final MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(this, 2);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.removeItemDecoration(linearViewItemDecoration);
        mRecyclerView.addItemDecoration(gridViewItemDecoration);
        mRecyclerView.setAdapter(mShoutsAdapter);
        mShoutsAdapter.switchLayoutManager(false);
    }

    @NonNull
    public static Intent newIntent(Context context, String discoverId, String title) {
        return new Intent(context, DiscoverShoutsActivity.class)
                .putExtra(DISCOVER_ID, discoverId)
                .putExtra(DISCOVER_NAME, title);
    }
}
