package com.shoutit.app.android.view.authorization.signup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.view.authorization.SignUpChooseFragment;
import com.shoutit.app.android.view.signin.LoginActivity;

import butterknife.Bind;
import rx.functions.Action1;

public class SignUpFragment extends BaseDaggerFragment {

    @Bind(R.id.signup_signup_btn)
    Button signupBtn;
    @Bind(R.id.signup_name_et)
    EditText nameEt;
    @Bind(R.id.signup_name_til)
    TextInputLayout nameTil;

    public static Fragment newInstance() {
        return new SignUpFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RxView.clicks(signupBtn)
                .compose(bindToLifecycle())
                .subscribe(aVoid -> {
                    final String name = nameEt.getText().toString();
                    if (checkNameValidity(name)) {
                        ((LoginActivity) getActivity()).showFragment(SignUpChooseFragment.newInstance(name), false);
                    }
                });
    }

    private boolean checkNameValidity(String name) {
        final boolean isNameValid = !Strings.isNullOrEmpty(name);
        nameTil.setError(isNameValid ?
                null : getString(R.string.error_field_empty));

        return isNameValid;
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
