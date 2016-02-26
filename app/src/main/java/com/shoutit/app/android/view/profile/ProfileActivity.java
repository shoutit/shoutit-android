package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.MyAndroidSchedulers;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
    private static final String KEY_PROFILE_TYPE = "profile_type";

    @Bind(R.id.profile_progress_bar)
    ProgressBar progressBar;
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
    @Bind(R.id.base_fab)
    FloatingActionButton fab;

    @Inject
    MyProfilePresenter presenter;
    @Inject
    MyProfileAdapter adapter;
    @Inject
    Picasso picasso;

    private final AccelerateInterpolator toolbarInterpolator = new AccelerateInterpolator();

    public static Intent newIntent(@Nonnull Context context, @Nonnull String userName, @Nonnull String profileType) {
        return new Intent(context, ProfileActivity.class)
                .putExtra(KEY_USER_NAME, userName)
                .putExtra(KEY_PROFILE_TYPE, profileType);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        handleAppBarScrollAnimation();
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

        presenter.getShoutsErrorsResponse()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        RxView.clicks(fab)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.getShareObservable()
                .observeOn(MyAndroidSchedulers.mainThread())
                .compose(this.<String>bindToLifecycle())
                .subscribe(shareProfileUrl());
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
        final int gridSideSpacing = getResources().getDimensionPixelSize(R.dimen.profile_shout_grid_side_spacing);
        //recyclerView.addItemDecoration(new ProfileGridSpacingItemDecoration(gridSideSpacing));
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
                picasso.load("http://lorempixel.com/800/600/animals/")
                        .fit()
                        .centerCrop()
                        .into(coverImageView);
            }
        };
    }

    private Action1<String> loadAvatarAction() {
        final Target target = PicassoHelper.getRoundedBitmapWithStrokeTarget(
                avatarImageView, getResources().getDimensionPixelSize(R.dimen.profile_avatar_stroke));

        return new Action1<String>() {
            @Override
            public void call(String url) {
                picasso.load(url)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .into(target);
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

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        final String userName = checkNotNull(intent.getStringExtra(KEY_USER_NAME));
        final String profileType = checkNotNull(intent.getStringExtra(KEY_PROFILE_TYPE));

        final ProfileActivityComponent component = DaggerProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .profileActivityModule(new ProfileActivityModule(userName, profileType))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
