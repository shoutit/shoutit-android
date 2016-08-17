package com.shoutit.app.android;

import android.os.Bundle;

import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.dagger.DaggerBaseDaggerFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseDaggerFragment extends BaseFragment {

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        final BaseDaggerFragmentComponent component = DaggerBaseDaggerFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build();

        inject(component);
    }

    protected abstract void inject(BaseDaggerFragmentComponent component);
}
