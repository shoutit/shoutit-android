package com.shoutit.app.android.view.verifybusiness;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseEmptyActivityComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.Bind;

public class VerifyBusinessActivity extends BaseActivity {

    @Bind(R.id.business_ver_name_et)
    EditText nameEt;
    @Bind(R.id.business_ver_name_il)
    TextInputLayout nameTil;

    @Bind(R.id.business_ver_contact_person_et)
    EditText contactPersonEt;
    @Bind(R.id.business_ver_contact_person_tl)
    TextInputLayout contactPersonTil;

    @Bind(R.id.business_ver_contact_number_et)
    EditText contactNumberEt;
    @Bind(R.id.business_ver_contact_number_tl)
    TextInputLayout contactNumberTil;

    @Bind(R.id.business_ver_email_et)
    EditText emailEt;
    @Bind(R.id.business_ver_email_til)
    TextInputLayout emailTil;

    @Bind(R.id.business_ver_images_container)
    LinearLayout imagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buisness_verification);


    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final BaseEmptyActivityComponent component = DaggerBaseEmptyActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
