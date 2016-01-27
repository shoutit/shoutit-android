package com.shoutit.app.android.view.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import javax.inject.Inject;

public class IntroPagerAdapter extends FragmentPagerAdapter {

    private static final int ITEMS_COUNT = 5;

    @Inject
    public IntroPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return IntroFragment.newInstance(i);
    }

    @Override
    public int getCount() {
        return ITEMS_COUNT;
    }
}
