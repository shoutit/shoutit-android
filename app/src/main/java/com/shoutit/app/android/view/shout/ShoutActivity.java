package com.shoutit.app.android.view.shout;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.gson.Gson;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Promotion;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.model.MobilePhoneResponse;
import com.shoutit.app.android.utils.AppseeHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.RtlUtils;
import com.shoutit.app.android.utils.UpNavigationHelper;
import com.shoutit.app.android.view.ReportDialog;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.chats.chatsfirstconversation.ChatFirstConversationActivity;
import com.shoutit.app.android.view.conversations.ConversationsActivity;
import com.shoutit.app.android.view.createshout.edit.EditShoutActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.profile.ProfileIntentHelper;
import com.shoutit.app.android.view.profile.tagprofile.TagProfileActivity;
import com.shoutit.app.android.view.promote.PromoteActivity;
import com.shoutit.app.android.view.promote.promoted.PromotedActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.main.MainSearchActivity;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;
import com.shoutit.app.android.view.videoconversation.OutgoingVideoCallActivity;

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
    private static final int EDIT_SHOUT_REQUEST_CODE = 3001;
    private static final int REQUEST_CODE_PROMOTE = 2;

    @Bind(R.id.shout_toolbar)
    Toolbar toolbar;
    @Bind(R.id.shout_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.shout_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.shout_bottom_toolbar)
    View bottomBar;
    @Bind(R.id.shout_bottom_bar_call_or_promote)
    TextView callOrPromoteTextView;
    @Bind(R.id.shout_bottom_bar_video_call_or_edit)
    TextView videoCallOrEditTextView;
    @Bind(R.id.shout_bottom_bar_chat_or_chats)
    TextView chatOrChatsTextView;
    @Bind(R.id.shout_bottom_bar_more)
    TextView showMoreIcon;
    @Bind(R.id.shout_item_not_exist_tv)
    TextView shoutNotExistTv;

    @Inject
    ShoutPresenter presenter;
    @Inject
    ShoutAdapter adapter;
    @Inject
    UserPreferences userPreferences;
    @Inject
    Gson gson;

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

        AppseeHelper.start(this);

        setUpActionBar();
        setUpBottomBar();
        setUpAdapter();

        presenter.getBottomBarDataObservable()
                .compose(this.<ShoutPresenter.BottomBarData>bindToLifecycle())
                .subscribe(setUpBottomBar());

        presenter.getAllAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getShoutNotFoundErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(throwable -> {
                    shoutNotExistTv.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.GONE);
                });

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        presenter.getRelatedShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(shoutId -> {
                    startActivity(ShoutActivity.newIntent(ShoutActivity.this, shoutId));
                });

        presenter.getSeeAllRelatedShoutObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(shoutId -> {
                    startActivity(SearchShoutsResultsActivity.newIntent(
                            ShoutActivity.this, null, shoutId, SearchPresenter.SearchType.RELATED_SHOUTS));
                });

        presenter.getTitleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(title -> {
                    final ActionBar supportActionBar = getSupportActionBar();
                    assert supportActionBar != null;
                    supportActionBar.setTitle(title);
                });

        presenter.getUserShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(shoutId -> {
                    startActivity(ShoutActivity.newIntent(ShoutActivity.this, shoutId));
                });

        presenter.getVisitProfileObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(user -> {
                    startActivity(ProfileIntentHelper.newIntent(ShoutActivity.this, user));
                });

        presenter.getAddToCartSubject()
                .compose(this.<String>bindToLifecycle())
                .subscribe(s -> {
                    Toast.makeText(ShoutActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                });

        presenter.getOnCategoryClickedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(categorySlug -> {
                    startActivity(TagProfileActivity.newIntent(ShoutActivity.this, categorySlug));
                });

        presenter.getCallErrorObservable()
                .compose(this.<ResponseOrError<MobilePhoneResponse>>bindToLifecycle())
                .subscribe(responseOrError -> {
                    if (responseOrError.isData()) {
                        final String phoneNumber = responseOrError.data().getMobile();

                        new AlertDialog.Builder(ShoutActivity.this)
                                .setMessage(getString(R.string.call_dialog_message, phoneNumber))
                                .setPositiveButton(getString(R.string.call_dialog_positive_button), (dialog, which) -> {
                                    startCall(phoneNumber);
                                })
                                .setNegativeButton(getString(R.string.dialog_cancel_button), null)
                                .show();

                    } else {
                        Snackbar.make(findViewById(android.R.id.content), R.string.no_phone_number_error, Snackbar.LENGTH_SHORT).show();
                    }
                });

        presenter.getHasMobilePhoneObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(hasPhoneNumber -> {
                    callOrPromoteTextView.setEnabled(hasPhoneNumber);
                    callOrPromoteTextView.setAlpha(hasPhoneNumber ? 1f : 0.5f);
                });

        presenter.getDeleteShoutResponseObservable()
                .compose(this.<Response<Object>>bindToLifecycle())
                .subscribe(responseBody -> {
                    if (responseBody.isSuccess()) {
                        setResult(RESULT_OK);
                        finish();
                        Toast.makeText(ShoutActivity.this, R.string.delete_shout_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), R.string.delete_shout_error, Snackbar.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    Snackbar.make(findViewById(android.R.id.content), R.string.delete_shout_error, Snackbar.LENGTH_SHORT).show();
                });

        presenter.getShowDeleteDialogObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(aBoolean -> {
                    new AlertDialog.Builder(ShoutActivity.this)
                            .setTitle(R.string.delete_shout_dialog_title)
                            .setMessage(getString(R.string.delete_shout_dialog_message))
                            .setPositiveButton(getString(R.string.delete_shout_dialog_button), (dialog, which) -> {
                                presenter.getDeleteShoutObserver().onNext(null);

                            })
                            .setNegativeButton(getString(R.string.dialog_cancel_button), null)
                            .show();
                });

        presenter.getReportShoutObservable()
                .compose(this.<Response<Object>>bindToLifecycle())
                .subscribe(objectResponse -> {
                    if (objectResponse.isSuccess()) {
                        ColoredSnackBar.success(findViewById(android.R.id.content), R.string.report_send_success, Snackbar.LENGTH_SHORT).show();
                    } else {
                        ColoredSnackBar.error(findViewById(android.R.id.content), R.string.error_default, Snackbar.LENGTH_SHORT);
                    }
                }, ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getRefreshShoutsObservable()
                .compose(this.bindToLifecycle())
                .subscribe();

        RxView.clicks(videoCallOrEditTextView)
                .compose(bindToLifecycle())
                .subscribe(presenter.getVideoOrEditClickSubject());

        presenter.getVideoCallClickedObservable()
                .compose(bindToLifecycle())
                .subscribe(shoutOwnerProfile -> {
                    startActivity(OutgoingVideoCallActivity.newIntent(
                            shoutOwnerProfile.getName(),
                            shoutOwnerProfile.getUsername(),
                            shoutOwnerProfile.getImage(),
                            ShoutActivity.this));
                });

        presenter.getEditShoutClickedObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(aBoolean -> {
                    startActivityForResult(EditShoutActivity.newIntent(mShoutId, ShoutActivity.this), EDIT_SHOUT_REQUEST_CODE);
                });

        presenter.getOnlyForLoggedInUserObservable()
                .compose(this.bindToLifecycle())
                .subscribe(o -> {
                    ColoredSnackBar.error(ColoredSnackBar.contentView(ShoutActivity.this), R.string.error_action_only_for_logged_in_user, Snackbar.LENGTH_SHORT).show();
                });

        presenter.getBookmarkSuccesMessageObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getLikeApiMessage()
                .compose(this.<String>bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getShareObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(shareUrl -> {
                    startActivity(Intent.createChooser(
                            IntentHelper.getShareIntent(shareUrl),
                            getString(R.string.shout_share)));
                });

        presenter.getShowPromoteObservable()
                .compose(bindToLifecycle())
                .subscribe(shout -> {
                    startActivityForResult(
                            PromoteActivity.newIntent(this, shout.getTitle(), shout.getId()),
                            REQUEST_CODE_PROMOTE);
                });

        presenter.getShowPromotedObservable()
                .compose(bindToLifecycle())
                .subscribe(shout -> {
                    final Promotion promotion = shout.getPromotion();
                    final String promotionJson = gson.toJson(promotion, Promotion.class);

                    startActivity(PromotedActivity.newIntent(
                            ShoutActivity.this, promotionJson, shout.getTitle()));
                });

        presenter.getMarkAsObservable()
                .compose(bindToLifecycle())
                .subscribe();
    }

    private void startCall(String phoneNumber) {
        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (PermissionHelper.checkPermissions(this,
                MainActivity.REQUST_CODE_CALL_PHONE_PERMISSION,
                ColoredSnackBar.contentView(this),
                R.string.permission_call_phone_explanation,
                new String[]{Manifest.permission.CALL_PHONE})) {
            startActivity(callIntent);
        }
    }

    @NonNull
    private Action1<ShoutPresenter.BottomBarData> setUpBottomBar() {
        final PopupMenu popupMenu = new PopupMenu(toolbar.getContext(), showMoreIcon);
        popupMenu.inflate(R.menu.menu_shout_bottom_bar);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_report:
                    ReportDialog.show(ShoutActivity.this,
                            reportBody -> presenter.sendReportObserver().onNext(reportBody));
                    return true;
                case R.id.menu_delete_shout:
                    presenter.getShowDeleteDialogObserver().onNext(null);
                    return true;
                default:
                    return false;
            }
        });

        return bottomBarData -> {
            final boolean isUserShoutOwner = bottomBarData.isUserShoutOwner();

            if (isUserShoutOwner) {
                callOrPromoteTextView.setEnabled(true);
                callOrPromoteTextView.setAlpha(1f);
                callOrPromoteTextView.setText(bottomBarData.isPromoted() ?
                        R.string.shout_bottom_bar_promoted : R.string.shout_bottom_bar_promote);

                ImageHelper.setStartCompoundRelativeDrawable(callOrPromoteTextView, R.drawable.ic_promote);
                ImageHelper.setStartCompoundRelativeDrawable(videoCallOrEditTextView, R.drawable.ic_edit_green);

                videoCallOrEditTextView.setText(R.string.shout_bottom_bar_edit);

                chatOrChatsTextView.setText(R.string.shout_bottom_bar_chats);
                chatOrChatsTextView.setOnClickListener(v ->
                        startActivity(ConversationsActivity.newIntent(ShoutActivity.this)));

            } else {
                callOrPromoteTextView.setText(R.string.shout_bottom_bar_call);

                chatOrChatsTextView.setText(R.string.shout_bottom_bar_chat);

                ImageHelper.setStartCompoundRelativeDrawable(callOrPromoteTextView, R.drawable.ic_call_green);
                ImageHelper.setStartCompoundRelativeDrawable(videoCallOrEditTextView, R.drawable.ic_video_chat_red);

                videoCallOrEditTextView.setText(R.string.shout_bottom_bar_video_call);

                chatOrChatsTextView.setOnClickListener(v -> {
                    if (bottomBarData.isNormalUser()) {
                        if (bottomBarData.isHasConversation()) {
                            startActivity(ChatActivity.newIntent(ShoutActivity.this, bottomBarData.getConversationId()));
                        } else {
                            startActivity(ChatFirstConversationActivity.newIntent(ShoutActivity.this, true, mShoutId));
                        }
                    } else {
                        ColoredSnackBar.error(ColoredSnackBar.contentView(ShoutActivity.this), R.string.error_action_only_for_logged_in_user, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            ImageHelper.setStartCompoundRelativeDrawable(showMoreIcon, R.drawable.ic_more_white);
            showMoreIcon.setOnClickListener(v -> popupMenu.show());

            popupMenu.getMenu().findItem(R.id.menu_delete_shout).setVisible(isUserShoutOwner);
            popupMenu.getMenu().findItem(R.id.menu_report).setVisible(!isUserShoutOwner);

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
        };
    }

    @OnClick(R.id.shout_bottom_bar_call_or_promote)
    public void onCallOrPromoteClicked() {
        presenter.callOrPromoteObserver().onNext(null);
    }

    private void setUpAdapter() {
        final GridLayoutManager layoutManager = new MyGridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new MyGridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == ShoutAdapter.VIEW_TYPE_USER_SHOUTS) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });

        final boolean rtlEnable = RtlUtils.isRtlEnabled(this);
        final int spacing = getResources().getDimensionPixelSize(R.dimen.shout_item_padding);
        final int bottomSpacing = getResources().getDimensionPixelSize(R.dimen.last_item_margin);
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
                    if (rtlEnable ? position % 2 == 1 : position % 2 == 0) {
                        outRect.left = spacing;
                    } else {
                        outRect.right = spacing;
                    }
                } else {
                    outRect.right = spacing;
                    outRect.left = spacing;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        final ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shouts_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.shouts_chats).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new UpNavigationHelper(this) {
                    @Override
                    public void handleActivityFinish() {
                        setResult(RESULT_OK);
                        super.handleActivityFinish();
                    }
                }.onUpButtonClicked();
                return true;
            case R.id.shouts_search:
                startActivity(MainSearchActivity.newIntent(this));
                return true;
            case R.id.shouts_share:
                presenter.onShareClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == EDIT_SHOUT_REQUEST_CODE || requestCode == REQUEST_CODE_PROMOTE) && resultCode == RESULT_OK) {
            presenter.refreshShoutsObserver().onNext(null);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        mShoutId = intent.getStringExtra(KEY_SHOUT_ID);
        if (mShoutId == null && intent.getData() != null) {
            mShoutId = intent.getData().getQueryParameter("id");
        }
        checkNotNull(mShoutId);

        final ShoutActivityComponent component = DaggerShoutActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .shoutActivityModule(new ShoutActivityModule(mShoutId))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
