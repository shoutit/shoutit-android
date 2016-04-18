package com.shoutit.app.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.utils.PriceUtils;

import java.util.List;

public class CurrencySpinnerAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private final int mLayout;
    private final int mDropdownLayout;
    private List<PriceUtils.SpinnerCurrency> list;

    public CurrencySpinnerAdapter(@StringRes int startingText,
                                  Context context,
                                  @LayoutRes int layout,
                                  @LayoutRes int dropdownLayout) {
        mDropdownLayout = dropdownLayout;
        list = ImmutableList.of(new PriceUtils.SpinnerCurrency(context.getString(startingText), context.getString(startingText)));
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
        return list.get(position).getCode().hashCode();
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) mLayoutInflater.inflate(mLayout, parent, false);
        view.setText(list.get(position).getCode());

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) mLayoutInflater.inflate(mDropdownLayout, parent, false);
        view.setText(list.get(position).getName());
        return view;
    }

    public void setData(@NonNull List<PriceUtils.SpinnerCurrency> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}