package com.shoutit.app.android.view.chats.chat_media_gallery;

import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import dagger.Component;


@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
                ChatMediaGalleryActivityModule.class
        }
)
public interface ChatMediaGalleryActivityComponent extends BaseActivityComponent {

    void inject(ChatMediaGalleryActivity activity);

}


