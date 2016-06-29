package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.view.ReportDialog;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.chats.chatsfirstconversation.ChatFirstConversationActivity;
import com.shoutit.app.android.view.editprofile.EditProfileActivity;
import com.shoutit.app.android.view.listeners.ListenersActivity;
import com.shoutit.app.android.view.listenings.ListeningsActivity;
import com.shoutit.app.android.view.notifications.NotificationsActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;
import com.shoutit.app.android.view.verifyemail.VerifyEmailActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class UserOrPageProfileActivity extends ProfileActivity {

    private UserOrPageProfilePresenter presenter;
    private PopupMenu popupMenu;

    public static Intent newIntent(@Nonnull Context context, @Nonnull String userName) {
        return new Intent(context, UserOrPageProfileActivity.class)
                .putExtra(KEY_PROFILE_ID, userName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = (UserOrPageProfilePresenter) ((ProfileActivityComponent) getActivityComponent()).getPresenter();

        setUpPopupMenu();

        presenter.getWebUrlClickedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(webUrl -> {
                    startActivity(IntentHelper.websiteIntent(webUrl));
                });

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(userName -> {
                    startActivityForResult(
                            UserOrPageProfileActivity.newIntent(UserOrPageProfileActivity.this, userName),
                            REQUEST_PROFILE_OPENED_FROM_PROFILE);
                });

        presenter.getRefreshUserShoutsObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getUserUpdatesObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getRefreshPagesOrAdminsObservable()
                .compose(bindToLifecycle())
                .subscribe();

        // User Profile specific subscriptions
        presenter.getUserProfilePresenter().getOnChatIconClickedSubject()
                .compose(this.<ChatInfo>bindToLifecycle())
                .subscribe(chatInfo -> {
                    if (chatInfo.isUserLoggedIn()) {
                        final String conversationId = chatInfo.getConversationId();
                        if (chatInfo.isListener()) {
                            if (conversationId == null) {
                                startActivity(ChatFirstConversationActivity.newIntent(UserOrPageProfileActivity.this, false, chatInfo.getUsername()));
                            } else {
                                startActivity(ChatActivity.newIntent(UserOrPageProfileActivity.this, conversationId));
                            }
                        } else {
                            Toast.makeText(UserOrPageProfileActivity.this, R.string.profile_not_listening, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ColoredSnackBar.error(ColoredSnackBar.contentView(UserOrPageProfileActivity.this), R.string.error_action_only_for_logged_in_user, Snackbar.LENGTH_SHORT).show();
                    }
                });


        presenter.getReportSuccessObservable()
                .compose(this.bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(this), R.string.profile_report_succes));

        presenter.getMoreMenuOptionClickedSubject()
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    popupMenu.show();
                });

        presenter.getListeningObservable()
                .compose(bindToLifecycle())
                .subscribe();

        // My Profile specific subscriptions
        presenter.getMyProfilePresenter()
                .getEditProfileClickObservable()
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    startActivityForResult(
                            EditProfileActivity.newIntent(UserOrPageProfileActivity.this),
                            REQUEST_CODE_FROM_EDIT_PROFILE);
                });

        presenter.getMyProfilePresenter()
                .getListeningsClickObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    startActivityForResult(ListeningsActivity.newIntent(UserOrPageProfileActivity.this, false),
                            REQUEST_CODE_PROFILE_UPDATED_FROM_LISTENINGS);
                });

        presenter.getMyProfilePresenter()
                .getInterestsClickObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    startActivityForResult(ListeningsActivity.newIntent(UserOrPageProfileActivity.this, true),
                            REQUEST_CODE_PROFILE_UPDATED_FROM_LISTENINGS);
                });

        presenter.getMyProfilePresenter()
                .getListenersClickObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    startActivityForResult(ListenersActivity.newIntent(UserOrPageProfileActivity.this, User.ME),
                            REQUEST_CODE_PROFILE_UPDATED_FROM_LISTENINGS);
                });

        presenter.getMyProfilePresenter()
                .getVerifyAccountClickObservable()
                .compose(bindToLifecycle())
                .subscribe(o -> {
                    startActivity(VerifyEmailActivity.newIntent(UserOrPageProfileActivity.this));
                });

        presenter.getMyProfilePresenter().getNotificationsClickObservable()
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    startActivity(NotificationsActivity.newIntent(UserOrPageProfileActivity.this));
                });

        presenter.getSearchMenuItemClickObservable()
                .compose(this.<Intent>bindToLifecycle())
                .subscribe(this::startActivity);

        presenter.getUserProfilePresenter().getActionOnlyForLoggedInUserObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(UserOrPageProfileActivity.this),
                        R.string.error_action_only_for_logged_in_user));

        presenter.getSeeAllShoutsObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(userName -> {
                    startActivity(SearchShoutsResultsActivity.newIntent(
                            UserOrPageProfileActivity.this, null, userName, SearchPresenter.SearchType.PROFILE));
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_menu_search:
                presenter.onSearchMenuItemClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpPopupMenu() {
        popupMenu = new PopupMenu(this, popupAnchorView);
        popupMenu.inflate(R.menu.menu_profile_popup);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ReportDialog.show(UserOrPageProfileActivity.this, new Action1<String>() {
                    @Override
                    public void call(String reportBody) {
                        presenter.sendReportObserver().onNext(reportBody);
                    }
                });

                return true;
            }
        });
    }

    @Override
    protected int getAvatarPlaceholder() {
        return R.drawable.ic_rect_avatar_placeholder;
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        String userName = intent.getStringExtra(KEY_PROFILE_ID);
        if (userName == null && intent.getData() != null) {
            userName = intent.getData().getQueryParameter("username");
        }
        checkNotNull(userName);

        final ProfileActivityComponent component = DaggerProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .profileActivityModule(new ProfileActivityModule(userName))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
