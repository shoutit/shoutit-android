package com.shoutit.app.android.view.home;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.Bind;

public class HomeFragment extends BaseFragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Bind(R.id.fragment_home_recycler_view)
    RecyclerView recyclerView;

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @android.support.annotation.Nullable ViewGroup container,
                             @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {

        DaggerHomeFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }
}
