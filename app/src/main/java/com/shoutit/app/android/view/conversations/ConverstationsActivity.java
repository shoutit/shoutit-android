package com.shoutit.app.android.view.conversations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConverstationsActivity extends BaseActivity implements ConversationsPresenter.Listener {

    @Bind(R.id.conversation_toolbar)
    Toolbar mConversationToolbar;
    @Bind(R.id.conversation_recyclerview)
    RecyclerView mConversationRecyclerview;
    @Bind(R.id.conversation_progress)
    ProgressBar mConversationProgress;
    @Bind(R.id.conversation_empty)
    TextView mConversationEmptyText;

    @Inject
    ConversationsPresenter presenter;
    @Inject
    ConversationsAdapter adapter;

    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, ConverstationsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        ButterKnife.bind(this);

        mConversationRecyclerview.setAdapter(adapter);
        mConversationRecyclerview.setLayoutManager(new MyLinearLayoutManager(this));
        mConversationToolbar.setTitle(R.string.conversation_title);
        mConversationToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mConversationToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RxRecyclerView.scrollEvents(mConversationRecyclerview)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) mConversationRecyclerview.getLayoutManager(), adapter))
                .subscribe(presenter.loadMoreObserver());

        presenter.register(this);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ConversationsActivityComponent component = DaggerConversationsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unregister();
    }

    @Override
    public void emptyList() {
        mConversationEmptyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showProgress(boolean show) {
        mConversationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setData(@NonNull List<BaseAdapterItem> items) {
        adapter.call(items);
    }

    @Override
    public void error() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
    }
}