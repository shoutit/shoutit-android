package com.shoutit.app.android.view.shout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.profile.myprofile.MyProfileActivity;
import com.shoutit.app.android.view.profile.userprofile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    View showMoreIcon;

    @Inject
    ShoutPresenter presenter;
    @Inject
    ShoutAdapter adapter;
    @Inject
    UserPreferences userPreferences;

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
                .compose(this.<Boolean>bindToLifecycle())
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
                        Toast.makeText(ShoutActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
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
                        final User currentUser = userPreferences.getUser();
                        final boolean isMyProfile = currentUser != null && user.getUsername().equals(currentUser.getUsername());
                        startActivity(UserProfileActivity.newIntent(ShoutActivity.this,
                                user.getUsername(), user.getType(),
                                isMyProfile ? MyProfileActivity.class : UserProfileActivity.class));
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
    }

    @NonNull
    private Action1<Boolean> setUpBottomBar() {
        final PopupMenu popupMenu = new PopupMenu(toolbar.getContext(), showMoreIcon);
        popupMenu.inflate(R.menu.menu_shout_bottom_bar);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(ShoutActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        showMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        return new Action1<Boolean>() {
            @Override
            public void call(Boolean isUserShoutOwner) {
                callOrDeleteTextView.setCompoundDrawablesWithIntrinsicBounds(
                        isUserShoutOwner ? R.drawable.ic_delete_red : R.drawable.ic_call_green, 0, 0, 0);
                callOrDeleteTextView.setText(isUserShoutOwner ?
                        R.string.shout_bottom_bar_delete : R.string.shout_bottom_bar_call);

                videoCallOrEditTextView.setCompoundDrawablesWithIntrinsicBounds(
                        isUserShoutOwner ? R.drawable.ic_edit_red : R.drawable.ic_video_chat_red, 0, 0, 0);
                videoCallOrEditTextView.setText(isUserShoutOwner ?
                        R.string.shout_bottom_bar_edit : R.string.shout_bottom_bar_video_call);

                chatOrChatsTextView.setText(isUserShoutOwner ?
                        R.string.shout_bottom_bar_chats : R.string.shout_bottom_bar_chat);

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
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.shout_bottom_bar_video_call_or_edit)
    public void onVideoCallOrEditClicked() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.shout_bottom_bar_chat_or_chats)
    public void onChatClicked() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        final String shoutId = checkNotNull(intent.getStringExtra(KEY_SHOUT_ID));

        final ShoutActivityComponent component = DaggerShoutActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .shoutActivityModule(new ShoutActivityModule(this, shoutId))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
