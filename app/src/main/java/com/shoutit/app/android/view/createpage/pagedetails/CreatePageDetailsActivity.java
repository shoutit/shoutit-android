package com.shoutit.app.android.view.createpage.pagedetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ToolbarUtils;
import com.shoutit.app.android.view.main.MainActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePageDetailsActivity extends BaseActivity implements CreatePageDetailsPresenter.Listener {

    private static class SpinnerAdapter extends BaseAdapter {

        private List<CreatePageDetailsPresenter.CategoryInfo> mCategoryInfos = ImmutableList.of();

        private final LayoutInflater mLayoutInflater;

        private SpinnerAdapter(LayoutInflater layoutInflater) {
            mLayoutInflater = layoutInflater;
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
            final CreatePageDetailsPresenter.CategoryInfo item = (CreatePageDetailsPresenter.CategoryInfo) getItem(position);
            return item.getSlug().hashCode();
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View inflate = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            final TextView text = (TextView) inflate.findViewById(android.R.id.text1);
            text.setText(mCategoryInfos.get(position).getName());
            return inflate;
        }
    }

    private static final String EXTRA_SELECTED_CATEGORY = "EXTRA_SELECTED_CATEGORY";

    @Bind(R.id.create_page_details_spinner)
    Spinner mCreatePageDetailsSpinner;
    @Bind(R.id.create_page_details_name)
    EditText mCreatePageDetailsName;
    @Bind(R.id.create_page_details_name_layout)
    TextInputLayout mCreatePageDetailsNameLayout;
    @Bind(R.id.create_page_details_full_name)
    EditText mCreatePageDetailsFullName;
    @Bind(R.id.create_page_details_full_name_layout)
    TextInputLayout mCreatePageDetailsFullNameLayout;
    @Bind(R.id.create_page_details_email)
    EditText mCreatePageDetailsEmail;
    @Bind(R.id.create_page_details_email_layout)
    TextInputLayout mCreatePageDetailsEmailLayout;
    @Bind(R.id.create_page_details_password)
    EditText mCreatePageDetailsPassword;
    @Bind(R.id.create_page_details_password_layout)
    TextInputLayout mCreatePageDetailsPasswordLayout;
    @Bind(R.id.create_page_details_toolbar)
    Toolbar mCreatePageCategoryToolbar;

    public static Intent newIntent(Context context, String selectedCategory) {
        return new Intent(context, CreatePageDetailsActivity.class).putExtra(EXTRA_SELECTED_CATEGORY, selectedCategory);
    }

    @Inject
    CreatePageDetailsPresenter mPresenter;

    private SpinnerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_page_details_activity);
        ButterKnife.bind(this);

        mAdapter = new SpinnerAdapter(LayoutInflater.from(this));
        mCreatePageDetailsSpinner.setAdapter(mAdapter);
        ToolbarUtils.setupToolbar(mCreatePageCategoryToolbar, R.string.create_page_category_title, this);

        mPresenter.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unregister();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final String selectedCategory = getIntent().getStringExtra(EXTRA_SELECTED_CATEGORY);
        final CreatePageDetailsActivityComponent build = DaggerCreatePageDetailsActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .createPageDetailsModule(new CreatePageDetailsModule(selectedCategory))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        build.inject(this);
        return build;
    }

    @Override
    public void setCategories(List<CreatePageDetailsPresenter.CategoryInfo> categoryInfos) {
        notifyAdapter(categoryInfos);
    }

    private void notifyAdapter(List<CreatePageDetailsPresenter.CategoryInfo> categoryInfos) {
        mAdapter.mCategoryInfos = categoryInfos;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgress(boolean show) {

    }

    @Override
    public void error() {

    }

    @Override
    public void emptyEmail() {

    }

    @Override
    public void fullNameEmpty() {

    }

    @Override
    public void passwordEmpty() {

    }

    @Override
    public void startMainActivity() {
        ActivityCompat.finishAffinity(this);
        startActivity(MainActivity.newIntent(this));
    }

    @Override
    public void nameEmpty() {

    }

    @OnClick(R.id.create_page_details_button)
    public void onClick() {
        mPresenter.passCreatePageData(new CreatePageDetailsPresenter.CreatePageData((CreatePageDetailsPresenter.CategoryInfo) mAdapter.getItem(mCreatePageDetailsSpinner.getSelectedItemPosition()), mCreatePageDetailsName.getText().toString(), mCreatePageDetailsFullName.getText().toString(), mCreatePageDetailsEmail.getText().toString(), mCreatePageDetailsPassword.getText().toString()));
    }


}