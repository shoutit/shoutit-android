package com.shoutit.app.android.view.main;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.BlurTransform;
import com.shoutit.app.android.utils.KeyboardHelper;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.RtlUtils;
import com.shoutit.app.android.view.conversations.ConversationsPagerFragment;
import com.shoutit.app.android.view.createshout.CreateShoutDialogActivity;
import com.shoutit.app.android.view.discover.DiscoverFragment;
import com.shoutit.app.android.view.home.HomeFragment;
import com.shoutit.app.android.view.invitefriends.InviteFriendsFragment;
import com.shoutit.app.android.view.location.LocationActivity;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsFragment;
import com.shoutit.app.android.view.settings.SettingsActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.uservoice.uservoicesdk.UserVoice;

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class MenuHandler {
    public static final String FRAGMENT_HOME = "fragment_home";
    public static final String FRAGMENT_DISCOVER = "fragment_discover";
    public static final String FRAGMENT_BROWSE = "fragment_browse";
    public static final String FRAGMENT_CHATS = "fragment_chats";
    public static final String FRAGMENT_PUBLIC_CHATS = "fragment_public_chats";
    public static final String FRAGMENT_INVITE_FRIENDS = "fragment_invite_friends";
    public static final String ACTIVITY_SETTINGS = "activity_settings";
    public static final String ACTIVITY_HELP = "activity_help";

    @Bind(R.id.menu_user_name_tv)
    TextView userNameTextView;
    @Bind(R.id.menu_avatar_iv)
    ImageView avatarImageView;
    @Bind(R.id.menu_cover_iv)
    ImageView coverImageView;
    @Bind(R.id.menu_location_tv)
    TextView locationTextView;
    @Bind(R.id.menu_flag_iv)
    ImageView flagImageView;
    @Bind(R.id.menu_version_name)
    TextView versionNameTextView;
    @Bind(R.id.menu_profile_badge)
    TextView notificationsBadgeTv;
    @Bind(R.id.menu_chat_badge)
    TextView chatsBadgeTv;

    @Bind(R.id.menu_home)
    CheckedTextView homeItem;
    @Bind(R.id.menu_discover)
    CheckedTextView discoverItem;
    @Bind(R.id.menu_browse)
    CheckedTextView browseItem;
    @Bind(R.id.menu_chat)
    CheckedTextView chatItem;

    @Nonnull
    private final RxAppCompatActivity rxActivity;
    @Nonnull
    private final OnMenuItemSelectedListener onMenuItemSelectedListener;
    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final MenuHandlerPresenter presenter;

    private List<CheckedTextView> selectableItems = ImmutableList.of();

    private static BiMap<String, Integer> viewTagViewIdMap = HashBiMap.create();

    static {
        viewTagViewIdMap.put(FRAGMENT_HOME, R.id.menu_home);
        viewTagViewIdMap.put(FRAGMENT_DISCOVER, R.id.menu_discover);
        viewTagViewIdMap.put(FRAGMENT_BROWSE, R.id.menu_browse);
        viewTagViewIdMap.put(FRAGMENT_CHATS, R.id.menu_chat);
        viewTagViewIdMap.put(FRAGMENT_INVITE_FRIENDS, R.id.menu_invite_friends);
        viewTagViewIdMap.put(ACTIVITY_HELP, R.id.menu_help);
        viewTagViewIdMap.put(ACTIVITY_SETTINGS, R.id.menu_settings);
    }

    public MenuHandler(@Nonnull final RxAppCompatActivity rxActivity,
                       @Nonnull OnMenuItemSelectedListener onMenuItemSelectedListener,
                       @Nonnull Picasso picasso,
                       @Nonnull UserPreferences userPreferences,
                       @Nonnull MenuHandlerPresenter presenter) {
        this.rxActivity = rxActivity;
        this.onMenuItemSelectedListener = onMenuItemSelectedListener;
        this.picasso = picasso;
        this.userPreferences = userPreferences;
        this.presenter = presenter;
    }

    public void initMenu(@Nonnull View view) {
        initMenu(view, R.id.menu_home);
    }

    public void initMenu(@Nonnull View view, @IdRes int id) {
        ButterKnife.bind(this, view);
        selectableItems = ImmutableList.of(homeItem, discoverItem, browseItem, chatItem);
        setData(id);
    }

    public void setData(@IdRes int id) {
        selectItem(id);

        RtlUtils.setTextDirection(rxActivity, locationTextView);

        presenter.getNameObservable()
                .compose(rxActivity.<String>bindToLifecycle())
                .subscribe(RxTextView.text(userNameTextView));

        presenter.getCityObservable()
                .compose(rxActivity.<String>bindToLifecycle())
                .subscribe(RxTextView.text(locationTextView));

        presenter.getAvatarObservable()
                .compose(rxActivity.<String>bindToLifecycle())
                .subscribe(loadAvatarAction());

        presenter.getCoverObservable()
                .compose(rxActivity.<String>bindToLifecycle())
                .subscribe(loadCoverAction());

        presenter.getCountryCodeObservable()
                .compose(rxActivity.<Integer>bindToLifecycle())
                .subscribe(loadFlag());

        presenter.getVersionNameObservable()
                .compose(rxActivity.<String>bindToLifecycle())
                .subscribe(RxTextView.text(versionNameTextView));
    }

    @NonNull
    private Action1<Integer> loadFlag() {
        final Target roundedBitmapTarget = PicassoHelper.getRoundedBitmapTarget(rxActivity, flagImageView);
        return new Action1<Integer>() {
            @Override
            public void call(@DrawableRes Integer flagId) {
                picasso.load(flagId)
                        .into(roundedBitmapTarget);
            }
        };
    }

    @OnClick({R.id.menu_home, R.id.menu_discover, R.id.menu_browse, R.id.menu_chat, R.id.menu_settings, R.id.menu_help, R.id.menu_invite_friends})
    public void onMenuItemSelected(View view) {
        selectMenuItem(viewTagViewIdMap.inverse().get(view.getId()));
    }

    public void selectMenuItem(@Nonnull String viewTag) {
        switch (viewTag) {
            case FRAGMENT_HOME:
            case FRAGMENT_DISCOVER:
            case FRAGMENT_BROWSE:
            case FRAGMENT_INVITE_FRIENDS:
                selectFragment(viewTag);
                break;
            case FRAGMENT_CHATS:
                if (userPreferences.isNormalUser()) {
                    selectFragment(viewTag);
                } else {
                    showLoginActivity();
                }
                break;
            case ACTIVITY_SETTINGS:
                rxActivity.startActivity(SettingsActivity.newIntent(rxActivity));
                break;
            case ACTIVITY_HELP:
                UserVoice.launchUserVoice(rxActivity);
                break;
            default:
                throw new RuntimeException("Unknown menu item with tag: " + viewTag);
        }

        KeyboardHelper.hideSoftKeyboard(rxActivity);
    }

    private void selectFragment(@Nonnull String viewTag) {
        selectItem(viewTagViewIdMap.get(viewTag));
        onMenuItemSelectedListener.onMenuItemSelected(viewTag);
        setToolbarElevation(viewTagViewIdMap.get(viewTag) != R.id.menu_chat);
    }

    public void setToolbarElevation(boolean enable) {
        final ActionBar actionBar = rxActivity.getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        actionBar.setElevation(enable ?
                rxActivity.getResources().getDimensionPixelSize(R.dimen.toolbar_elevation) : 0f);
    }

    private void showLoginActivity() {
        rxActivity.startActivity(LoginIntroActivity.newIntent(rxActivity));
    }

    private void selectItem(@IdRes int id) {
        for (CheckedTextView item : selectableItems) {
            item.setChecked(item.getId() == id);
        }
    }

    @IdRes
    public int getSelectedItem() {
        for (CheckedTextView item : selectableItems) {
            if (item.isChecked()) return item.getId();
        }
        return -1;
    }

    @OnClick({R.id.menu_avatar_iv, R.id.menu_user_name_tv})
    public void startUserProfile() {
        if (userPreferences.isNormalUser()) {
            rxActivity.startActivity(UserOrPageProfileActivity.newIntent(rxActivity, User.ME));
        } else {
            showLoginActivity();
        }
    }

    @OnClick(R.id.menu_location_change_tv)
    public void showChangeLocationScreen() {
        rxActivity.startActivity(LocationActivity.newIntent(rxActivity));
    }

    @OnClick(R.id.menu_new_shout_btn)
    public void newShoutClick() {
        if (userPreferences.isNormalUser()) {
            rxActivity.startActivity(CreateShoutDialogActivity.getIntent(rxActivity));
        } else {
            showLoginActivity();
        }
    }

    @NonNull
    private Action1<String> loadCoverAction() {
        final Transformation blurTransformation = new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                return new BlurTransform(rxActivity).transform(source, true);
            }

            @Override
            public String key() {
                return "menuCover";
            }
        };

        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                picasso.load(coverUrl)
                        .fit()
                        .centerCrop()
                        .transform(blurTransformation)
                        .into(coverImageView);
            }
        };
    }

    @NonNull
    private Action1<String> loadAvatarAction() {
        final int strokeSize = rxActivity.getResources().getDimensionPixelSize(R.dimen.side_menu_avatar_stroke_size);

        return new Action1<String>() {
            @Override
            public void call(String avatarUrl) {
                picasso.load(avatarUrl)
                        .error(R.drawable.ic_avatar_placeholder)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .resizeDimen(R.dimen.side_menu_avatar_size, R.dimen.side_menu_avatar_size)
                        .centerCrop()
                        .transform(PicassoHelper.getCircularBitmapWithStrokeTarget(strokeSize, "MenuAvatar"))
                        .into(avatarImageView);
            }
        };
    }

    public static Fragment getFragmentForTag(String fragmentTag) {
        switch (fragmentTag) {
            case FRAGMENT_HOME:
                return HomeFragment.newInstance();
            case FRAGMENT_DISCOVER:
                return DiscoverFragment.newInstance();
            case FRAGMENT_BROWSE:
                return SearchShoutsResultsFragment.newInstance(null, null, SearchPresenter.SearchType.BROWSE);
            case FRAGMENT_CHATS:
                return ConversationsPagerFragment.newInstance();
            case FRAGMENT_PUBLIC_CHATS:
                return ConversationsPagerFragment.newInstance(true);
            case FRAGMENT_INVITE_FRIENDS:
                return InviteFriendsFragment.newInstance();
            default:
                throw new RuntimeException("Unknown fragment tag");
        }
    }

    public void setStats(int messageCount, int notificationsCount) {
        chatsBadgeTv.setVisibility(messageCount > 0 ? View.VISIBLE : View.GONE);
        chatsBadgeTv.setText(String.valueOf(messageCount));

        notificationsBadgeTv.setVisibility(notificationsCount > 0 ? View.VISIBLE : View.GONE);
        notificationsBadgeTv.setText(String.valueOf(notificationsCount));
    }

    public void setDiscoverMenuItem() {
        onMenuItemSelected(discoverItem);
    }
}
