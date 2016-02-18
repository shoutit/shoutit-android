package com.shoutit.app.android.view.postlogininterest;

import dagger.Module;
import dagger.Provides;

@Module
public class PostLoginActivityModule {

    private final SelectionHelper<String> mStringSelectionHelper;

    public PostLoginActivityModule(SelectionHelper<String> stringSelectionHelper) {
        mStringSelectionHelper = stringSelectionHelper;
    }

    @Provides
    public SelectionHelper<String> provideSelectionHelper() {
        return mStringSelectionHelper;
    }
}
