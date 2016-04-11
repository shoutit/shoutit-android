package com.shoutit.app.android.view.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.github.clans.fab.FloatingActionButton;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.view.createshout.CreateShoutDialogFragment;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.main.OnSeeAllDiscoversListener;
import com.shoutit.app.android.view.postlogininterest.PostLoginInterestActivity;
import com.shoutit.app.android.view.shout.ShoutActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import rx.functions.Action1;

public class HomeFragment extends BaseFragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Bind(R.id.fragment_home_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.fragment_home_fab)
    FloatingActionButton fab;
    @Bind(R.id.fragment_home_progress_bar)
    ProgressBar progressBar;

    @Inject
    @ForActivity
    Context context;
    @Inject
    HomePresenter presenter;
    @Inject
    HomeAdapter adapter;
    @Inject
    UserPreferences mUserPreferences;
    @Inject
    OnSeeAllDiscoversListener onSeeAllDiscoversListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.home_linear_side_spacing)));
        setGridLayoutManager();
        recyclerView.setAdapter(adapter);

        presenter.getLinearLayoutManagerObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ignore) {
                        setLinearLayoutManager();
                    }
                });

        presenter.getGridLayoutManagerObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ignore) {
                        setGridLayoutManager();
                    }
                });

        presenter.getAllAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreShouts());

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getShowAllDiscoversObservable()
                .compose(this.bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        onSeeAllDiscoversListener.onSeeAllDiscovers();
                    }
                });

        presenter.getOnDiscoverSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(@Nonnull String discoverId) {
                        startActivity(DiscoverActivity.newIntent(context, discoverId));
                    }
                });

        presenter.getShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(ShoutActivity.newIntent(context, shoutId));
                    }
                });

        presenter.getRefreshShoutsObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getLoadMoreObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.openInterestsObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        startActivity(PostLoginInterestActivity.newIntent(getActivity()));
                        getActivity().finish();
                    }
                });

    }

    private void setLinearLayoutManager() {
        LayoutManagerHelper.setLinearLayoutManager(context, recyclerView, adapter);
    }

    private void setGridLayoutManager() {
        LayoutManagerHelper.setGridLayoutManager(context, recyclerView, adapter);
    }

    @OnClick(R.id.fragment_home_fab)
    void onAddShoutClicked() {
        CreateShoutDialogFragment.newInstance().show(getFragmentManager(), null);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerHomeFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .homeFragmentModule(new HomeFragmentModule(this))
                .build()
                .inject(this);
    }
}
