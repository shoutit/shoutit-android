package com.shoutit.app.android.view.search.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.main.shouts.SearchShoutFragment;
import com.shoutit.app.android.view.search.main.users.SearchUsersFragment;

import javax.inject.Inject;

public class MainSearchPagerAdapter extends FragmentPagerAdapter {
    public static final int SHOUTS_FRAGMENT_POSITION = 0;
    private static final int USER_FRAGMENT_POSITION = 1;

    private final Context context;

    @Inject
    public MainSearchPagerAdapter(@ForActivity Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case SHOUTS_FRAGMENT_POSITION:
                return SearchShoutFragment.newInstance(SearchPresenter.SearchType.SHOUTS);
            case USER_FRAGMENT_POSITION:
                return SearchUsersFragment.newInstance();
            default:
                throw new RuntimeException("Unknown adapter position");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case SHOUTS_FRAGMENT_POSITION:
                return context.getString(R.string.search_shouts_tab);
            case USER_FRAGMENT_POSITION:
                return context.getString(R.string.search_users_tab);
            default:
                throw new RuntimeException("Unknown adapter position");
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
