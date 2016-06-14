package com.shoutit.app.android.view.credits.transactions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TransactionsActivity extends BaseActivity implements TransactionsPresenter.Listener {

    @Bind(R.id.transactions_toolbar)
    Toolbar mTransactionsToolbar;
    @Bind(R.id.transactions_recyclerview)
    RecyclerView mTransactionsRecyclerview;

    @Inject
    TransactionsPresenter presenter;

    @Inject
    TransactionsAdapter adapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, TransactionsActivity.class);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final TransactionsActivityComponent build = DaggerTransactionsActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        build.inject(this);
        return build;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions_activity);
        ButterKnife.bind(this);

        mTransactionsRecyclerview.setAdapter(adapter);
        mTransactionsRecyclerview.setLayoutManager(new MyLinearLayoutManager(this));

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
}
