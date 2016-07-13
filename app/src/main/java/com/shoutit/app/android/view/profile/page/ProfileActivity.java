package com.shoutit.app.android.view.profile.page;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.RtlUtils;
import com.shoutit.app.android.utils.rx.RxUtils;
import com.shoutit.app.android.view.shout.ShoutActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public abstract class ProfileActivity extends BaseActivity {

    protected static final String KEY_PROFILE_ID = "profile_id";
    private static final String KEY_OFFSET = "key_offset";
    protected static final int REQUEST_PROFILE_OPENED_FROM_PROFILE = 1;
    protected static final int REQUEST_CODE_FROM_EDIT_PROFILE = 2;
    protected static final int REQUEST_CODE_PROFILE_UPDATED_FROM_LISTENINGS = 3;

    @Bind(R.id.profile_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.profile_fragment_avatar)
    ImageView avatarImageView;
    @Bind(R.id.profile_app_bar)
    AppBarLayout appBarLayout;
    @Bind(R.id.profile_fragment_toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.profile_fragment_toolbar_subtitle)
    TextView toolbarSubtitle;
    @Bind(R.id.profile_reycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.profile_fragment_cover_image_view)
    ImageView coverImageView;
    @Bind(R.id.profile_fragment_toolbar)
    Toolbar toolbar;
    @Bind(R.id.profile_popup_menu_anchor_view)
    View popupAnchorView;

    @Inject
    ProfileAdapter adapter;
    @Inject
    Picasso picasso;
    @Inject
    ProfilePresenter presenter;

    private final AccelerateInterpolator toolbarInterpolator = new AccelerateInterpolator();
    private int lastVerticalOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        setUpToolbar();
        handleAppBarScrollAnimation();
        if (savedInstanceState != null) {
            // To scroll appbar after rotation
            recyclerView.scrollTo(0, recyclerView.getScrollY());
        }
        setUpAdapter();

        if (savedInstanceState == null) {
            presenter.refreshProfile();
        }

        presenter
                .getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        presenter
                .getAllAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter
                .getAvatarObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(loadAvatarAction());

        presenter
                .getCoverUrlObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(loadCoverAction());

        presenter
                .getToolbarTitleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(toolbarTitle));

        presenter
                .getToolbarSubtitleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(toolbarSubtitle));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getShareObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(shareProfileUrl());

        presenter.getShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivityForResult(ShoutActivity.newIntent(ProfileActivity.this, shoutId),
                                REQUEST_CODE_FROM_EDIT_PROFILE);
                    }
                });

        presenter.getBookmarkSuccesMessageObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getActionOnlyForLoggedInUserObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(ProfileActivity.this),
                        R.string.error_action_only_for_logged_in_user));

        presenter.getListenSuccessObservable()
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        setResult(RESULT_OK, null);
                    }
                })
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxUtils.listenMessageAction(this));

        presenter.getUnListenSuccessObservable()
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        setResult(RESULT_OK, null);
                    }
                })
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxUtils.unListenMessageAction(this));

    }

    @DrawableRes
    protected abstract int getAvatarPlaceholder();

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.profile_menu_share:
                presenter.getShareInitObserver().onNext(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpAdapter() {
        final boolean rtlEnable = RtlUtils.isRtlEnabled(this);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                final int viewType = adapter.getItemViewType(position);
                if (viewType == ProfileAdapter.VIEW_TYPE_SHOUT) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            int firstShoutPosition = 0;

            final int sideSpacing = getResources().getDimensionPixelSize(R.dimen.profile_side_spacing);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);

                if (position == RecyclerView.NO_POSITION) {
                    return;
                }

                final int viewType = parent.getAdapter().getItemViewType(position);
                if (viewType == ProfileAdapter.VIEW_TYPE_USER_NAME ||
                        viewType == ProfileAdapter.VIEW_TYPE_MY_PROFILE_USER_NAME ||
                        viewType == ProfileAdapter.VIEW_TYPE_MY_PROFILE_THREE_ICONS ||
                        viewType == ProfileAdapter.VIEW_TYPE_USER_INFO ||
                        viewType == ProfileAdapter.VIEW_TYPE_TAG_INFO ||
                        viewType == ProfileAdapter.VIEW_TYPE_THREE_ICONS) {
                    return;
                } else if (viewType == ProfileAdapter.VIEW_TYPE_SHOUT) {
                    if (adapter.getItemViewType(position - 1) != ProfileAdapter.VIEW_TYPE_SHOUT) {
                        firstShoutPosition = position - 1;
                    }

                    boolean isFirstPositionEven = firstShoutPosition % 2 == 0;
                    boolean isCurrentPositionEven = position % 2 == 0;
                    if (rtlEnable) {
                        isCurrentPositionEven = !isCurrentPositionEven;
                    }
                    if (isFirstPositionEven) {
                        if (isCurrentPositionEven) {
                            outRect.right = sideSpacing;
                        } else {
                            outRect.left = sideSpacing;
                        }
                    } else {
                        if (isCurrentPositionEven) {
                            outRect.left = sideSpacing;
                        } else {
                            outRect.right = sideSpacing;
                        }
                    }
                } else {
                    outRect.right = sideSpacing;
                    outRect.left = sideSpacing;
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @NonNull
    private Action1<String> shareProfileUrl() {
        return new Action1<String>() {
            @Override
            public void call(String shareUrl) {
                startActivity(Intent.createChooser(IntentHelper.getShareIntent(shareUrl),
                        getString(R.string.profile_share_profile_title)));
            }
        };
    }

    private Action1<String> loadCoverAction() {
        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                picasso.load(coverUrl)
                        .noPlaceholder()
                        .fit()
                        .centerCrop()
                        .error(R.drawable.pattern_placeholder)
                        .into(coverImageView);
            }
        };
    }

    private Action1<String> loadAvatarAction() {
        final int strokeSize = getResources().getDimensionPixelSize(R.dimen.profile_avatar_stroke);
        final int corners = getResources().getDimensionPixelSize(R.dimen.profile_avatar_radius);

        return new Action1<String>() {
            @Override
            public void call(String url) {
                picasso.load(url)
                        .placeholder(getAvatarPlaceholder())
                        .error(getAvatarPlaceholder())
                        .fit()
                        .centerCrop()
                        .transform(PicassoHelper.roundedWithStrokeTransformation(strokeSize, false, corners, "ProfileAvatar"))
                        .into(avatarImageView);
            }
        };
    }

    private void handleAppBarScrollAnimation() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                lastVerticalOffset = verticalOffset;
                float maxScroll = appBarLayout.getTotalScrollRange();
                float progressPercentage = ((float) (Math.abs(verticalOffset)) / maxScroll);

                if (progressPercentage > 0.5) {
                    avatarImageView.animate().scaleX(0).scaleY(0).setDuration(200).setInterpolator(toolbarInterpolator).start();
                } else {
                    avatarImageView.animate().scaleX(1).scaleY(1).setDuration(200).setInterpolator(toolbarInterpolator).start();
                }

                if (progressPercentage >= 0.8) {
                    toolbarTitle.setAlpha(progressPercentage);
                    toolbarSubtitle.setAlpha(progressPercentage);
                } else {
                    toolbarTitle.setAlpha(0);
                    toolbarSubtitle.setAlpha(0);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK &&
                (requestCode == REQUEST_PROFILE_OPENED_FROM_PROFILE ||
                        requestCode == REQUEST_CODE_FROM_EDIT_PROFILE ||
                requestCode == REQUEST_CODE_PROFILE_UPDATED_FROM_LISTENINGS)) {
            // Need to refresh profile if returned from other profile to refresh related data.
            // And need to refresh profile if one was just edited
            presenter.refreshProfile();
        } else if (requestCode == RESULT_OK) {
            super.onActivityResult(requestCode, requestCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_OFFSET, lastVerticalOffset);
        super.onSaveInstanceState(outState);
    }
}
