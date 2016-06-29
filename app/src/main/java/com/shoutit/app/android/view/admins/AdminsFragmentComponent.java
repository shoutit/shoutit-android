package com.shoutit.app.android.view.admins;

import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import dagger.Component;


@FragmentScope
@Component(
        dependencies = BaseActivityComponent.class,
        modules = {
                FragmentModule.class,
                AdminsFragmentModule.class
        }
)
public interface AdminsFragmentComponent extends BaseFragmentComponent {

    void inject(AdminsFragment fragment);

    BaseProfileListPresenter profileListPresenter();
}




