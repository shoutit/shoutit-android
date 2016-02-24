package com.shoutit.app.android.view.shout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
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

    @Inject
    ShoutPresenter presenter;
    @Inject
    ShoutAdapter adapter;

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
        setUpAdapter();

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
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String userName) {
                        Toast.makeText(ShoutActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
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
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);

                if (position == RecyclerView.NO_POSITION) {
                    return;
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