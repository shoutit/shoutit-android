package com.shoutit.app.android.view.conversations;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;

import javax.inject.Inject;
import javax.inject.Named;

public class ConversationsPagerAdapter extends FragmentPagerAdapter {
    public static final int POSITION_MY_CONVERSATIONS = 0;
    public static final int POSITION_PUBLIC_CONVERSATIONS = 1;

    private final Context context;

    @Inject
    public ConversationsPagerAdapter(@Named("childFragmentManager") FragmentManager fm,
                                     @ForActivity Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case POSITION_MY_CONVERSATIONS:
                return ConversationsFragment.newInstance(true);
            case POSITION_PUBLIC_CONVERSATIONS:
                return ConversationsFragment.newInstance(false);
            default:
                throw new RuntimeException("Unknown adapter position: " + position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case POSITION_MY_CONVERSATIONS:
                return context.getString(R.string.conversations_my_ab_title);
            case POSITION_PUBLIC_CONVERSATIONS:
                return context.getString(R.string.conversations_public_ab_title);
            default:
                throw new RuntimeException("Unknown adapter position: " + position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
