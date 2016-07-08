package com.shoutit.app.android.view.filter;

import android.app.Activity;
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
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.retainfragment.RetainFragmentHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.KeyboardHelper;
import com.shoutit.app.android.view.location.LocationActivityForResult;
import com.shoutit.app.android.view.location.LocationResultHelper;
import com.shoutit.app.android.view.search.SearchPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;

public class FiltersFragment extends BaseFragment {

    private static final String KEY_SEARCH_TYPE = "key_search_type";
    private static final String KEY_INIT_CATEGORY_SLUG = "key_init_category_slug";

    public interface OnFiltersSubmitListener {
        void onFiltersSubmit(@Nonnull FiltersToSubmit filtersToSubmit);
    }

    private static final int REQUEST_GET_LOCATION = 1;

    @Bind(R.id.filters_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    FiltersAdapter adapter;
    @Inject
    FilterPresenterFactory filterPresenterFactory;

    private FiltersPresenter presenter;
    private OnFiltersSubmitListener onFiltersSubmitListener;

    public static Fragment newInstance(@Nonnull SearchPresenter.SearchType searchType,
                                       @Nullable String initCategorySlug) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_SEARCH_TYPE, searchType);
        bundle.putString(KEY_INIT_CATEGORY_SLUG, initCategorySlug);

        final FiltersFragment fragment = new FiltersFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onFiltersSubmitListener = (OnFiltersSubmitListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    getParentFragment().getClass().getSimpleName() + " must implement OnFiltersSubmitListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.filters_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        if (savedInstanceState == null) {
            presenter = filterPresenterFactory.getFiltersPresenter();
            RetainFragmentHelper.setObject(this, getActivity().getSupportFragmentManager(), presenter);
        } else {
            presenter = RetainFragmentHelper.getObjectOrNull(this, getChildFragmentManager());
            if (presenter == null) {
                presenter = filterPresenterFactory.getFiltersPresenter();
                RetainFragmentHelper.setObject(this, getActivity().getSupportFragmentManager(), presenter);
            }
        }

        presenter.getAllAdapterItems()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getLocationChangeClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (getParentFragment() != null) {
                            getParentFragment().startActivityForResult(
                                    LocationActivityForResult.newIntent(getActivity()), REQUEST_GET_LOCATION);
                        } else {
                            startActivityForResult(
                                    LocationActivityForResult.newIntent(getActivity()), REQUEST_GET_LOCATION);
                        }

                    }
                });

        presenter.getSelectedFiltersObservable()
                .compose(this.<FiltersToSubmit>bindToLifecycle())
                .subscribe(new Action1<FiltersToSubmit>() {
                    @Override
                    public void call(FiltersToSubmit filtersToSubmit) {
                        if (onFiltersSubmitListener != null) {
                            onFiltersSubmitListener.onFiltersSubmit(filtersToSubmit);
                            KeyboardHelper.hideSoftKeyboard(getActivity());
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GET_LOCATION && resultCode == Activity.RESULT_OK && data != null) {
            final UserLocation userLocation = LocationResultHelper.getLocationFromIntent(data);
            presenter.onLocationChanged(userLocation);
        } else if (resultCode == Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        final SearchPresenter.SearchType searchType = (SearchPresenter.SearchType)
                checkNotNull(getArguments().getSerializable(KEY_SEARCH_TYPE));
        final String initCategorySlug = getArguments().getString(KEY_INIT_CATEGORY_SLUG);

        DaggerFiltersFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .filtersFragmentModule(new FiltersFragmentModule(this, searchType, initCategorySlug))
                .build()
                .inject(this);
    }

}
