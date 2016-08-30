package com.shoutit.app.android.view.home.picks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.view.discover.DiscoverActivity;
import com.shoutit.app.android.view.shout.ShoutActivity;

import javax.inject.Inject;

import butterknife.Bind;

public class PicksFragment extends BaseDaggerFragment {

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.base_recycler_view)
    RecyclerView recyclerView;

    @Inject
    PicksAdapter adapter;
    @Inject
    PicksPresenter presenter;

    public static Fragment newInstance() {
        return new PicksFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_with_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        presenter.getAllAdapterItemsObservable()
                .compose(bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getShoutSelectedObservable()
                .compose(bindToLifecycle())
                .subscribe(shoutId -> {
                    startActivity(ShoutActivity.newIntent(getActivity(), shoutId));
                });

        presenter.getDiscoverSelectedObservable()
                .compose(bindToLifecycle())
                .subscribe(discoverId -> {
                    startActivity(DiscoverActivity.newIntent(getActivity(), discoverId));
                });
    }

    private void setupRecyclerView() {
        final MyGridLayoutManager layoutManager = new MyGridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                final int itemViewType = adapter.getItemViewType(position);
                return itemViewType == PicksAdapter.VIEW_TYPE_TRENDING_SHOUT ? 1 : 2;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
