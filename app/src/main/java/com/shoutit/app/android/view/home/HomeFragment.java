package com.shoutit.app.android.view.home;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.main.OnSeeAllDiscoversListener;
import com.shoutit.app.android.view.shout.ShoutActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
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
    HomeGridSpacingItemDecoration gridViewItemDecoration;
    @Inject
    HomeLinearSpacingItemDecoration linearViewItemDecoration;
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
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        // TODO handle error
                        Toast.makeText(context, "Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        RxView.clicks(fab)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mUserPreferences.isGuest()) {
                            startActivity(LoginIntroActivity.newIntent(getActivity()));
                        } else {
                            Toast.makeText(context, "Not implemented yet", Toast.LENGTH_LONG).show();
                        }
                    }
                });

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

    }

    private void setLinearLayoutManager() {
        recyclerView.setLayoutManager(new MyLinearLayoutManager(context));
        recyclerView.removeItemDecoration(gridViewItemDecoration);
        recyclerView.addItemDecoration(linearViewItemDecoration);
        recyclerView.setAdapter(adapter);
        adapter.switchLayoutManager(true);
    }

    private void setGridLayoutManager() {
        final MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(context, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == HomeAdapter.VIEW_TYPE_SHOUT_ITEM) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.removeItemDecoration(linearViewItemDecoration);
        recyclerView.addItemDecoration(gridViewItemDecoration);
        recyclerView.setAdapter(adapter);
        adapter.switchLayoutManager(false);
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
