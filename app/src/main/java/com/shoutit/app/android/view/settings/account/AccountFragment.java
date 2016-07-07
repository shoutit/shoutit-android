package com.shoutit.app.android.view.settings.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.LogoutHelper;
import com.shoutit.app.android.view.main.MainActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccountFragment extends BaseFragment {

    @Bind(R.id.account_password_tv)
    TextView changePasswordTextView;

    @Inject
    UserPreferences userPreferences;
    @Inject
    LogoutHelper logoutHelper;

    public static Fragment newInstance() {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);

        changePasswordTextView.setVisibility(userPreferences.isLoggedInAsPage() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.account_email_tv)
    public void onEmailClick() {
        ((AccountActivity) getActivity()).onFragmentSelected(AccountActivity.FRAGMENT_CHANGE_EMAIL);
    }

    @OnClick(R.id.account_password_tv)
    public void onPasswordClick() {
        ((AccountActivity) getActivity()).onFragmentSelected(AccountActivity.FRAGMENT_CHANGE_PASSWORD);
    }

    @OnClick(R.id.account_linked_accounts_tv)
    public void onLinkedAccountsClick() {
        ((AccountActivity) getActivity()).onFragmentSelected(AccountActivity.FRAGMENT_LINKED_ACCOUNTS);
    }

    @OnClick(R.id.account_logout_tv)
    public void onLogoutClick() {
        logoutHelper.logout();
        getActivity().finish();
        startActivity(MainActivity.newIntent(getActivity())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @javax.annotation.Nullable Bundle savedInstanceState) {
        DaggerAccountFragmentComponent.builder()
                .appComponent(App.getAppComponent(getActivity().getApplication()))
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }

}
