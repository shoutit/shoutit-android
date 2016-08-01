package com.shoutit.app.android.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;


public abstract class BaseSpinnerAdapter<T> extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;
    private final int layoutId;
    private List<T> items = ImmutableList.of();

    public BaseSpinnerAdapter(Context context, @LayoutRes int layoutId) {
        this.layoutId = layoutId;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void bindData(@Nonnull Collection<T> newItems) {
        items = ImmutableList.<T>builder()
                .addAll(newItems)
                .build();
        notifyDataSetChanged();
    }

    protected abstract String getDisplayName(int position);

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        return getLayoutView(position, view, parent, layoutId);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        return getLayoutView(position, view, parent, layoutId);
    }

    public View getLayoutView(int position, View view, ViewGroup parent, @LayoutRes int layoutResId) {
        if (view == null) {
            view = mLayoutInflater.inflate(layoutResId, parent, false);
        }

        setData(view, position);

        return view;
    }

    private void setData(View view, int position) {
        final TextView textView = (TextView) view;

        final String name = getDisplayName(position);
        textView.setText(name);
    }

    public int getItemPosition(@Nonnull T item) {
        for (int i = 0; i < getCount(); i++) {
            if (items.get(i).equals(item)) {
                return i;
            }
        }

        throw new RuntimeException("Item do not belong to adapter: " + item.getClass().getSimpleName());
    }
}
