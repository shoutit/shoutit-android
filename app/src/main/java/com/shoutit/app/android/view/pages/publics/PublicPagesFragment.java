package com.shoutit.app.android.view.pages.publics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ApiMessagesHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.listenings.ProfilesListAdapter;
import com.shoutit.app.android.view.pages.PagesPagerFragment;
import com.shoutit.app.android.view.profile.page.PageProfileActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;


public class PublicPagesFragment extends BaseFragment {

    public static final int REQUEST_OPENED_PROFILE_WAS_LISTENED = 1;

    @Bind(R.id.pages_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    PublicPagesPresenter presenter;
    @Inject
    ProfilesListAdapter adapter;

    public static Fragment newInstance() {
        return new PublicPagesFragment();
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @android.support.annotation.Nullable ViewGroup container,
                             @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pages, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        presenter.getAdapterItemsObservable()
                .compose(bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .subscribe(ApiMessagesHelper.apiMessageAction(getActivity()));

        presenter.getUnListenSuccessObservable()
                .compose(this.<ApiMessageResponse>bindToLifecycle())
                .subscribe(ApiMessagesHelper.apiMessageAction(getActivity()));

        presenter.getLoadMoreObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getRefreshDataObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getProfileToOpenObservable()
                .compose(bindToLifecycle())
                .subscribe(this::showPageProfile);

        presenter.getListeningObservable()
                .compose(bindToLifecycle())
                .subscribe();

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(layoutManager, adapter))
                .subscribe(presenter.getLoadMoreObserver());

    }

    private void showPageProfile(@Nonnull String userName) {
        getParentFragment().startActivityForResult(
                PageProfileActivity.newIntent(getActivity(), userName),
                REQUEST_OPENED_PROFILE_WAS_LISTENED);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerPublicPagesFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == REQUEST_OPENED_PROFILE_WAS_LISTENED) || requestCode == PagesPagerFragment.REQUEST_CODE_PAGE_EDITED) {
            // Need to refresh items if returned from other profile which was listened/unlistened.
            presenter.refreshData();
        } else {
            super.onActivityResult(requestCode, requestCode, data);
        }
    }
}

