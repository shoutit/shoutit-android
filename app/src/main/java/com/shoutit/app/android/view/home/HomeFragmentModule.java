package com.shoutit.app.android.view.home;

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
    public OnSeeAllDiscoversListener provideOnSeeAllDiscoversListener() {
        return (OnSeeAllDiscoversListener) fragment.getActivity();
    }

}
