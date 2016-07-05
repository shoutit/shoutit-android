package com.shoutit.app.android.view.chats.chat_shouts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.view.shouts_list_common.ShoutListActivityHelper;
import com.shoutit.app.android.view.shouts_list_common.SimpleShoutsAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class ChatShoutsActivity extends BaseActivity {

    private static final String EXTRA_CONVERSATION_ID = "conversation_id";

    @Inject
    SimpleShoutsAdapter adapter;
    @Inject
    ChatShoutsPresenter presenter;

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.chat_shouts_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.chat_shouts_toolbar)
    Toolbar toolbar;
    @Bind(R.id.chat_shouts_layout_switcher)
    CheckedTextView layoutSwitchIcon;
    @Bind(R.id.chat_shouts_count_tv)
    TextView headerCountTv;

    private LayoutManagerHelper layoutManagerHelper;
    private CompositeSubscription mCompositeSubscription;

    public static Intent newIntent(@Nonnull Context context, @Nonnull String conversationId) {
        return new Intent(context, ChatShoutsActivity.class)
                .putExtra(EXTRA_CONVERSATION_ID, conversationId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_shouts);
        ButterKnife.bind(this);

        layoutManagerHelper = new LayoutManagerHelper(this, adapter, recyclerView, layoutSwitchIcon);

        mCompositeSubscription = ShoutListActivityHelper.setup(this, presenter, adapter, progressView);

        presenter.getCountObservable()
                .compose(this.<Integer>bindToLifecycle())
                .subscribe(integer -> {
                    headerCountTv.setText(getResources().getQuantityString(
                            R.plurals.chat_shouts_results, integer, integer));
                });

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(layoutManagerHelper.getLinearLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(layoutManagerHelper.getGridLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());

        layoutManagerHelper.setupLayoutSwitchIcon();

        setUpToolbar();
        initAdapter();
    }

    private void setUpToolbar() {
        toolbar.setTitle(R.string.chat_shouts_ab_title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }

    private void initAdapter() {
        recyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.shouts_search_results_side_spacing), this));
        layoutManagerHelper.setGridLayoutManager();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);

        final ChatShoutsActivityComponent build = DaggerChatShoutsActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .chatShoutsActivityModule(new ChatShoutsActivityModule(conversationId))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        build.inject(this);

        return build;
    }
}
