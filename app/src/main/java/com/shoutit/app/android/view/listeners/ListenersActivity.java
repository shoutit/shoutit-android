package com.shoutit.app.android.view.listeners;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
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
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.rx.RxUtils;
import com.shoutit.app.android.view.listenings.ProfilesListAdapter;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListenersActivity extends BaseActivity {

    private static final String KEY_USER_NAME = "user_name";
    private static final int REQUEST_OPENED_PROFILE_WAS_LISTENED = 1;

    @Bind(R.id.listeners_toolbar)
    Toolbar toolbar;
    @Bind(R.id.listeners_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    ListenersPresenter presenter;
    @Inject
    ProfilesListAdapter adapter;

    public static Intent newIntent(Context context, @Nonnull String userName) {
        return new Intent(context, ListenersActivity.class)
                .putExtra(KEY_USER_NAME, userName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listeners);
        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setUpToolbar();

        presenter.getAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                            startActivityForResult(
                                    UserOrPageProfileActivity.newIntent(ListenersActivity.this, userName),
                                    REQUEST_OPENED_PROFILE_WAS_LISTENED);
                    }
                });

        presenter.getListenSuccessObservable()
                .compose(this.<String>bindToLifecycle())
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        setResult(RESULT_OK);
                    }
                })
                .subscribe(RxUtils.listenMessageAction(this));

        presenter.getUnListenSuccessObservable()
                .compose(this.<String>bindToLifecycle())
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        setResult(RESULT_OK);
                    }
                })
                .subscribe(RxUtils.unListenMessageAction(this));

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.listeners_ab_title);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == REQUEST_OPENED_PROFILE_WAS_LISTENED)) {
            // Need to refresh items if returned from other profile which was listened/unlistened.
            presenter.refreshData();
            recyclerView.scrollToPosition(0);
        } else if (requestCode == RESULT_OK) {
            super.onActivityResult(requestCode, requestCode, data);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String userName = checkNotNull(getIntent().getStringExtra(KEY_USER_NAME));

        final ListenersActivityComponent component = DaggerListenersActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .listenersActivityModule(new ListenersActivityModule(userName))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
