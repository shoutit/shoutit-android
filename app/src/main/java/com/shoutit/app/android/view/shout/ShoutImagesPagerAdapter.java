package com.shoutit.app.android.view.shout;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ShoutImagesPagerAdapter extends PagerAdapter {

    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final LayoutInflater inflater;
    @Nonnull
    private List<String> images = ImmutableList.of();

    @Inject
    public ShoutImagesPagerAdapter(@ForActivity Context context, @Nonnull Picasso picasso) {
        this.picasso = picasso;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<String> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final ImageView imageView = (ImageView) inflater.inflate(R.layout.shout_pager_image, container, false);

        picasso.load(images.get(position))
                .placeholder(R.drawable.pattern_placeholder)
                .error(R.drawable.pattern_placeholder)
                .fit()
                .centerCrop()
                .into(imageView);

        container.addView(imageView);

        return imageView;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
