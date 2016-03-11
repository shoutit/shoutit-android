package com.shoutit.app.android.view.signin.forgotpassword;

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
        }
)
public interface ForgotPasswordActivityComponent extends BaseActivityComponent {

    void inject(ForgotPasswordActivity forgotPasswordActivity);

}
