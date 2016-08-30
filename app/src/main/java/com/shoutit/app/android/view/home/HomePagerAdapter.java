package com.shoutit.app.android.view.home;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.home.myfeed.MyFeedFragment;
import com.shoutit.app.android.view.home.picks.PicksFragment;

import javax.inject.Inject;
import javax.inject.Named;

public class HomePagerAdapter extends FragmentPagerAdapter {


    private final Resources resources;

    @Inject
    public HomePagerAdapter(@ForActivity Resources resources,
            @Named("childFragmentManager") FragmentManager fragmentManager) {
        super(fragmentManager);
        this.resources = resources;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MyFeedFragment.newInstance();
            case 1:
                return PicksFragment.newInstance();
            case 2:
                return PicksFragment.newInstance();
            default:
                throw new RuntimeException("Wrong position: " + position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.home_tab_my_feed);
            case 1:
                return resources.getString(R.string.home_tab_picks);
            case 2:
                return resources.getString(R.string.home_tab_discover);
            default:
                throw new RuntimeException("Wrong position: " + position);
        }
    }
}
