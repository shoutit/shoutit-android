package com.shoutit.app.android.view.invitefriends;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.view.createshout.DialogsHelper;
import com.shoutit.app.android.view.invitefriends.contactsfriends.ContactsFriendsActivity;
import com.shoutit.app.android.view.invitefriends.contactsinvite.InviteContactsActivity;
import com.shoutit.app.android.view.invitefriends.facebookfriends.FacebookFriendsActivity;
import com.shoutit.app.android.view.invitefriends.suggestionspages.PagesSuggestionActivity;
import com.shoutit.app.android.view.invitefriends.suggestionsusers.UserSuggestionActivity;
import com.shoutit.app.android.facebook.FacebookHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteFacebookFriendsFragment extends BaseFragment {

    private Menu mMenu;
    private CallbackManager callbackManager;

    @Inject
    UserPreferences userPreferences;
    @Inject
    InviteFacebookFriendsPresenter presenter;

    @Bind(R.id.base_progress)
    View progressView;

    @Nonnull
    public static Fragment newInstance() {
        return new InviteFacebookFriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.invite_friends_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.getInvitationCodeObservable()
                .compose(bindToLifecycle())
                .subscribe(invitationCode -> {
                    FacebookHelper.showAppInviteDialog(getActivity(),
                            FacebookHelper.FACEBOOK_SHARE_APP_LINK,
                            callbackManager,
                            invitationCode);
                });

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent, @Nonnull FragmentModule fragmentModule, @Nullable Bundle savedInstanceState) {
        DaggerInviteFacebookFriendsFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }

    @OnClick({R.id.invite_friends_users, R.id.invite_friends_pages, R.id.invite_friends_find_facebook,
            R.id.invite_friends_find_contacts, R.id.invite_friends_invite_facebook,
            R.id.invite_friends_invite_twitter, R.id.invite_friends_share_app, R.id.invite_friends_invite_contacts})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.invite_friends_users:
                startActivity(UserSuggestionActivity.newIntent(getActivity()));
                break;
            case R.id.invite_friends_pages:
                startActivity(PagesSuggestionActivity.newIntent(getActivity()));
                break;
            case R.id.invite_friends_find_facebook:
                if (userPreferences.isNormalUser()) {
                    startActivity(FacebookFriendsActivity.newIntent(getActivity()));
                } else {
                    showOnlyForLoggedUserError();
                }
                break;
            case R.id.invite_friends_find_contacts:
                if (userPreferences.isNormalUser()) {
                    startActivity(ContactsFriendsActivity.newIntent(getActivity()));
                } else {
                    showOnlyForLoggedUserError();
                }
                break;
            case R.id.invite_friends_invite_facebook:
                presenter.initFbFriendInvite();
                break;
            case R.id.invite_friends_invite_twitter:
                shareThroughTwitter();
                break;
            case R.id.invite_friends_share_app:
                startActivity(IntentHelper.getShareIntent(getString(R.string.invite_app_invite_text)));
                break;
            case R.id.invite_friends_invite_contacts:
                startActivity(InviteContactsActivity.newIntent(getActivity()));
                break;
        }
    }

    @OnClick(R.id.invite_friend_find_friends_header)
    public void onFindFriendInfoClicked() {
        DialogsHelper.showDialog(getActivity(), R.string.invite_friend_find_friend_dialog);
    }

    @OnClick(R.id.invite_friend_invite_friends_header)
    public void onInviteFriendInfoClicked() {
        DialogsHelper.showDialog(getActivity(), R.string.invite_friend_invite_friend_dialog);
    }

    private void showOnlyForLoggedUserError() {
        ColoredSnackBar.error(
                ColoredSnackBar.contentView(getActivity()),
                R.string.error_action_only_for_logged_in_user, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void shareThroughTwitter() {
        final List<Intent> twitterShareIntents = IntentHelper.getTwitterShareIntent(
                getActivity().getPackageManager(), getString(R.string.invite_app_invite_text));

        if (!twitterShareIntents.isEmpty()) {
            if (twitterShareIntents.size() > 1) {
                final Intent chooser = Intent.createChooser(
                        twitterShareIntents.get(0), getString(R.string.invite_chooser_text));
                twitterShareIntents.remove(0);
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, twitterShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooser);
            } else {
                startActivity(twitterShareIntents.get(0));
            }
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()),
                    R.string.invite_no_twitter_app, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mMenu = menu;
        changeItemVisibility(false);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        changeItemVisibility(true);
    }

    private void changeItemVisibility(boolean visible) {
        for (int i = 0; i < mMenu.size(); i++) {
            mMenu.getItem(i).setVisible(visible);
        }
    }
}
