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
                return new FirstIntroPageFragment();
            case 1:
                return new SecondIntroPageFragment();
            case 2:
                return new ThirdIntroPageFragment();
            case 3:
                return new FourthIntroPageFragment();
            case 4:
                return new FifthIntroPageFragment();
            default:
                throw new RuntimeException("Unknown page number");
        }
    }

    @Override
    public int getCount() {
        return ITEMS_COUNT;
    }
}
