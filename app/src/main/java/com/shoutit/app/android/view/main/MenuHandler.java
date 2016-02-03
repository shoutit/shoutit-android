package com.shoutit.app.android.view.main;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.R;
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

    @Inject
    MenuHandlerPresenter presenter;

    @Nonnull
    private final RxAppCompatActivity rxActivity;
    @Nonnull
    private final OnMenuItemSelectedListener onMenuItemSelectedListener;
    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final Target roundedTarget;

    @Inject
    public MenuHandler(@Nonnull RxAppCompatActivity rxActivity,
                       @Nonnull OnMenuItemSelectedListener onMenuItemSelectedListener,
                       @Nonnull Picasso picasso) {
        this.rxActivity = rxActivity;
        this.onMenuItemSelectedListener = onMenuItemSelectedListener;
        this.picasso = picasso;

        roundedTarget = PicassoHelper.getRoundedBitmapWithStrokeTarget(
                avatarImageView,
                rxActivity.getResources().getDimensionPixelSize(R.dimen.side_menu_avatar_stroke_size)
        );
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
                .subscribe(loadAvatarAction(roundedTarget));

        presenter.getCoverObservable()
                .compose(rxActivity.<String>bindToLifecycle())
                .subscribe(loadCoverAction());
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
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_CHATS);
                break;
            case R.id.menu_orders:
                onMenuItemSelectedListener.onMenuItemSelected(FRAGMENT_ORDERS);
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
        Toast.makeText(rxActivity, "Not implemented yet", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.menu_location_tv)
    public void showChangeLocationScreen() {
        Toast.makeText(rxActivity, "Not implemented yet", Toast.LENGTH_LONG).show();
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
    private Action1<String> loadAvatarAction(final Target roundedTarget) {
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
