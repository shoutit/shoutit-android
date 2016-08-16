package com.shoutit.app.android.view.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.NotificationsResponse;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.UpNavigationHelper;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.main.MainActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class NotificationsActivity extends BaseActivity {

    @Bind(R.id.notifications_reycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.notifications_toolbar)
    Toolbar toolbar;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.notifications_badge)
    TextView notificationBadge;

    @Inject
    NotificationsPresenter presenter;
    @Inject
    NotificationsAdapter adapter;
    @Inject
    UserPreferences userPreferences;
    @Inject
    @UiScheduler
    Scheduler uiScheduler;

    private Subscription subscription;

    public static Intent newIntent(Context context) {
        return new Intent(context, NotificationsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        ButterKnife.bind(this);

        setUpToolbar();

        if (isFromDeepLink() && !userPreferences.isNormalUser()) {
            finish();
            startActivity(LoginIntroActivity.newIntent(this));
            return;
        }

        final MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        subscription = presenter.getAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getOpenViewForNotificationObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String appUrl) {
                        if (TextUtils.isEmpty(appUrl)) {
                            startActivity(MainActivity.newIntent(NotificationsActivity.this));
                            finishAffinity();
                        } else {
                            startActivity(IntentHelper.inAppDeepLinkIntent(appUrl));
                        }
                    }
                });

        presenter.getScrollUpObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if (firstVisibleItemPosition > 1) {
                            showNewMessagesBadge();
                        } else {
                            recyclerView.scrollToPosition(0);
                        }
                    }
                });

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .map(new Func1<RecyclerViewScrollEvent, NotificationsResponse>() {
                    @Override
                    public NotificationsResponse call(RecyclerViewScrollEvent recyclerViewScrollEvent) {
                        return null;
                    }
                })
                .subscribe(presenter.loadMoreObserver());
    }

    private void showNewMessagesBadge() {
        notificationBadge.setVisibility(View.VISIBLE);
        Observable.timer(4, TimeUnit.SECONDS)
                .observeOn(uiScheduler)
                .compose(this.<Long>bindToLifecycle())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
    }

    @OnClick(R.id.notifications_badge)
    public void onNotificationBadgeClick() {
        recyclerView.scrollToPosition(0);
        notificationBadge.setVisibility(View.GONE);
    }

    private boolean isFromDeepLink() {
        return getIntent() != null && getIntent().getData() != null;
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.notifications_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new UpNavigationHelper(this).onUpButtonClicked();
                return true;
            case R.id.notifications_menu_mark:
                presenter.markAllNotificationsAsRead();
                return true;
            case R.id.notifications_menu_settings:
                startActivity(IntentHelper.getAppSettingsIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final NotificationsActivityComponent component = DaggerNotificationsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
