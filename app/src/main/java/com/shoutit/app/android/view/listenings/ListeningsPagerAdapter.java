package com.shoutit.app.android.view.listenings;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;

public class ListeningsPagerAdapter extends FragmentPagerAdapter {

    private final Context context;

    @Inject
    public ListeningsPagerAdapter(FragmentManager fm, @ForActivity Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ListeningsFragment.newInstance(ListeningsPresenter.ListeningsType.USERS);
            case 1:
                return ListeningsFragment.newInstance(ListeningsPresenter.ListeningsType.PAGES);
            default:
                throw new RuntimeException("Unknow adapter position: " + position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.listening_tab_user);
            case 1:
                return context.getString(R.string.listening_tab_pages);
            default:
                throw new RuntimeException("Unknow adapter position: " + position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
