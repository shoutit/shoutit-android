package com.shoutit.app.android.dagger;

import com.shoutit.app.android.view.interests.InterestsActivity;
import com.shoutit.app.android.view.verifybusiness.VerifyBusinessActivity;

import dagger.Component;

@ActivityScope
@Component(
        dependencies = AppComponent.class,
        modules = {
                ActivityModule.class,
        }
)
/**
 * Universal Component for activities that don't need additional modules.
 * Add inject(Activity activity) method for your activity
 */
public interface BaseEmptyActivityComponent extends BaseActivityComponent {

        void inject(VerifyBusinessActivity activity);

        void inject(InterestsActivity activity);
}
