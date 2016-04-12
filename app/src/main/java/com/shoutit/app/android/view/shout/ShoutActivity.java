package com.shoutit.app.android.view.shout;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.model.MobilePhoneResponse;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.chats.chatsfirstconversation.ChatFirstConversationActivity;
import com.shoutit.app.android.view.createshout.edit.EditShoutActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.profile.tagprofile.TagProfileActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.main.MainSearchActivity;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;
import com.shoutit.app.android.view.videoconversation.VideoConversationActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Response;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class ShoutActivity extends BaseActivity {

    private static final String KEY_SHOUT_ID = "shout_id";

    @Bind(R.id.shout_toolbar)
    Toolbar toolbar;
    @Bind(R.id.shout_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.shout_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.shout_bottom_toolbar)
    View bottomBar;
    @Bind(R.id.shout_bottom_bar_call_or_delete)
    TextView callOrDeleteTextView;
    @Bind(R.id.shout_bottom_bar_video_call_or_edit)
    TextView videoCallOrEditTextView;
    @Bind(R.id.shout_bottom_bar_chat_or_chats)
    TextView chatOrChatsTextView;
    @Bind(R.id.shout_bottom_bar_more)
    TextView showMoreIcon;

    @Inject
    ShoutPresenter presenter;
    @Inject
    ShoutAdapter adapter;
    @Inject
    UserPreferences userPreferences;

    private String mShoutId;

    public static Intent newIntent(@Nonnull Context context, @Nonnull String shoutId) {
        return new Intent(context, ShoutActivity.class)
                .putExtra(KEY_SHOUT_ID, shoutId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shout);
        ButterKnife.bind(this);

        setUpActionBar();
        setUpBottomBar();
        setUpAdapter();

        presenter.getIsUserShoutOwnerObservable()
                .compose(this.<ShoutPresenter.BottomBarData>bindToLifecycle())
                .subscribe(setUpBottomBar());

        presenter.getAllAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        presenter.getRelatedShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(ShoutActivity.newIntent(ShoutActivity.this, shoutId));
                    }
                });

        presenter.getSeeAllRelatedShoutObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(SearchShoutsResultsActivity.newIntent(
                                ShoutActivity.this, null, shoutId, SearchPresenter.SearchType.RELATED_SHOUTS));
                    }
                });

        presenter.getTitleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String title) {
                        getSupportActionBar().setTitle(title);
                    }
                });

        presenter.getUserShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(ShoutActivity.newIntent(ShoutActivity.this, shoutId));
                    }
                });

        presenter.getVisitProfileObservable()
                .compose(this.<User>bindToLifecycle())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        startActivity(UserOrPageProfileActivity.newIntent(ShoutActivity.this, user.getUsername()));
                    }
                });

        presenter.getAddToCartSubject()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(ShoutActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.getOnCategoryClickedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String categorySlug) {
                        startActivity(TagProfileActivity.newIntent(ShoutActivity.this, categorySlug));
                    }
                });

        presenter.getShoutOwnerNameObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String name) {
                        userPreferences.setShoutOwnerName(name);
                    }
                });

        presenter.getCallErrorObservable()
                .compose(this.<ResponseOrError<MobilePhoneResponse>>bindToLifecycle())
                .subscribe(new Action1<ResponseOrError<MobilePhoneResponse>>() {
                    @Override
                    public void call(ResponseOrError<MobilePhoneResponse> responseOrError) {
                        if (responseOrError.isData()) {
                            final String phoneNumber = responseOrError.data().getMobile();

                            new AlertDialog.Builder(ShoutActivity.this)
                                    .setMessage(getString(R.string.call_dialog_message, phoneNumber))
                                    .setPositiveButton(getString(R.string.call_dialog_positive_button), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startCall(phoneNumber);
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.dialog_cancel_button), null)
                                    .show();

                        } else {
                            Snackbar.make(findViewById(android.R.id.content), R.string.no_phone_number_error, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

        presenter.getHasMobilePhoneObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean hasPhoneNumber) {
                        if (getString(R.string.shout_bottom_bar_delete).equals(callOrDeleteTextView.getText().toString())) {
                            hasPhoneNumber = true;
                        }
                        callOrDeleteTextView.setEnabled(hasPhoneNumber);
                        callOrDeleteTextView.setAlpha(hasPhoneNumber ? 1f : 0.5f);
                    }
                });

        presenter.getDeleteShoutResponseObservable()
                .compose(this.<Response<Object>>bindToLifecycle())
                .subscribe(new Action1<Response<Object>>() {
                    @Override
                    public void call(Response<Object> responseBody) {
                        if (responseBody.isSuccess()) {
                            setResult(RESULT_OK);
                            finish();
                            Toast.makeText(ShoutActivity.this, R.string.delete_shout_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), R.string.delete_shout_error, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.delete_shout_error, Snackbar.LENGTH_SHORT).show();
                    }
                });

        presenter.getShowDeleteDialogObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        new AlertDialog.Builder(ShoutActivity.this)
                                .setTitle(R.string.delete_shout_dialog_title)
                                .setMessage(getString(R.string.delete_shout_dialog_message))
                                .setPositiveButton(getString(R.string.delete_shout_dialog_button), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        presenter.getDeleteShoutObserver().onNext(null);

                                    }
                                })
                                .setNegativeButton(getString(R.string.dialog_cancel_button), null)
                                .show();
                    }
                });

        presenter.getReportShoutObservable()
                .compose(this.<Response<Object>>bindToLifecycle())
                .subscribe(new Action1<Response<Object>>() {
                    @Override
                    public void call(Response<Object> objectResponse) {
                        if (objectResponse.isSuccess()) {
                            ColoredSnackBar.success(findViewById(android.R.id.content), R.string.report_send_success, Snackbar.LENGTH_SHORT).show();
                        } else {
                            ColoredSnackBar.error(findViewById(android.R.id.content), R.string.error_default, Snackbar.LENGTH_SHORT);
                        }
                    }
                }, ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getRefreshUserShoutsObservable()
                .compose(this.bindToLifecycle())
                .subscribe();

        RxView.clicks(videoCallOrEditTextView)
                .compose(bindToLifecycle())
                .subscribe(presenter.getVideoOrEditClickSubject());

        presenter.getVideoCallClickedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userId) {
                        startActivity(VideoConversationActivity.newIntent(null, userId, ShoutActivity.this));
                    }
                });

        presenter.getEditShoutClickedObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        startActivity(EditShoutActivity.newIntent(mShoutId, ShoutActivity.this));
                    }
                });

        presenter.getOnlyForLoggedInUserObservable()
                .compose(this.bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        ColoredSnackBar.error(ColoredSnackBar.contentView(ShoutActivity.this), R.string.error_action_only_for_logged_in_user, Snackbar.LENGTH_SHORT).show();
                    }
                });

    }

    private void startCall(String phoneNumber) {
        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (PermissionHelper.checkPermissions(this,
                MainActivity.REQUST_CODE_CALL_PHONE_PERMISSION,
                ColoredSnackBar.contentView(this),
                R.string.permission_call_phone_explanation,
                new String[]{Manifest.permission.CAMERA})) {
            startActivity(callIntent);
        }
    }

    @NonNull
    private Action1<ShoutPresenter.BottomBarData> setUpBottomBar() {
        final PopupMenu popupMenu = new PopupMenu(toolbar.getContext(), showMoreIcon);
        popupMenu.inflate(R.menu.menu_shout_bottom_bar);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                final EditText editText = new EditText(ShoutActivity.this);
                editText.setHint(R.string.report_dialog_hint);

                final int spacing = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
                new AlertDialog.Builder(ShoutActivity.this)
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

        return new Action1<ShoutPresenter.BottomBarData>() {
            @Override
            public void call(final ShoutPresenter.BottomBarData bottomBarData) {
                final boolean isUserShoutOwner = bottomBarData.isUserShoutOwner();

                ImageHelper.setStartCompoundRelativeDrawable(showMoreIcon,
                        isUserShoutOwner ? R.drawable.ic_more_disabled : R.drawable.ic_more_white);

                if (!isUserShoutOwner) {
                    showMoreIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupMenu.show();
                        }
                    });
                }

                callOrDeleteTextView.setCompoundDrawablesWithIntrinsicBounds(
                        isUserShoutOwner ? R.drawable.ic_delete_red : R.drawable.ic_call_green, 0, 0, 0);
                callOrDeleteTextView.setText(isUserShoutOwner ?
                        R.string.shout_bottom_bar_delete : R.string.shout_bottom_bar_call);

                if (isUserShoutOwner) {
                    callOrDeleteTextView.setEnabled(true);
                    callOrDeleteTextView.setAlpha(1f);
                }

                videoCallOrEditTextView.setCompoundDrawablesWithIntrinsicBounds(
                        isUserShoutOwner ? R.drawable.ic_edit_red : R.drawable.ic_video_chat_red, 0, 0, 0);
                videoCallOrEditTextView.setText(isUserShoutOwner ?
                        R.string.shout_bottom_bar_edit : R.string.shout_bottom_bar_video_call);

                chatOrChatsTextView.setText(isUserShoutOwner ?
                        R.string.shout_bottom_bar_chats : R.string.shout_bottom_bar_chat);
                chatOrChatsTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bottomBarData.isNormalUser()) {
                            if (bottomBarData.isHasConversation()) {
                                startActivity(ChatActivity.newIntent(ShoutActivity.this, bottomBarData.getConversationId(), true));
                            } else {
                                startActivity(ChatFirstConversationActivity.newIntent(ShoutActivity.this, true, mShoutId));
                            }
                        } else {
                            ColoredSnackBar.error(ColoredSnackBar.contentView(ShoutActivity.this), R.string.error_action_only_for_logged_in_user, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

                final int bottomBarHeight = getResources().getDimensionPixelSize(R.dimen.shout_bottom_bar);
                final ObjectAnimator animator = ObjectAnimator.ofFloat(bottomBar, "translationY", bottomBarHeight, 0);
                animator.setDuration(500)
                        .setStartDelay(1000);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        bottomBar.setVisibility(View.VISIBLE);
                    }
                });
                animator.start();
            }
        };
    }

    @OnClick(R.id.shout_bottom_bar_call_or_delete)
    public void onCallOrDeleteClicked() {
        presenter.callOrDeleteObserver().onNext(null);
    }

    private void setUpAdapter() {
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == ShoutAdapter.VIEW_TYPE_USER_SHOUTS) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });

        final int spacing = getResources().getDimensionPixelSize(R.dimen.shout_item_padding);
        final int bottomSpacing = getResources().getDimensionPixelSize(R.dimen.shout_bottom_bar);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);

                if (position == RecyclerView.NO_POSITION) {
                    return;
                }

                if (position == adapter.getItemCount() - 1) {
                    outRect.bottom = bottomSpacing;
                }

                final int viewType = parent.getAdapter().getItemViewType(position);
                if (viewType == ShoutAdapter.VIEW_TYPE_USER_SHOUTS) {
                    if (position % 2 == 0) {
                        outRect.left = spacing;
                    } else {
                        outRect.right = spacing;
                    }
                } else if (viewType == ShoutAdapter.VIEW_TYPE_RELATED_SHOUTS_CONTAINER) {
                    return;
                } else {
                    outRect.right = spacing;
                    outRect.left = spacing;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shouts_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.shouts_search:
                startActivity(MainSearchActivity.newIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        mShoutId = checkNotNull(intent.getStringExtra(KEY_SHOUT_ID));

        final ShoutActivityComponent component = DaggerShoutActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .shoutActivityModule(new ShoutActivityModule(this, mShoutId))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
