package com.shoutit.app.android.view.chooseprofile;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;

public class SelectProfilePagerAdapter extends FragmentPagerAdapter {

    private final Context context;

    @Inject
    public SelectProfilePagerAdapter(FragmentManager fm,
                                     @ForActivity Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SelectProfileFragment.newInstance(false);
            case 1:
                return SelectProfileFragment.newInstance(true);
            default:
                throw new RuntimeException("Unknwon pager position: " + position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.select_profile_tab_listenings);
            case 1:
                return context.getString(R.string.select_profile_tab_listeners);
            default:
                throw new RuntimeException("Unknwon pager position: " + position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
