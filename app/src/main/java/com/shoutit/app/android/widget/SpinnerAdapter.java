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

public class SpinnerAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private final int mLayout;
    private final int mDropdownLayout;
    private List<Pair<String, String>> list;

    public SpinnerAdapter(@StringRes int startingText,
                          Context context,
                          @LayoutRes int layout,
                          @LayoutRes int dropdownLayout) {
        mDropdownLayout = dropdownLayout;
        list = ImmutableList.of(new Pair<>("", context.getString(startingText)));
        mLayout = layout;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).first.hashCode();
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) mLayoutInflater.inflate(mLayout, parent, false);
        view.setText(list.get(position).second);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) mLayoutInflater.inflate(mDropdownLayout, parent, false);
        view.setText(list.get(position).second);
        return view;
    }

    public void setData(@NonNull List<Pair<String, String>> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}