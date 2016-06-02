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
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.view.invitefriends.contactsfriends.ContactsFriendsActivity;
import com.shoutit.app.android.view.invitefriends.facebookfriends.FacebookFriendsActivity;
import com.shoutit.app.android.view.loginintro.FacebookHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteFriendsFragment extends BaseFragment {

    private static final String SHARE_APP_URL = "https://www.shoutit.com/app";
    private static final String SHARE_FACEBOOK_APP_LINK_URL = "https://fb.me/1224908360855680";

    private Menu mMenu;
    private CallbackManager callbackManager;

    @Nonnull
    public static Fragment newInstance() {
        return new InviteFriendsFragment();
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
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent, @Nonnull FragmentModule fragmentModule, @Nullable Bundle savedInstanceState) {
        DaggerInviteFriendsFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }

    @OnClick({R.id.invite_friends_users, R.id.invite_friends_pages, R.id.invite_friends_find_facebook, R.id.invite_friends_find_contacts, R.id.invite_friends_invite_facebook, R.id.invite_friends_invite_twitter, R.id.invite_friends_share_app})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.invite_friends_users:
                startActivity(UserSuggestionActivity.newUserIntent(getActivity()));
                break;
            case R.id.invite_friends_pages:
                startActivity(UserSuggestionActivity.newPagesIntent(getActivity()));
                break;
            case R.id.invite_friends_find_facebook:
                startActivity(FacebookFriendsActivity.newIntent(getActivity()));
                break;
            case R.id.invite_friends_find_contacts:
                startActivity(ContactsFriendsActivity.newIntent(getActivity()));
                break;
            case R.id.invite_friends_invite_facebook:
                FacebookHelper.showAppInviteDialog(this, SHARE_FACEBOOK_APP_LINK_URL, callbackManager);
                break;
            case R.id.invite_friends_invite_twitter:
                shareThroughTwitter();
                break;
            case R.id.invite_friends_share_app:
                startActivity(IntentHelper.getShareIntent(SHARE_APP_URL));
                break;
        }
    }

    private void shareThroughTwitter() {
        final List<Intent> twitterShareIntents = IntentHelper.getTwitterShareIntent(
                getActivity().getPackageManager(), getString(R.string.invite_text));

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
