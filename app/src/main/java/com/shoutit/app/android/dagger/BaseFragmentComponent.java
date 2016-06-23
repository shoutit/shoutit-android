package com.shoutit.app.android.dagger;



import com.shoutit.app.android.dao.ListeningsDao;
import com.shoutit.app.android.dao.PagesDao;
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

        ListeningsDao listeningsDao();

        PagesDao pagesDao();

}
