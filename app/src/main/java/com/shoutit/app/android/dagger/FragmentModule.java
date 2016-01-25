package com.shoutit.app.android.dagger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {

    private Fragment fragment;

    public FragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @FragmentScope
    @Named("fragmentManager")
    public FragmentManager provideFragmentManager() {
        return fragment.getFragmentManager();
    }

    @Provides
    @FragmentScope
    @Named("childFragmentManager")
    public FragmentManager provideChildFragmentManager() {
        return fragment.getChildFragmentManager();
    }
}
