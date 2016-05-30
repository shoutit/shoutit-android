package com.shoutit.app.android.view.invitefriends;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteFriendsFragment extends BaseFragment {

    private Menu mMenu;

    @Nonnull
    public static Fragment newInstance() {
        return new InviteFriendsFragment();
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
                break;
            case R.id.invite_friends_pages:
                break;
            case R.id.invite_friends_find_facebook:
                break;
            case R.id.invite_friends_find_contacts:
                break;
            case R.id.invite_friends_invite_facebook:
                break;
            case R.id.invite_friends_invite_twitter:
                break;
            case R.id.invite_friends_share_app:
                break;
        }
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
