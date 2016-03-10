package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import javax.inject.Inject;

public class PostSignupPagerAdapter extends FragmentPagerAdapter {

    @Inject
    public PostSignupPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return PostSignupUserFragment.newInstance();
        } else {
            return PostSignupPagesFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
