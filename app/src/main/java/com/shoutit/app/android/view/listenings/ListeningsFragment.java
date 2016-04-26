package com.shoutit.app.android.view.listenings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListeningsFragment extends BaseFragment {

    private static final int REQUEST_CODE_PROFILE = 1;
    private static final String KEY_LISTENING_TYPE = "listening_type";

    @Bind(R.id.listenings_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    ListeningsPresenter presenter;
    @Inject
    ProfilesListAdapter adapter;

    public static Fragment newInstance(@Nonnull ListeningsPresenter.ListeningsType listeningsType) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_LISTENING_TYPE, listeningsType);

        final Fragment fragment = new ListeningsFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listenings, container, false);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {

        final ListeningsPresenter.ListeningsType listeningsType = (ListeningsPresenter.ListeningsType)
                checkNotNull(getArguments().getSerializable(KEY_LISTENING_TYPE));

        DaggerListeningsFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .listeningsFragmentModule(new ListeningsFragmentModule(listeningsType))
                .build()
                .inject(this);
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

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                        startActivityForResult(UserOrPageProfileActivity.newIntent(getActivity(), userName), REQUEST_CODE_PROFILE);
                    }
                });
    }
}
