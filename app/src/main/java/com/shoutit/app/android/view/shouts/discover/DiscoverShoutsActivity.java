package com.shoutit.app.android.view.shouts.discover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.BothParams;
import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.view.createshout.CreateShoutDialogActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.subsearch.SubSearchActivity;
import com.shoutit.app.android.view.shout.ShoutActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class DiscoverShoutsActivity extends BaseActivity {

    private static final String DISCOVER_ID = "discover_id";
    private static final String DISCOVER_NAME = "discover_name";

    @Bind(R.id.shouts_activity_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.shouts_progress)
    ProgressBar mProgress;

    @Bind(R.id.shouts_layout_btn)
    CheckedTextView layoutSwitchIcon;

    @Bind(R.id.discover_shouts_title)
    TextView countTv;

    @Bind(R.id.shouts_toolbar)
    Toolbar mToolbar;

    @Inject
    DiscoverShoutsAdapter mShoutsAdapter;

    @Inject
    DiscoverShoutsPresenter mShoutsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_shouts);

        ButterKnife.bind(this);

        final Bundle bundle = Preconditions.checkNotNull(getIntent().getExtras());
        final String name = bundle.getString(DISCOVER_NAME);

        setUpToolbar(name);

        mRecyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.home_linear_side_spacing)));
        setGridLayoutManager();

        mRecyclerView.setAdapter(mShoutsAdapter);

        mShoutsPresenter.getSuccessObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(mShoutsAdapter);

        mShoutsPresenter.getFailObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        mShoutsPresenter.getProgressVisible()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(mProgress));

        RxRecyclerView.scrollEvents(mRecyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) mRecyclerView.getLayoutManager(), mShoutsAdapter))
                .subscribe(mShoutsPresenter.getLoadMoreObserver());

        layoutSwitchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutSwitchIcon.setChecked(!layoutSwitchIcon.isChecked());
                if (layoutSwitchIcon.isChecked()) {
                    layoutSwitchIcon.setBackground(getResources().getDrawable(R.drawable.ic_grid_switch));
                    setLinearLayoutManager();
                } else {
                    layoutSwitchIcon.setBackground(getResources().getDrawable(R.drawable.ic_list_switch));
                    setGridLayoutManager();
                }
            }
        });

        mShoutsPresenter.getShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(ShoutActivity.newIntent(DiscoverShoutsActivity.this, shoutId));
                    }
                });

        mShoutsPresenter.getSearchClickedObservable()
                .compose(this.<BothParams<String, String>>bindToLifecycle())
                .subscribe(new Action1<BothParams<String, String>>() {
                    @Override
                    public void call(BothParams<String, String> discoverIdAndName) {
                        startActivity(SubSearchActivity.newIntent(
                                DiscoverShoutsActivity.this, SearchPresenter.SearchType.DISCOVER,
                                discoverIdAndName.param1(), discoverIdAndName.param2()));
                    }
                });

        mShoutsPresenter.getCountObservable()
                .compose(this.<Integer>bindToLifecycle())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        countTv.setText(getResources().
                                getQuantityString(R.plurals.shouts_results_pluaral, integer, integer));
                    }
                });
    }

    private void setUpToolbar(String name) {
        mToolbar.setTitle(getString(R.string.discover_shouts_title, name));
        mToolbar.inflateMenu(R.menu.shouts_menu);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.shouts_search:
                        mShoutsPresenter.onSearchClicked();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String discoverId = getIntent().getStringExtra(DISCOVER_ID);
        final String name = getIntent().getStringExtra(DISCOVER_NAME);

        final DiscoverShoutsActivityComponent activityComponent = DaggerDiscoverShoutsActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .discoverShoutsActivityModule(new DiscoverShoutsActivityModule(discoverId, name))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        activityComponent.inject(this);
        return activityComponent;
    }

    private void setLinearLayoutManager() {
        LayoutManagerHelper.setLinearLayoutManager(this, mRecyclerView, mShoutsAdapter);
    }

    private void setGridLayoutManager() {
        LayoutManagerHelper.setGridLayoutManager(this, mRecyclerView, mShoutsAdapter);
    }

    @NonNull
    public static Intent newIntent(Context context, String discoverId, String title) {
        return new Intent(context, DiscoverShoutsActivity.class)
                .putExtra(DISCOVER_ID, discoverId)
                .putExtra(DISCOVER_NAME, title);
    }

    @OnClick(R.id.discover_fab)
    void onAddShoutClicked() {
        startActivity(CreateShoutDialogActivity.getIntent(this));
    }
}
