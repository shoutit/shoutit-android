package com.shoutit.app.android.view.invitefriends.contactsfriends;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                ContactsFriendsActivityModule.class
        }
)
public interface ContactsFriendsActivityComponent extends BaseActivityComponent {

    void inject(ContactsFriendsActivity activity);

    ProfilesListPresenter profilesListPresenter();

}


