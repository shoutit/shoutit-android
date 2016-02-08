package com.shoutit.app.android.view.main;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.view.home.HomeFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class MenuHandler {
    public static final String FRAGMENT_HOME = "fragment_home";
    public static final String FRAGMENT_DISCOVER = "fragment_discover";
    public static final String FRAGMENT_BROWSE = "fragment_browse";
    public static final String FRAGMENT_CHATS = "fragment_chats";
    public static final String FRAGMENT_ORDERS = "fragment_orders";
    public static final String FRAGMENT_SETTINGS = "fragment_settings";
    public static final String FRAGMENT_HELP = "fragment_help";
    public static final String FRAGMENT_INVITE_FRIENDS = "fragment_invite_friends";

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

    @Inject
    MenuHandlerPresenter presenter;

    @Nonnull
    private final RxAppCompatActivity rxActivity;
    @Nonnull
    private final OnMenuItemSelectedListener onMenuItemSelectedListener;
    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final UserPreferences userPreferences;

    @Inject
    public MenuHandler(@Nonnull final RxAppCompatActivity rxActivity,
                       @Nonnull OnMenuItemSelectedListener onMenuItemSelectedListener,
                       @Nonnull Picasso picasso,
                       @Nonnull UserPreferences userPreferences) {
        this.rxActivity = rxActivity;
        this.onMenuItemSelectedListener = onMenuItemSelectedListener;
        this.picasso = picasso;
        this.userPreferences = userPreferences;
    }

    public void initMenu(@Nonnull View view) {
        ButterKnife.bind(this, view);
        setData();
    }

    public void setData() {
        presenter.getNameObservable()
                .compose(rxActivity.<String>bindToLifecycle())
                .subscribe(RxTextView.text(userNameTextView));

        presenter.getLocationObservable()
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

    @OnClick({R.id.menu_home, R.id.menu_discover, R.id.menu_browse, R.id.menu_chat,
            R.id.menu_orders, R.id.menu_settings, R.id.menu_help, R.id.menu_invite_frirends})
    public void onMenuItemSelected(View view) {
        switch (view.getId()) {
            case R.id.menu_home:
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_HOME);
                break;
            case R.id.menu_discover:
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_DISCOVER);
                break;
            case R.id.menu_browse:
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_BROWSE);
                break;
            case R.id.menu_chat:
                if (userPreferences.isUserLoggedIn()) {
                    onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_CHATS);
                    Toast.makeText(rxActivity, "Not implemented yet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(rxActivity, "Hello. Log in popup here", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_orders:
                if (userPreferences.isUserLoggedIn()) {
                    onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_ORDERS);
                    Toast.makeText(rxActivity, "Not implemented yet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(rxActivity, "Hello. Log in popup here", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_settings:
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_SETTINGS);
                break;
            case R.id.menu_help:
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_HELP);
                break;
            case R.id.menu_invite_frirends:
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_INVITE_FRIENDS);
                break;
            default:
                throw new RuntimeException("Unknown menu item");
        }
    }

    @OnClick({R.id.menu_avatar_iv, R.id.menu_user_name_tv})
    public void startUserProfile() {
        if (userPreferences.isUserLoggedIn()) {
            Toast.makeText(rxActivity, "Not implemented yet", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(rxActivity, "Hello. Log in popup here", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.menu_location_change_tv)
    public void showChangeLocationScreen() {
        Toast.makeText(rxActivity, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.menu_new_shout_btn)
    public void newShoutClick() {
        if (userPreferences.isUserLoggedIn()) {
            Toast.makeText(rxActivity, "Not implemented yet", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(rxActivity, "Hello. Log in popup here", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private Action1<String> loadCoverAction() {
        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                picasso.load(coverUrl)
                        .error(R.drawable.pattern_bg)
                        .fit()
                        .centerCrop()
                        .into(coverImageView);
            }
        };
    }

    @NonNull
    private Action1<String> loadAvatarAction() {
        final Target roundedTarget = PicassoHelper.getRoundedBitmapWithStrokeTarget(
                avatarImageView,
                rxActivity.getResources().getDimensionPixelSize(R.dimen.side_menu_avatar_stroke_size)
        );

        return new Action1<String>() {
            @Override
            public void call(String avatarUrl) {
                picasso.load(avatarUrl)
                        .error(R.drawable.ic_avatar_placeholder)
                        .resizeDimen(R.dimen.side_menu_avatar_size, R.dimen.side_menu_avatar_size)
                        .into(roundedTarget);
            }
        };
    }

    public static Fragment getFragmentForTag(String fragmentTag) {
        switch (fragmentTag) {
            case FRAGMENT_HOME:
                return HomeFragment.newInstance();
            case FRAGMENT_DISCOVER:
            case FRAGMENT_BROWSE:
            case FRAGMENT_CHATS:
            case FRAGMENT_ORDERS:
            case FRAGMENT_SETTINGS:
            case FRAGMENT_HELP:
            case FRAGMENT_INVITE_FRIENDS:
                return new Fragment();
            default:
                throw new RuntimeException("Unknown fragment tag");

        }
    }
}
