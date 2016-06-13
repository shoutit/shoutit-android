package com.shoutit.app.android.view.credits;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.createshout.CreateShoutDialogActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.main.MenuHandler;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.promote.PromoteShoutInfoActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

public class CreditsFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new CreditsFragment();
    }

    @Bind(R.id.credit_number)
    TextView mCreditNumber;

    @Inject
    UserPreferences mUserPreferences;

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.credit_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final int credits = Preconditions.checkNotNull(mUserPreferences.getUser()).getStats().getCredits();
        mCreditNumber.setText(String.valueOf(credits));
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent, @Nonnull FragmentModule fragmentModule, @Nullable Bundle savedInstanceState) {
        DaggerCreditsFragmentComponent
                .builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }


    @OnClick({R.id.credit_balance, R.id.credit_complete_profile, R.id.credit_facebook, R.id.credit_invite_friends, R.id.credit_listen, R.id.credit_promote_shout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.credit_balance:
                break;
            case R.id.credit_complete_profile:
                showDialogAndStartActivity("Complete your profile to earn 1 Shoutit Credit", "Complete Profile", UserOrPageProfileActivity.newIntent(getActivity(), User.ME));
                break;
            case R.id.credit_facebook:
                showDialogAndStartActivity("Earn 1 Shoutit Credit for each shout you publicly share on Facebook", "Create Shout", CreateShoutDialogActivity.getIntent(getActivity()));
                break;
            case R.id.credit_invite_friends:
                showDialogAndShowFragment("Earn 1 Shoutit Credit whenever a friend you invited signs up", "Invite Friends", MenuHandler.FRAGMENT_INVITE_FRIENDS);
                break;
            case R.id.credit_listen:
                showDialogAndShowFragment("Earn up to 10 Shoutit Credits for finding your friends and listening to them", "Find Friends", MenuHandler.FRAGMENT_INVITE_FRIENDS);
                break;
            case R.id.credit_promote_shout:
                startActivity(PromoteShoutInfoActivity.newIntent(getActivity()));
                break;
        }
    }

    private void showDialog(@NonNull String message, @NonNull String positiveText, @NonNull DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(positiveText, onClickListener)
                .setNegativeButton("Got it", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showDialogAndStartActivity(@NonNull String message, @NonNull String positiveText, @NonNull Intent intent) {
        showDialog(message, positiveText, (dialog, which) -> startActivity(intent));
    }

    private void showDialogAndShowFragment(@NonNull String message, @NonNull String positiveText, @NonNull String fragmentTag) {
        showDialog(message, positiveText, (dialog, which) -> {
            final MainActivity activity = (MainActivity) getActivity();
            activity.changeMenuItem(fragmentTag);
        });
    }
}
