package com.shoutit.app.android.view.bookmarks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.view.shouts_list_common.ShoutListActivityHelper;
import com.shoutit.app.android.view.shouts_list_common.SimpleShoutsAdapter;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.subscriptions.CompositeSubscription;

public class BookmarkedShoutsFragment extends BaseDaggerFragment {

    @Inject
    SimpleShoutsAdapter adapter;
    @Inject
    BookmarkedShoutsPresenter presenter;

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.chat_shouts_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.chat_shouts_layout_switcher)
    CheckedTextView layoutSwitchIcon;

    private LayoutManagerHelper layoutManagerHelper;
    private CompositeSubscription mCompositeSubscription;

    public static Fragment newInstance() {
        return new BookmarkedShoutsFragment();
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmarked_shouts_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutManagerHelper = new LayoutManagerHelper(getActivity(), adapter, recyclerView, layoutSwitchIcon);

        mCompositeSubscription = ShoutListActivityHelper.setup((RxAppCompatActivity) getActivity(), presenter, adapter, progressView);

        layoutManagerHelper.setupLayoutSwitchIcon();

        initAdapter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCompositeSubscription.unsubscribe();
    }

    private void initAdapter() {
        recyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.shouts_search_results_side_spacing), getActivity()));
        layoutManagerHelper.setGridLayoutManager();
    }
}
