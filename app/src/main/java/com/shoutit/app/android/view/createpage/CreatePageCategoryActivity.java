package com.shoutit.app.android.view.createpage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.UniversalAdapter;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.adapter.ClassViewHolderManager;
import com.shoutit.app.android.utils.adapter.EmptyClassViewHolderManager;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreatePageCategoryActivity extends BaseActivity implements CreatePageCategoryPresenter.Listener {

    public class CategoryViewBinder {

        @Bind(R.id.create_page_categories_icon)
        ImageView mCreatePageCategoriesIcon;
        @Bind(R.id.create_page_categories_text)
        TextView mCreatePageCategoriesText;

        public CategoryViewBinder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private UniversalAdapter mAdapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, CreatePageCategoryActivity.class);
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

        mAdapter = new UniversalAdapter(ImmutableList.of(
                new EmptyClassViewHolderManager<>(CreatePageCategoryPresenter.HeaderItem.class, R.layout.create_page_categories_header),
                new ClassViewHolderManager<CreatePageCategoryPresenter.CategoryItem>(CreatePageCategoryPresenter.CategoryItem.class, R.layout.create_page_categories_item) {

                    @Override
                    public ViewBinder createViewBinder(View view) {
                        return new ViewBinder() {

                            CategoryViewBinder binder = new CategoryViewBinder(view);

                            @Override
                            public void bind(CreatePageCategoryPresenter.CategoryItem categoryItem) {
                                picasso.load(categoryItem.getImage())
                                        .fit()
                                        .into(binder.mCreatePageCategoriesIcon);

                                binder.mCreatePageCategoriesText.setText(categoryItem.getName());
                                view.setOnClickListener(v -> categoryItem.click());
                            }
                        };
                    }
                }));

        mCreatePageCategoryList.setLayoutManager(new LinearLayoutManager(this));
        mCreatePageCategoryList.setAdapter(mAdapter);

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
}
