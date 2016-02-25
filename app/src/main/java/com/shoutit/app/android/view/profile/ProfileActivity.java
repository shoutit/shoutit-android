package com.shoutit.app.android.view.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.ShoutitApp;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.data.adapter.ProfileAdapter;
import com.shoutit.app.android.data.event.shouts.ShoutCreatedEvent;
import com.shoutit.app.android.data.util.LoadMoreHelper;
import com.shoutit.app.android.data.util.UserUtils;
import com.shoutit.app.android.presenter.ProfilePresenter;
import com.shoutit.app.android.ui.util.BitmapUtils;
import com.shoutit.app.android.ui.util.ProfileGridSpacingItemDecoration;
import com.shoutit.app.android.ui.util.TakeBackModel;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class ProfileActivity extends BaseActivity {

    public static final int RC_EDIT_PROFILE = 1340;

    @Bind(R.id.profile_fragment_avatar)
    ImageView avatarImageView;
    @Bind(R.id.app_bar)
    AppBarLayout appBarLayout;
    @Bind(R.id.profile_fragment_toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.profile_fragment_toolbar_subtitle)
    TextView toolbarSubtitle;
    @Bind(R.id.profile_reycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.profile_fragment_cover_image_view)
    ImageView coverImageView;
    @Bind(R.id.profile_fab)
    FloatingActionButton addShoutFab;
    @Bind(R.id.profile_progress_bar)
    ProgressBar progressBar;

    @Inject
    ProfilePresenter presenter;

    private ProfileAdapter adapter;
    private final AccelerateInterpolator toolbarInterpolator = new AccelerateInterpolator();

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, ProfileActivity.class);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_new_profile;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShoutitApp.get(this).inject(this);

        ButterKnife.bind(this);

        UserUtils.checkForSkippedUser(shoutitPreferences,
                ProfileActivity.this,
                new TakeBackModel(ProfileActivity.class.getName(), ""));

        otto.register(this);

        handleAppBarScrollAnimation();

        adapter = new ProfileAdapter(this);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                final int viewType = adapter.getItemViewType(position);
                if (viewType == ProfileAdapter.VIEW_TYPE_USER || viewType == ProfileAdapter.VIEW_TYPE_SHOW_MORE) {
                    return 2;
                } else {
                    return 1;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        final int gridSideSpacing = getResources().getDimensionPixelSize(R.dimen.profile_shout_grid_side_spacing);
        recyclerView.addItemDecoration(new ProfileGridSpacingItemDecoration(gridSideSpacing));
        recyclerView.setAdapter(adapter);

        presenter
                .getProgressObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressBar));

        presenter
                .getAllAdapterItemsObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        RxRecyclerView.scrollEvents(recyclerView)
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore(gridLayoutManager, adapter))
                .subscribe(presenter.getLoadMoreShoutsSubject());

        presenter
                .getAvatarObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<Uri>bindToLifecycle())
                .subscribe(loadAvatarAction());

        presenter
                .getCoverUrlObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<String>bindToLifecycle())
                .subscribe(loadCoverAction());

        presenter
                .getToolbarTitleObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(toolbarTitle));

        presenter
                .getToolbarSubtitleObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(toolbarSubtitle));

        presenter.getShoutsErrorsResponse()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Snackbar.make(findViewById(android.R.id.content),
                                R.string.error_main_message, Snackbar.LENGTH_LONG)
                                .show();
                    }
                });

        RxView.clicks(addShoutFab)
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startActivity(new Intent(ProfileActivity.this, ShoutCreationActivity.class));
                    }
                });

        presenter.getShareObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<String>bindToLifecycle())
                .subscribe(shareProfileUrl());
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

    @Override
    protected Toolbar getActionBarToolbar() {
        return (Toolbar) findViewById(R.id.profile_fragment_toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_menu_share:
                presenter.shareProfileInitObserver().onNext(null);
                return true;
            case R.id.profile_menu_buy:
            case R.id.profile_menu_search:
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean isNavigationDrawerEnabled() {
        return true;
    }

    private Action1<String> loadCoverAction() {
        return new Action1<String>() {
            @Override
            public void call(String coverUrl) {
                Glide.with(ProfileActivity.this)
                        .load("http://lorempixel.com/800/600/animals/")
                        .placeholder(R.drawable.image_placeholder)
                        .centerCrop()
                        .into(coverImageView);
            }
        };
    }

    private Action1<Uri> loadAvatarAction() {
        return new Action1<Uri>() {
            @Override
            public void call(Uri uri) {
                Glide.with(ProfileActivity.this)
                        .load(uri)
                        .asBitmap()
                        .placeholder(R.drawable.ic_drawer_profile)
                        .error(R.drawable.ic_drawer_profile)
                        .centerCrop()
                        .into(new BitmapImageViewTarget(avatarImageView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                final Bitmap bitmapWithStroke = BitmapUtils
                                        .getRoundedCornerBitmap(
                                                resource,
                                                Color.WHITE,
                                                (int) getResources().getDimension(R.dimen.profile_avatar_radius),
                                                (int) getResources().getDimension(R.dimen.profile_avatar_stroke),
                                                ProfileActivity.this);
                                avatarImageView.setImageBitmap(bitmapWithStroke);
                            }
                        });
            }
        };
    }

    private void handleAppBarScrollAnimation() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_EDIT_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean profilePictureChanged = data.getBooleanExtra(EditProfileActivity.EXTRA_PICTURE_CHANGED, false);
                if (profilePictureChanged) {
                    presenter.refreshProfileObserver().onNext(null);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe
    public void onShoutCreated(ShoutCreatedEvent event) {
        presenter.getNewShoutSubject().onNext(event.getShout());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.otto.unregister(this);
        ButterKnife.unbind(this);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final ProfileActivityComponent component = DaggerProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
