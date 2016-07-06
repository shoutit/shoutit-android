package com.shoutit.app.android.view.createpage.pagedetails.common;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.R;

import java.util.List;

public class SpinnerAdapter extends BaseAdapter {

    private List<CategoryInfo> mCategoryInfos = ImmutableList.of();

    private final LayoutInflater mLayoutInflater;

    public SpinnerAdapter(LayoutInflater layoutInflater) {
        mLayoutInflater = layoutInflater;
    }

    public void setData(List<CategoryInfo> categoryInfos){
        mCategoryInfos = categoryInfos;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCategoryInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mCategoryInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        final CategoryInfo item = (CategoryInfo) getItem(position);
        return item.getSlug().hashCode();
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View inflate = mLayoutInflater.inflate(R.layout.spinner_item, parent, false);
        final TextView text = (TextView) inflate.findViewById(android.R.id.text1);
        text.setText(mCategoryInfos.get(position).getName());
        return inflate;
    }
}