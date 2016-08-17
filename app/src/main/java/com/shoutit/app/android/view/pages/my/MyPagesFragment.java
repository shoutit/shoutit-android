package com.shoutit.app.android.view.pages.my;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.facebook.FacebookHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.pages.PagesAdapter;
import com.shoutit.app.android.view.pages.PagesPagerFragment;
import com.shoutit.app.android.view.profile.page.PageProfileActivity;
import com.shoutit.app.android.view.profile.page.edit.EditPageActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;

public class MyPagesFragment extends BaseDaggerFragment implements MyPagesDialog.PagesDialogListener {

    private static final int REQUEST_OPENED_PROFILE_WAS_LISTENED = 1;

    @Bind(R.id.pages_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    MyPagesPresenter presenter;
    @Inject
    PagesAdapter adapter;
    @Inject
    MyPagesDialog dialog;
    @Inject
    UserPreferences mUserPreferences;

    public static Fragment newInstance() {
        return new MyPagesFragment();
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

        presenter.getPagesObservable()
                .compose(bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getPageSelectedObservable()
                .compose(bindToLifecycle())
                .subscribe(this::showOptionsDialog);

        presenter.getLoadMoreObservable()
                .compose(bindToLifecycle())
                .subscribe();

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(layoutManager, adapter))
                .subscribe(presenter.getLoadMoreObserver());
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

    private void showOptionsDialog(@Nonnull Page page) {
        dialog.show(page, this);
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }

    @Override
    public void showProfile(String userName) {
        getParentFragment().startActivityForResult(
                PageProfileActivity.newIntent(getActivity(), userName),
                REQUEST_OPENED_PROFILE_WAS_LISTENED);
    }

    @Override
    public void editPage(String userName) {
        getActivity().startActivityForResult(EditPageActivity.newIntent(getActivity(), userName),
                PagesPagerFragment.REQUEST_CODE_PAGE_EDITED);
    }

    @Override
    public void useShoutItAsPage(Page page) {
        FacebookHelper.logOutFromFacebook();
        mUserPreferences.setPage(page);
        mUserPreferences.setTwilioToken(null);

        ActivityCompat.finishAffinity(getActivity());
        startActivity(MainActivity.newIntent(getActivity()));
    }
}
