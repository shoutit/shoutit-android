package com.shoutit.app.android.view.promote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.PromoteOption;
import com.shoutit.app.android.api.model.PromoteResponse;
import com.shoutit.app.android.api.model.Promotion;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.createshout.DialogsHelper;
import com.shoutit.app.android.view.promote.promoted.PromotedActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class PromoteActivity extends BaseActivity {

    private static final String KEY_SHOUT_NAME = "shout_name";
    private static final String KEY_SHOUT_ID = "shout_id";

    @Bind(R.id.promote_toolbar)
    Toolbar toolbar;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.promote_recycler_view)
    RecyclerView recyclerView;
    
    @Inject
    PromoteAdapter adapter;
    @Inject
    PromotePresenter presenter;
    @Inject
    Gson gson;

    public static Intent newIntent(Context context,
                                   @Nullable String shoutName,
                                   @Nonnull String shoutId) {
        return new Intent(context, PromoteActivity.class)
                .putExtra(KEY_SHOUT_NAME, shoutName)
                .putExtra(KEY_SHOUT_ID, shoutId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote_shout);
        ButterKnife.bind(this);

        setUpToolbar();

        recyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        presenter.getAdapterItemsObservable()
                .compose(bindToLifecycle())
                .subscribe(adapter);

        presenter.getErrorObservable()
                .compose(bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getNotEnoughCreditsObservable()
                .compose(bindToLifecycle())
                .subscribe(DialogsHelper.showDialogAction(this, R.string.promote_not_enough_money));

        presenter.getSuccessfullyPromotedObservable()
                .compose(bindToLifecycle())
                .subscribe(shoutTitleAndPromoteResponse -> {
                    final String shoutTitle = shoutTitleAndPromoteResponse.param1();
                    final PromoteResponse promoteResponse = shoutTitleAndPromoteResponse.param2();

                    Toast.makeText(this, promoteResponse.getSuccess(), Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();

                    final String promotionJson = gson.toJson(promoteResponse.getPromotion(), Promotion.class);
                    startActivity(PromotedActivity.newIntent(this, promotionJson, shoutTitle));
                });

        presenter.getShowConfirmDialogObservable()
                .compose(bindToLifecycle())
                .subscribe(this::showConfirmDialog);
    }

    private void showConfirmDialog(@Nonnull PromoteOption promoteOption) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.promote_confirm_text, promoteOption.getName(), promoteOption.getCredits()))
                .setPositiveButton(R.string.promote_confirm_button, (dialog, which) -> {
                    presenter.buyOption(promoteOption);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_cancel_button, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.promote_ab_title);
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
        final String shoutTitle = getIntent().getStringExtra(KEY_SHOUT_NAME);
        final String shoutId = checkNotNull(getIntent().getStringExtra(KEY_SHOUT_ID));

        final PromoteActivityComponent component = DaggerPromoteActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .promoteActivityModule(new PromoteActivityModule(shoutId, shoutTitle))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }


}
