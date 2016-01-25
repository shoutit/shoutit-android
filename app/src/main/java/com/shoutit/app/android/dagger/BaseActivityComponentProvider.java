package com.shoutit.app.android.dagger;

import android.os.Bundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BaseActivityComponentProvider {

    @Nonnull
    BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState);

}
