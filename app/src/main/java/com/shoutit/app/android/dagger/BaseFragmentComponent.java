package com.shoutit.app.android.dagger;

import com.shoutit.app.android.db.DbHelper;

import dagger.Component;

@FragmentScope
@Component(
        dependencies = BaseActivityComponent.class,
        modules = {
                FragmentModule.class
        }
)
public interface BaseFragmentComponent {

        DbHelper dbHelper();
}
