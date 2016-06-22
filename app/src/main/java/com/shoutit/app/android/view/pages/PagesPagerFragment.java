package com.shoutit.app.android.view.pages;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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
import javax.inject.Inject;

import butterknife.Bind;

public class PagesPagerFragment extends BaseFragment {

    @Bind(R.id.pages_pager_tablayout)
    TabLayout tabLayout;
    @Bind(R.id.pages_view_pager)
    ViewPager viewPager;

    @Inject
    PagesPagerAdapter adapter;

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @android.support.annotation.Nullable ViewGroup container,
                             @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pages_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerPagesPagerFragmentComponent.builder()
                .appComponent(App.getAppComponent(getActivity().getApplication()))
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }
}
