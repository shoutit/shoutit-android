package com.shoutit.app.android.view.credits.transactions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.ToolbarUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TransactionsActivity extends BaseDaggerActivity implements TransactionsPresenter.Listener {

    @Bind(R.id.transactions_toolbar)
    Toolbar mTransactionsToolbar;
    @Bind(R.id.transactions_recyclerview)
    RecyclerView mTransactionsRecyclerview;
    @Bind(R.id.base_progress)
    View mProgress;

    @Inject
    TransactionsPresenter presenter;

    @Inject
    TransactionsAdapter adapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, TransactionsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions_activity);
        ButterKnife.bind(this);

        mTransactionsRecyclerview.setAdapter(adapter);
        mTransactionsRecyclerview.setLayoutManager(new MyLinearLayoutManager(this));

        ToolbarUtils.setupToolbar(mTransactionsToolbar, R.string.transactions_title, this);

        RxRecyclerView.scrollEvents(mTransactionsRecyclerview)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) mTransactionsRecyclerview.getLayoutManager(), adapter))
                .subscribe(presenter.loadMoreObserver());

        presenter.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unregister();
    }

    @Override
    public void setData(List<BaseAdapterItem> transactions) {
        adapter.call(transactions);
    }

    @Override
    public void error() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void progress(boolean show) {
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void startActivity(String appUrl) {
        startActivity(IntentHelper.inAppDeepLinkIntent(appUrl));
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
