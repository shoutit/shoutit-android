package com.shoutit.app.android.view.promote;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.PromoteLabel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.ButterKnife;

public class PromoteLabelsPager extends PagerAdapter {

    @Nonnull
    private final LayoutInflater inflater;
    private List<PromoteLabel> items = new ArrayList<>();

    @Inject
    public PromoteLabelsPager(@Nonnull LayoutInflater inflater) {
        this.inflater = inflater;
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
        badgeTv.setBackgroundColor(Color.parseColor(promoteLabel.getColor()));
        labelText.setText(promoteLabel.getDescription());

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
