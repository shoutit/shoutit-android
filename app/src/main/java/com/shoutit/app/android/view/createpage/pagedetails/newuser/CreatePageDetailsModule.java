package com.shoutit.app.android.view.createpage.pagedetails.newuser;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class CreatePageDetailsModule {

    @NonNull
    private final String categoryId;

    public CreatePageDetailsModule(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    @Provides
    public String provideCategoryId() {
        return categoryId;
    }

}
