package com.shoutit.app.android.view.home.myfeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.utils.BaseItemDecoration;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyGridLayoutManager;

import javax.inject.Inject;

import butterknife.Bind;

public class MyFeedFragment extends BaseDaggerFragment {

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.base_recycler_view)
    RecyclerView recyclerView;

    @Inject
    MyFeedAdapter adapter;
    @Inject
    MyFeedPresenter presenter;

    public static Fragment newInstance() {
        return new MyFeedFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_with_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MyGridLayoutManager layoutManager = new MyGridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new BaseItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.home_grid_side_spacing), getActivity()));

        presenter.getShoutsAdapterItems()
                .compose(bindToLifecycle())
                .subscribe(adapter);

        presenter.getErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getRefreshShoutsObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getLoadMoreObservable()
                .compose(bindToLifecycle())
                .subscribe();

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(layoutManager, adapter))
                .subscribe(presenter.getLoadMoreShouts());
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
