package com.shoutit.app.android.view.chats.chat_shouts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
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
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsAdapter;
import com.shoutit.app.android.view.shout.ShoutActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class ChatShoutsActivity extends BaseActivity {

    private static final String EXTRA_CONVERSATION_ID = "conversation_id";
    
    @Inject
    SearchShoutsResultsAdapter adapter;
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

    
    private MyGridLayoutManager gridLayoutManager;
    private MyLinearLayoutManager linearLayoutManager;

    public static Intent newIntent(@Nonnull Context context, @Nonnull String conversationId) {
        return new Intent(context, ChatShoutsActivity.class)
                .putExtra(EXTRA_CONVERSATION_ID, conversationId);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_shouts);
        ButterKnife.bind(this);

        gridLayoutManager = new MyGridLayoutManager(this, 2);
        linearLayoutManager = new MyLinearLayoutManager(this);

        setUpToolbar();
        initAdapter();

        presenter.getAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(ShoutActivity.newIntent(ChatShoutsActivity.this, shoutId));
                    }
                });

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(linearLayoutManager, adapter))
                .subscribe(presenter.getLoadMoreObserver());

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(gridLayoutManager, adapter))
                .subscribe(presenter.getLoadMoreObserver());

        presenter.getCountObservable()
                .compose(this.<Integer>bindToLifecycle())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        headerCountTv.setText(getResources().getQuantityString(
                                R.plurals.chat_shouts_results, integer, integer));
                    }
                });

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

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.chat_shouts_ab_title);
    }

    private void initAdapter() {
        recyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.shouts_search_results_side_spacing), this));
        setGridLayoutManager();
    }

    private void setLinearLayoutManager() {
        LayoutManagerHelper.setLinearLayoutManager(recyclerView, adapter, linearLayoutManager);
    }

    private void setGridLayoutManager() {
        LayoutManagerHelper.setGridLayoutManager(recyclerView, adapter, gridLayoutManager);
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
