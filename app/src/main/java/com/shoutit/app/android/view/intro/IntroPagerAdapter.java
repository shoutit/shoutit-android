package com.shoutit.app.android.view.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class IntroPagerAdapter extends FragmentPagerAdapter {

    private static final int ITEMS_COUNT = 5;

    @Inject
    public IntroPagerAdapter(@Nonnull FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return new FirstIntroPageFragment();
            default:
                throw new RuntimeException("Unknown page number");
        }
    }

    @Override
    public int getCount() {
        return ITEMS_COUNT;
    }
}
