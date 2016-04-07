package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.chats.chatsfirstconversation.ChatFirstConversationActivity;
import com.shoutit.app.android.view.editprofile.EditProfileActivity;
import com.shoutit.app.android.view.notifications.NotificationsActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;

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
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String webUrl) {
                        startActivity(IntentHelper.websiteIntent(webUrl));
                    }
                });

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                        startActivityForResult(
                                UserOrPageProfileActivity.newIntent(UserOrPageProfileActivity.this, userName),
                                REQUEST_PROFILE_OPENED_FROM_PROFILE);
                    }
                });

        // User Profile specific subscriptions
        presenter.getUserProfilePresenter().getOnChatIconClickedSubject()
                .compose(this.<ChatInfo>bindToLifecycle())
                .subscribe(new Action1<ChatInfo>() {
                    @Override
                    public void call(ChatInfo chatInfo) {
                        final String conversationId = chatInfo.getConversationId();
                        if (conversationId == null) {
                            startActivity(ChatFirstConversationActivity.newIntent(UserOrPageProfileActivity.this, false, chatInfo.getUsername()));
                        } else {
                            startActivity(ChatActivity.newIntent(UserOrPageProfileActivity.this, conversationId, false));
                        }
                        Toast.makeText(UserOrPageProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.getReportSuccessObservable()
                .compose(this.bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(this), R.string.profile_report_succes));

        presenter.getMoreMenuOptionClickedSubject()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        popupMenu.show();
                    }
                });

        // My Profile specific subscriptions
        presenter.getMyProfilePresenter().getEditProfileClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        startActivityForResult(
                                EditProfileActivity.newIntent(UserOrPageProfileActivity.this),
                                REQUEST_CODE_FROM_EDIT_PROFILE);
                    }
                });

        presenter.getMyProfilePresenter().getNotificationsClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        startActivity(NotificationsActivity.newIntent(UserOrPageProfileActivity.this));
                    }
                });

        presenter.getSearchMenuItemClickObservable()
                .compose(this.<Intent>bindToLifecycle())
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        startActivity(intent);
                    }
                });

        presenter.getUserProfilePresenter().getActionOnlyForLoggedInUserObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(UserOrPageProfileActivity.this),
                        R.string.error_action_only_for_logged_in_user));

        presenter.getSeeAllShoutsObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                        startActivity(SearchShoutsResultsActivity.newIntent(
                                UserOrPageProfileActivity.this, null, userName, SearchPresenter.SearchType.PROFILE));
                    }
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
                final EditText editText = new EditText(UserOrPageProfileActivity.this);
                editText.setHint(R.string.report_dialog_hint);

                final int spacing = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
                new AlertDialog.Builder(UserOrPageProfileActivity.this)
                        .setTitle(R.string.shout_bottom_bar_report)
                        .setView(editText, spacing, spacing / 2, spacing, spacing / 2)
                        .setPositiveButton(getString(R.string.send_report_positive_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String reportBody = editText.getText().toString();
                                if (Strings.isNullOrEmpty(reportBody)) {
                                    editText.setError(getString(R.string.report_dialog_empty_error));
                                    dialog.dismiss();
                                    return;
                                }

                                presenter.sendReportObserver().onNext(reportBody);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_cancel_button), null)
                        .show();

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
        final String userName = checkNotNull(intent.getStringExtra(KEY_PROFILE_ID));

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
