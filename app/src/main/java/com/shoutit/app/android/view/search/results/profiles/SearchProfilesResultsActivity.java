package com.shoutit.app.android.view.search.results.profiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ApiMessagesHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.profile.ProfileIntentHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchProfilesResultsActivity extends BaseActivity {

    private static final int REQUEST_PROFILE_OPENED = 1;
    private static final String KEY_SEARCH_QUERY = "search_query";

    @Bind(R.id.search_profiles_recyclerview)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.search_profiles_toolbar)
    Toolbar toolbar;

    @Inject
    SearchProfilesResultsPresenter presenter;
    @Inject
    SearchProfilesResultsAdapter adapter;

    public static Intent newIntent(Context context, @Nonnull String searchQuery) {
        return new Intent(context, SearchProfilesResultsActivity.class)
                .putExtra(KEY_SEARCH_QUERY, searchQuery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profiles_results);
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

        presenter.getActionOnlyForLoggedInUserObserable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(SearchProfilesResultsActivity.this),
                        R.string.error_action_only_for_logged_in_user));

        presenter.getProfileToOpenObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(profile -> {
                    startActivityForResult(
                            ProfileIntentHelper.newIntent(SearchProfilesResultsActivity.this, profile),
                            REQUEST_PROFILE_OPENED);
                });

        presenter.getListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .subscribe(ApiMessagesHelper.apiMessageAction(this));

        presenter.getUnListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .subscribe(ApiMessagesHelper.apiMessageAction(this));

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());
    }

    @SuppressLint("PrivateResource")
    private void setUpToolbar() {
        toolbar.setTitle(getString(R.string.search_profiles_results));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == REQUEST_PROFILE_OPENED)) {
            // Need to refresh items if returned from other profile to refresh related data.
            presenter.refreshData();
            recyclerView.scrollToPosition(0);
        } else if (requestCode == RESULT_OK) {
            super.onActivityResult(requestCode, requestCode, data);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String searchQuery = checkNotNull(getIntent().getStringExtra(KEY_SEARCH_QUERY));

        final SearchProfilesResultsActivityComponent component = DaggerSearchProfilesResultsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .searchProfilesResultsActivityModule(new SearchProfilesResultsActivityModule(searchQuery))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
