package com.shoutit.app.android.view.home;

import android.content.res.Resources;
import android.support.v4.app.Fragment;

import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.main.OnSeeAllDiscoversListener;

import dagger.Module;
import dagger.Provides;

@Module
public class HomeFragmentModule extends FragmentModule {

    private final HomeFragment fragment;

    public HomeFragmentModule(HomeFragment fragment) {
        super(fragment);
        this.fragment = fragment;
    }

    @Provides
    public HomeGridSpacingItemDecoration provideGridItemDecoration(@ForActivity Resources resources) {
        return new HomeGridSpacingItemDecoration(
                resources.getDimensionPixelSize(R.dimen.home_grid_side_spacing));
    }

    @Provides
    public HomeLinearSpacingItemDecoration provideLinearItemDecoration(@ForActivity Resources resources) {
        return new HomeLinearSpacingItemDecoration(
                resources.getDimensionPixelSize(R.dimen.home_linear_side_spacing));
    }

    @Provides
    public OnSeeAllDiscoversListener provideOnSeeAllDiscoversListener() {
        return (OnSeeAllDiscoversListener) fragment.getActivity();
    }

}
