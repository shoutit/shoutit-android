package com.shoutit.app.android.view.promote.promoted;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Label;
import com.shoutit.app.android.api.model.Promotion;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.utils.DateTimeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class PromotedActivity extends BaseDaggerActivity {

    private static final String KEY_PROMOTION_JSON = "promotion_json";
    private static final String KEY_SHOUT_TITLE = "shout_title";

    @Bind(R.id.promoted_toolbar)
    Toolbar toolbar;
    @Bind(R.id.promoted_label_background)
    View labelBackgroundView;
    @Bind(R.id.promoted_available_credits_tv)
    TextView creditsTv;
    @Bind(R.id.promoted_label_badge_tv)
    TextView badgeTv;
    @Bind(R.id.promoted_shout_title)
    TextView shoutTitleTv;
    @Bind(R.id.promoted_label_text)
    TextView labelTextTv;
    @Bind(R.id.promoted_days_tv)
    TextView daysTv;

    @Inject
    Gson gson;
    @Inject
    UserPreferences userPreferences;

    public static Intent newIntent(@Nonnull Context context,
                                   @Nonnull String promotionJson,
                                   @Nullable String shoutTitle) {
        return new Intent(context, PromotedActivity.class)
                .putExtra(KEY_SHOUT_TITLE, shoutTitle)
                .putExtra(KEY_PROMOTION_JSON, promotionJson);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promoted);
        ButterKnife.bind(this);

        setUpToolbar();

        final String promotionJson = checkNotNull(getIntent().getStringExtra(KEY_PROMOTION_JSON));
        final Promotion promotion = gson.fromJson(promotionJson, Promotion.class);
        final String shoutTitle = getIntent().getStringExtra(KEY_SHOUT_TITLE);

        userPreferences.getPageOrUserObservable()
                .compose(bindToLifecycle())
                .subscribe(user -> {
                    bindData(shoutTitle, promotion, user);
                });
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

    public void bindData(String shoutTitle, @Nonnull Promotion promotion, @Nonnull BaseProfile user) {
        final Label label = promotion.getLabel();

        shoutTitleTv.setText(shoutTitle);
        if (TextUtils.isEmpty(shoutTitle)) {
            shoutTitleTv.setVisibility(View.GONE);
        }

        final int badgeRadius = getResources().getDimensionPixelSize(R.dimen.promote_label_radius);
        final float[] radiuses = {0, 0, 0, 0, badgeRadius, badgeRadius, 0, 0};
        final ShapeDrawable badgeDrawable = new ShapeDrawable(new RoundRectShape(radiuses, new RectF(), radiuses));
        badgeDrawable.getPaint().setColor(Color.parseColor(label.getColor()));
        badgeTv.setBackground(badgeDrawable);
        badgeTv.setText(label.getName());

        labelBackgroundView.setBackgroundColor(Color.parseColor(label.getBgColor()));

        daysTv.setText(promotion.getDays() == null ?
                null : getResources().getQuantityString(
                R.plurals.plural_days, promotion.getDays(), promotion.getDays()));

        if (promotion.getDays() != null) {
            final String date = DateTimeUtils.getSlashedDate(promotion.getExpiresAt() * 1000L);
            labelTextTv.setText(getString(R.string.promoted_message_days, date));
        } else {
            labelTextTv.setText(getString(R.string.promoted_message_no_days));
        }

        //noinspection ConstantConditions
        creditsTv.setText(String.valueOf(user.getStats().getCredits()));
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
