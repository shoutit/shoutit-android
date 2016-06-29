package com.shoutit.app.android.view.invitefriends.facebookfriends;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                FacebookFriendsActivityModule.class
        }
)
public interface FacebookFriendsActivityComponent extends BaseActivityComponent {

    void inject(FacebookFriendsActivity activity);

    BaseProfileListPresenter profilesListPresenter();

}


