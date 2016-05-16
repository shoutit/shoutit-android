package com.shoutit.app.android.view.chooseprofile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLinearLayoutManager;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;

import static com.google.common.base.Preconditions.checkNotNull;

public class SelectProfileFragment extends BaseFragment {

    private static final String EXTRA_IS_LISTENERS_PAGE = "is_listeners_page";

    @Bind(R.id.select_profile_recycler_view)
    RecyclerView recyclerView;

    @Inject
    SelectProfilePresenter presenter;
    @Inject
    SelectProfileAdapter adapter;

    public static Fragment newInstance(boolean isListeners) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_IS_LISTENERS_PAGE, isListeners);

        final SelectProfileFragment fragment = new SelectProfileFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final boolean isListenersPage = checkNotNull(getArguments().getBoolean(EXTRA_IS_LISTENERS_PAGE));

        final MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        getAdapterItemsObservable(isListenersPage)
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(linearLayoutManager, adapter))
                .subscribe(getLoadMoreObserver(isListenersPage));
    }

    private Observable<List<BaseAdapterItem>> getAdapterItemsObservable(boolean isListenersPage) {
        if (isListenersPage) {
            return presenter.getListenersAdapterItems();
        } else {
            return presenter.getListeningsAdapterItems();
        }
    }

    private Observer<Object> getLoadMoreObserver(boolean isListenersPage) {
        if (isListenersPage) {
            return presenter.getLoadMoreListenersObserver();
        } else {
            return presenter.getLoadMoreListeningsObserver();
        }
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerSelectProfileFragmentComponent.builder()
                .selectProfileActivityComponent((SelectProfileActivityComponent) baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }

}
