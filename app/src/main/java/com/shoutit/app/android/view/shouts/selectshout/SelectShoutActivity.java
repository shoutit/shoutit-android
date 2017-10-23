package com.shoutit.app.android.view.shouts.selectshout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class SelectShoutActivity extends BaseDaggerActivity {

    public static final String RESULT_SHOUT_ID = "extra_shout_id";

    @Bind(R.id.shouts_activity_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.shouts_progress)
    ProgressBar mProgress;

    @Bind(R.id.shouts_toolbar)
    Toolbar mToolbar;

    @Inject
    SelectShoutsAdapter mShoutsAdapter;

    @Inject
    SelectShoutsPresenter mShoutsPresenter;

    private MyGridLayoutManager gridLayoutManager;
    private MyLinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shouts);

        gridLayoutManager = new MyGridLayoutManager(this, 2);
        linearLayoutManager = new MyLinearLayoutManager(this);

        ButterKnife.bind(this);

        mRecyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.home_linear_side_spacing), this));
        setGridLayoutManager();

        mRecyclerView.setAdapter(mShoutsAdapter);

        mShoutsPresenter.getSuccessObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(mShoutsAdapter);

        mShoutsPresenter.getBookmarkSuccessMessage()
                .compose(this.<String>bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(this)));

        mShoutsPresenter.getFailObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        mShoutsPresenter.getProgressVisible()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(mProgress));

        mShoutsPresenter.getShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        setResult(RESULT_OK, new Intent().putExtra(RESULT_SHOUT_ID, shoutId));
                        finish();
                    }
                });

        setUpToolbar();
    }

    private void setUpToolbar() {
        mToolbar.setTitle(getString(R.string.select_shout_title));
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setLinearLayoutManager() {
        LayoutManagerHelper.setLinearLayoutManager(mRecyclerView, mShoutsAdapter, linearLayoutManager);
    }

    private void setGridLayoutManager() {
        LayoutManagerHelper.setGridLayoutManager(mRecyclerView, mShoutsAdapter, gridLayoutManager);
    }

    @NonNull
    public static Intent newIntent(Context context) {
        return new Intent(context, SelectShoutActivity.class);
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
