package com.shoutit.app.android.view.createpage.pagecategory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.UniversalAdapter;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ToolbarUtils;
import com.shoutit.app.android.utils.adapter.BaseViewHolderManager;
import com.shoutit.app.android.utils.adapter.EmptyViewHolder;
import com.shoutit.app.android.view.createpage.pagedetails.newuser.CreatePageDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreatePageCategoryActivity extends BaseActivity implements CreatePageCategoryPresenter.Listener {

    private static final String KEY_IS_FROM_REGISTRATION = "is_from_registration";

    class CategoryViewBinder extends ViewHolderManager.BaseViewHolder<CreatePageCategoryPresenter.CategoryItem> {

        private final View mView;
        @Bind(R.id.create_page_categories_icon)
        ImageView mCreatePageCategoriesIcon;
        @Bind(R.id.create_page_categories_text)
        TextView mCreatePageCategoriesText;

        public CategoryViewBinder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

        @Override
        public void bind(@Nonnull CreatePageCategoryPresenter.CategoryItem categoryItem) {
            picasso.load(categoryItem.getImage())
                    .fit()
                    .into(mCreatePageCategoriesIcon);

            mCreatePageCategoriesText.setText(categoryItem.getName());
            mView.setOnClickListener(v -> categoryItem.click());
        }
    }

    private UniversalAdapter mAdapter;
    private boolean isFromRegistration;

    public static Intent newIntent(Context context, boolean isFromRegistration) {
        return new Intent(context, CreatePageCategoryActivity.class)
                .putExtra(KEY_IS_FROM_REGISTRATION, isFromRegistration);
    }

    @Bind(R.id.create_page_category_toolbar)
    Toolbar mCreatePageCategoryToolbar;
    @Bind(R.id.create_page_category_list)
    RecyclerView mCreatePageCategoryList;

    @Inject
    Picasso picasso;
    @Inject
    CreatePageCategoryPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_page_category_activity);
        ButterKnife.bind(this);

        isFromRegistration = getIntent().getBooleanExtra(KEY_IS_FROM_REGISTRATION, false);

        ToolbarUtils.setupToolbar(mCreatePageCategoryToolbar, R.string.create_page_category_title, this);

        mAdapter = new UniversalAdapter(ImmutableList.of(
                new BaseViewHolderManager<>(R.layout.create_page_categories_header, EmptyViewHolder::new, CreatePageCategoryPresenter.HeaderItem.class),
                new BaseViewHolderManager<>(R.layout.create_page_categories_item, CategoryViewBinder::new, CreatePageCategoryPresenter.CategoryItem.class)));

        mCreatePageCategoryList.setLayoutManager(getGridLayoutManager());
        mCreatePageCategoryList.setAdapter(mAdapter);

        mPresenter.register(this);
    }

    @NonNull
    private GridLayoutManager getGridLayoutManager() {
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                final int viewType = mAdapter.getItemViewType(position);
                if (viewType == 0) {
                    return 2;
                } else {
                    return 1;
                }
            }
        });
        return layoutManager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unregister();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final CreatePageCategoryActivityComponent build = DaggerCreatePageCategoryActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        build.inject(this);
        return build;
    }

    @Override
    public void setData(List<BaseAdapterItem> items) {
        mAdapter.call(items);
    }

    @Override
    public void showProgress(boolean show) {

    }

    @Override
    public void error() {

    }

    @Override
    public void startDetailsActivity(String categoryId) {
        startActivity(CreatePageDetailsActivity.newIntent(this, categoryId, isFromRegistration));
    }
}
