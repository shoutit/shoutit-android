package com.shoutit.app.android.view.settings.account;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccountFragment extends Fragment {

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
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.account_email_tv)
    public void onEmailClick() {
        ((AccountActivity) getActivity()).onFragmentSelected(AccountActivity.FRAGMENT_CHANGE_EMAIL);
    }

    @OnClick(R.id.account_password_tv)
    public void onPasswordClick() {
        ((AccountActivity) getActivity()).onFragmentSelected(AccountActivity.FRAGMENT_CHANGE_PASSWORD);
    }

}
