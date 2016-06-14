package com.shoutit.app.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class GenderSpinnerAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private final int mLayout;
    private final int mDropdownLayout;
    private String[] items = new String[]{};

    public GenderSpinnerAdapter(Context context,
                                @LayoutRes int layout,
                                @LayoutRes int dropdownLayout,
                                String[] items) {
        mDropdownLayout = dropdownLayout;
        mLayout = layout;
        this.items = items;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return items[position].hashCode();
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) mLayoutInflater.inflate(mLayout, parent, false);
        view.setText(items[position]);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) mLayoutInflater.inflate(mDropdownLayout, parent, false);
        view.setText(items[position]);
        return view;
    }
}