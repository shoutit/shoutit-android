package com.shoutit.app.android.view.pages;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PagesFragment extends BaseFragment {

    private static final String KEY_ARE_MY_PAGES = "are_my_pages";

    public static Fragment newInstance(boolean areMyPages) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_ARE_MY_PAGES, areMyPages);

        final PagesFragment fragment = new PagesFragment();
        fragment.setArguments(bundle);

        return fragment;
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


    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerPagesFragmentComponent.builder()
                .appComponent(App.getAppComponent(getActivity().getApplication()))
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }
}
