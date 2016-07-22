package com.shoutit.app.android.view.interests;

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
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseEmptyActivityComponent;
import com.shoutit.app.android.dagger.DaggerBaseEmptyActivityComponent;
import com.shoutit.app.android.utils.ApiMessagesHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.profile.tagprofile.TagProfileActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InterestsActivity extends BaseActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, InterestsActivity.class);
    }

    protected static final int REQUEST_OPENED_PROFILE_WAS_LISTENED = 1;

    @Bind(R.id.profiles_list_toolbar)
    Toolbar toolbar;
    @Bind(R.id.profiles_list_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    InterestsPresenter presenter;
    @Inject
    InterestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles_list);
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

        presenter.getListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .doOnNext(s -> setResult(RESULT_OK))
                .subscribe(ApiMessagesHelper.apiMessageAction(this));

        presenter.getUnListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .doOnNext(s -> setResult(RESULT_OK))
                .subscribe(ApiMessagesHelper.apiMessageAction(this));

        presenter.getLoadMoreObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getRefreshDataObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getListeningObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getProfileSelectedObservable()
                .compose(bindToLifecycle())
                .subscribe(tagDetail -> {
                    startActivityForResult(
                            TagProfileActivity.newIntent(InterestsActivity.this, tagDetail.getSlug()),
                            REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.listenings_interests_ab_title);
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
        final BaseEmptyActivityComponent component = DaggerBaseEmptyActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
