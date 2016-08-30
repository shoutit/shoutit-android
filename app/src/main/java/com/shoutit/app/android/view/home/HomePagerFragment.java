package com.shoutit.app.android.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;

import javax.inject.Inject;

import butterknife.Bind;

public class HomePagerFragment extends BaseDaggerFragment {

    @Bind(R.id.home_view_pager)
    ViewPager viewPager;
    @Bind(R.id.home_tab_layout)
    TabLayout tabLayout;

    @Inject
    HomePagerAdapter homePagerAdapter;

    public static Fragment newInstance() {
        return new HomePagerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setAdapter(homePagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
