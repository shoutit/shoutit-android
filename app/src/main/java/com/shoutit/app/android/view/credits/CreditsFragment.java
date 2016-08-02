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
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.pusher.PusherHelperHolder;
import com.shoutit.app.android.view.createshout.CreateShoutDialogActivity;
import com.shoutit.app.android.view.credits.transactions.TransactionsActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.main.MenuHandler;
import com.shoutit.app.android.view.profile.ProfileIntentHelper;
import com.shoutit.app.android.view.profile.user.UserProfileActivity;
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

    @Inject
    PusherHelperHolder mPusherHelper;

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.credit_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final BaseProfile profile = mUserPreferences.getUserOrPage();
        final int credits = Preconditions.checkNotNull(profile.getStats(), "null stats object for: " + profile.getUsername()).getCredits();
        mCreditNumber.setText(String.valueOf(credits));
        mPusherHelper.getPusherHelper().getStatsObservable()
                .compose(bindToLifecycle())
                .subscribe(stats -> {
                    mCreditNumber.setText(String.valueOf(stats.getCredits()));
                });
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
                startActivity(TransactionsActivity.newInstance(getActivity()));
                break;
            case R.id.credit_complete_profile:
                showDialogAndStartActivity(getString(R.string.credits_complete_profile), getString(R.string.credits_profile_positive), ProfileIntentHelper.newIntent(getActivity(), BaseProfile.ME, mUserPreferences.isLoggedInAsPage()));
                break;
            case R.id.credit_facebook:
                showDialogAndStartActivity(getString(R.string.credits_facebook), getString(R.string.credits_facebook_positive), CreateShoutDialogActivity.getIntent(getActivity()));
                break;
            case R.id.credit_invite_friends:
                showDialogAndShowFragment(getString(R.string.credits_invite), getString(R.string.credits_invite_positive), MenuHandler.FRAGMENT_INVITE_FRIENDS);
                break;
            case R.id.credit_listen:
                showDialogAndShowFragment(getString(R.string.credits_listen), getString(R.string.credits_listen_positive), MenuHandler.FRAGMENT_INVITE_FRIENDS);
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
                .setNegativeButton(getString(R.string.credits_negative), (dialog, which) -> {
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
