package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.view.shout.ShoutActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class ProfileActivity extends BaseActivity {

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_OFFSET = "key_offset";
    private static final int REQUEST_PROFILE_OPENED_FROM_PROFILE = 1;

    @Bind(R.id.profile_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.profile_fragment_avatar)
    ImageView avatarImageView;
    @Bind(R.id.profile_app_bar)
    protected AppBarLayout appBarLayout;
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
    ProfilePresenter presenter;
    @Inject
    ProfileAdapter adapter;
    @Inject
    Picasso picasso;

    private final AccelerateInterpolator toolbarInterpolator = new AccelerateInterpolator();
    private int lastVerticalOffset;
    private PopupMenu popupMenu;

    public static Intent newIntent(@Nonnull Context context, @Nonnull String userName) {
        return new Intent(context, ProfileActivity.class)
                .putExtra(KEY_USER_NAME, userName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        setUpToolbar();
        setUpPopupMenu();
        handleAppBarScrollAnimation();
        if (savedInstanceState != null) {
            // To scroll appbar after rotation
            recyclerView.scrollTo(0, recyclerView.getScrollY());
        }
        setUpAdapter();

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
                        startActivity(ShoutActivity.newIntent(ProfileActivity.this, shoutId));
                    }
                });

        presenter.getActionOnlyForLoggedInUserObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(ProfileActivity.this),
                        R.string.error_action_only_for_logged_in_user));

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                        startActivityForResult(
                                ProfileActivity.newIntent(ProfileActivity.this, userName),
                                REQUEST_PROFILE_OPENED_FROM_PROFILE);
                    }
                });

        // User Profile specific subscriptions
        presenter.getUserProfilePresenter().getOnChatIconClickedSubject()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(ProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.getUserProfilePresenter().getMoreMenuOptionClickedSubject()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        popupMenu.show();
                    }
                });

        presenter.getUserProfilePresenter().getActionOnlyForLoggedInUserObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(ProfileActivity.this),
                        R.string.error_action_only_for_logged_in_user));

        // My Profile specific subscriptions
        presenter.getMyProfilePresenter().getEditProfileClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(ProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.getMyProfilePresenter().getNotificationsClickObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object ignore) {
                        Toast.makeText(ProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
    }

    private void setUpPopupMenu() {
        popupMenu = new PopupMenu(this, popupAnchorView);
        popupMenu.inflate(R.menu.menu_profile_popup);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(ProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
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
                        viewType == ProfileAdapter.VIEW_TYPE_USER_INFO ||
                        viewType == ProfileAdapter.VIEW_TYPE_THREE_ICONS) {
                    return;
                } else if (viewType == ProfileAdapter.VIEW_TYPE_SHOUT) {
                    if (adapter.getItemViewType(position - 1) != ProfileAdapter.VIEW_TYPE_SHOUT) {
                        firstShoutPosition = position - 1;
                    }

                    boolean isFirstPositionEven = firstShoutPosition % 2 == 0;
                    boolean isCurrentPositionEven = position % 2 == 0;
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
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, shareUrl);
                startActivity(Intent.createChooser(intent, getString(R.string.profile_share_profile_title)));
            }
        };
    }

    private Action1<String> loadCoverAction() {
        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                picasso.load(coverUrl)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.pattern_placeholder)
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
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
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
    public void finish() {
        setResult(RESULT_OK, null);
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_PROFILE_OPENED_FROM_PROFILE) {
            // Need to refresh profile if returned from other profile to refresh related data
            presenter.refreshProfile();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_OFFSET, lastVerticalOffset);
        super.onSaveInstanceState(outState);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        final String userName = checkNotNull(intent.getStringExtra(KEY_USER_NAME));

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
