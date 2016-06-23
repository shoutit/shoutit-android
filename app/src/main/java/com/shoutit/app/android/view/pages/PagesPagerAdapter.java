package com.shoutit.app.android.view.pages;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.pages.my.MyPagesFragment;

import javax.inject.Inject;

public class PagesPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    @Inject
    public PagesPagerAdapter(@ForActivity Resources resources,
                             FragmentManager fm) {
        super(fm);
        this.resources = resources;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MyPagesFragment.newInstance();
            case 1:
                return MyPagesFragment.newInstance();
            default:
                throw new RuntimeException("Unknown pages pager adapter position: " + position);

        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.pages_my_pages_tab);
            case 1:
                return resources.getString(R.string.pages_public_pages_tab);
            default:
                throw new RuntimeException("Unknown pages pager adapter position: " + position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
