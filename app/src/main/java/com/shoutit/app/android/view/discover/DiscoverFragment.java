package com.shoutit.app.android.view.discover;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.main.MainActivityComponent;
import com.shoutit.app.android.view.main.OnNewDiscoverSelectedListener;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

public class DiscoverFragment extends BaseFragment {

    private static final String KEY_DISCOVER_ID = "discover_id";

    @Bind(R.id.discover_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.discover_progress_bar)
    ProgressBar progressBar;

    @Inject
    DiscoverAdapter adapter;
    @Inject
    DiscoverPresenter presenter;
    @Inject
    OnNewDiscoverSelectedListener onNewDiscoverSelectedListener;

    public static Fragment newInstance() {
        return new DiscoverFragment();
    }

    public static Fragment newInstance(@Nonnull String discoverId) {
        final Bundle bundle = new Bundle();
        bundle.putString(KEY_DISCOVER_ID, discoverId);

        final DiscoverFragment fragment = new DiscoverFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpAdapter();

        presenter.getAllAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        presenter.getErrorsObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getDiscoverSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String discoverItemId) {
                        onNewDiscoverSelectedListener.onNewDiscoverSelected(discoverItemId);
                    }
                });

        presenter.getShowMoreObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        // TODO
                        Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpAdapter() {
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                final int viewType = adapter.getItemViewType(position);
                if (viewType == DiscoverAdapter.VIEW_TYPE_DISCOVER ||
                        viewType == DiscoverAdapter.VIEW_TYPE_SHOUT) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });

        final int spacing = getResources().getDimensionPixelSize(R.dimen.discover_grid_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);

                if (position == RecyclerView.NO_POSITION) {
                    return;
                }

                final int viewType = parent.getAdapter().getItemViewType(position);
                if (viewType != DiscoverAdapter.VIEW_TYPE_SHOUT && viewType != DiscoverAdapter.VIEW_TYPE_DISCOVER) {
                    return;
                }

                if (position % 2 == 0) {
                    outRect.left = spacing;
                } else {
                    outRect.right = spacing;
                }
                outRect.bottom = spacing;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        String discoverId = null;
        if (getArguments() != null) {
             discoverId = getArguments().getString(KEY_DISCOVER_ID, null);
        }

        DaggerDiscoverFragmentComponent.builder()
                .mainActivityComponent((MainActivityComponent) baseActivityComponent)
                .fragmentModule(fragmentModule)
                .discoverFragmentModule(new DiscoverFragmentModule(this, discoverId))
                .build()
                .inject(this);
    }
}