package com.shoutit.app.android.view.promote;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.PromoteLabel;
import com.shoutit.app.android.dagger.ForActivity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.ButterKnife;

public class PromoteLabelsPagerAdapter extends PagerAdapter {

    @Nonnull
    private final LayoutInflater inflater;
    private final ShapeDrawable badgeDrawable;
    private List<PromoteLabel> items = new ArrayList<>();

    @Inject
    public PromoteLabelsPagerAdapter(@ForActivity Context context, @Nonnull LayoutInflater inflater) {
        this.inflater = inflater;
        final int badgeRadius = context.getResources().getDimensionPixelSize(R.dimen.promote_label_radius);
        final float[] radiuses = {0, 0, 0, 0, badgeRadius, badgeRadius, 0, 0};
        badgeDrawable = new ShapeDrawable(new RoundRectShape(radiuses, new RectF(), radiuses));
    }

    public void bindData(List<PromoteLabel> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view = inflater.inflate(R.layout.promote_label_item, container, false);

        final View backgroundView = ButterKnife.findById(view, R.id.promote_label_background);
        final TextView badgeTv = ButterKnife.findById(view, R.id.promote_label_badge_tv);
        final TextView labelText = ButterKnife.findById(view, R.id.promote_label_text);

        final PromoteLabel promoteLabel = items.get(position);

        backgroundView.setBackgroundColor(Color.parseColor(promoteLabel.getBgColor()));

        badgeTv.setText(promoteLabel.getName());
        badgeDrawable.getPaint().setColor(Color.parseColor(promoteLabel.getColor()));
        badgeTv.setBackground(badgeDrawable);

        labelText.setText(promoteLabel.getDescription());

        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
