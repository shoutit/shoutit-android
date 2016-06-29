package com.shoutit.app.android.view.profileslist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.utils.rx.RxUtils;
import com.shoutit.app.android.view.listenings.ProfilesListAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

public abstract class BaseProfileListFragment extends BaseFragment {

    @Bind(R.id.profiles_list_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    BaseProfileListPresenter presenter;
    @Inject
    ProfilesListAdapter adapter;

    protected static final int REQUEST_OPENED_PROFILE_WAS_LISTENED = 1;

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @android.support.annotation.Nullable ViewGroup container,
                             @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        presenter.getAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getListenSuccessObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxUtils.listenMessageAction(getActivity()));

        presenter.getUnListenSuccessObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxUtils.unListenMessageAction(getActivity()));

        presenter.getLoadMoreObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getRefreshDataObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getListeningObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getActionOnlyForLoggedInUsers()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(getActivity()),
                        R.string.error_action_only_for_logged_in_user));

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && (requestCode == REQUEST_OPENED_PROFILE_WAS_LISTENED)) {
            // Need to refresh items if returned from other profile which was listened/unlistened.
            presenter.refreshData();
        } else if (requestCode == Activity.RESULT_OK) {
            super.onActivityResult(requestCode, requestCode, data);
        }
    }

}
