package com.shoutit.app.android.view.authorization;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.view.postlogininterest.PostLoginInterestActivity;
import com.shoutit.app.android.view.signin.LoginActivity;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.OnClick;

import static com.google.common.base.Preconditions.checkNotNull;

public class SignUpChooseFragment extends BaseDaggerFragment {

    private static final String ARG_NAME = "name";

    @Bind(R.id.signup_choose_name_tv)
    TextView nameTv;

    public static Fragment newInstance(@Nonnull String selectedName) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_NAME, selectedName);

        final SignUpChooseFragment fragment = new SignUpChooseFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_choose, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String selectedName = checkNotNull(getArguments().getString(ARG_NAME));
        nameTv.setText(getString(R.string.signup_choose_welcome, selectedName));
    }

    @OnClick(R.id.signup_choose_contunue_as_user)
    public void continueAsUserClicked() {
        startActivity(PostLoginInterestActivity.newIntent(getActivity()));
    }

    @OnClick(R.id.signup_choose_create_page)
    public void createPageClicked() {
        // TODO
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }
}
