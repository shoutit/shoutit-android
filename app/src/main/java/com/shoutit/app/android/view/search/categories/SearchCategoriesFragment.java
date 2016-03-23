package com.shoutit.app.android.view.search.categories;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

public class SearchCategoriesFragment extends BaseFragment {

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.search_categories_recycler_view)
    RecyclerView recyclerView;

    @Inject
    SearchCategoriesPresenter presenter;
    @Inject
    SearchCategoriesAdapter adapter;

    public static Fragment newInstance() {
        return new SearchCategoriesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_categories_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        presenter.getCategoriesObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getErrorsObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getCategorySelectedObservable()
                .compose(this.<Intent>bindToLifecycle())
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        startActivity(intent);
                    }
                });
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerSearchCategoriesFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }
}
