package com.shoutit.app.android.dagger;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private final AppCompatActivity mActivity;

    public ActivityModule(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Provides
    @ForActivity
    public Resources provideResources() {
        return mActivity.getResources();
    }

    @Provides
    @ForActivity
    public Context provideContext() {
        return mActivity;
    }

    @Provides
    public AssetManager provideAssetManager(@ForActivity Context context) {
        return context.getAssets();
    }

    @Provides
    public LayoutInflater provideLayoutInflater(@ForActivity Context context) {
        return LayoutInflater.from(context);
    }

    @Provides
    InputMethodManager provideInputMethodManager(@ForActivity Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Provides
    FragmentManager provideFragmentManager() {
        return mActivity.getSupportFragmentManager();
    }
}
