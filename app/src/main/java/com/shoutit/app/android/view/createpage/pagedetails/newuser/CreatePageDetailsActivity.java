package com.shoutit.app.android.view.createpage.pagedetails.newuser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.RegisterUtils;
import com.shoutit.app.android.utils.ToolbarUtils;
import com.shoutit.app.android.view.about.AboutActivity;
import com.shoutit.app.android.view.createpage.pagedetails.common.CategoryInfo;
import com.shoutit.app.android.view.createpage.pagedetails.common.SpinnerAdapter;
import com.shoutit.app.android.view.main.MainActivity;
import com.uservoice.uservoicesdk.UserVoice;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CreatePageDetailsActivity extends BaseActivity implements CreatePageDetailsPresenter.Listener {


    private static final String EXTRA_SELECTED_CATEGORY = "EXTRA_SELECTED_CATEGORY";
    private static final String EXTRA_IS_FROM_REGISTRATION = "EXTRA_IS_FROM_REGISTRATION";

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
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.create_page_details_bottom_text)
    TextView mCreatePageDetailsBottomText;
    @Bind(R.id.create_page_info_section)
    View infoSection;
    @Bind(R.id.login_bottom_actions_container)
    View bottomButtons;
    @Bind(R.id.create_page_details_button)
    Button createButton;

    public static Intent newIntent(Context context, String selectedCategory, boolean isFromRegistration) {
        return new Intent(context, CreatePageDetailsActivity.class)
                .putExtra(EXTRA_SELECTED_CATEGORY, selectedCategory)
                .putExtra(EXTRA_IS_FROM_REGISTRATION, isFromRegistration);
    }

    @Inject
    CreatePageDetailsPresenter mPresenter;

    private SpinnerAdapter mAdapter;
    private boolean isFromRegistration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_page_details_activity);
        ButterKnife.bind(this);

        isFromRegistration = getIntent().getBooleanExtra(EXTRA_IS_FROM_REGISTRATION, false);
        infoSection.setVisibility(isFromRegistration ? View.VISIBLE : View.GONE);
        mCreatePageDetailsBottomText.setVisibility(isFromRegistration ? View.VISIBLE : View.GONE);
        bottomButtons.setVisibility(isFromRegistration ? View.VISIBLE : View.GONE);

        mAdapter = new SpinnerAdapter(LayoutInflater.from(this));
        mCreatePageDetailsSpinner.setAdapter(mAdapter);
        ToolbarUtils.setupToolbar(mCreatePageCategoryToolbar, R.string.create_page_category_title, this);
        RegisterUtils.setUpSpans(this, mCreatePageDetailsBottomText);

        mPresenter.register(this);

        RxView.clicks(createButton)
                .throttleFirst(5, TimeUnit.SECONDS)
                .compose(bindToLifecycle())
                .subscribe(ignore -> {
                    mPresenter.passCreatePageData(new CreatePageDetailsPresenter.CreatePageData((CategoryInfo)
                                    mAdapter.getItem(mCreatePageDetailsSpinner.getSelectedItemPosition()),
                                    mCreatePageDetailsName.getText().toString(), mCreatePageDetailsFullName.getText().toString(),
                                    mCreatePageDetailsEmail.getText().toString(), mCreatePageDetailsPassword.getText().toString()),
                            isFromRegistration);
                });
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

    @OnCheckedChanged(R.id.create_page_details_lock_password)
    public void lock(boolean checked) {
        if (checked) {
            mCreatePageDetailsPassword.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            mCreatePageDetailsPassword.setTransformationMethod(null);

        }
        mCreatePageDetailsPassword.setSelection(mCreatePageDetailsPassword.length());
    }

    @Override
    public void setCategories(List<CategoryInfo> categoryInfos) {
        notifyAdapter(categoryInfos);
    }

    private void notifyAdapter(List<CategoryInfo> categoryInfos) {
        mAdapter.setData(categoryInfos);
    }

    @Override
    public void showProgress(boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void error() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.error_default, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void emptyEmail() {
        mCreatePageDetailsEmailLayout.setError(getString(R.string.login_error_empty_email));
    }

    @Override
    public void fullNameEmpty() {
        mCreatePageDetailsFullNameLayout.setError(getString(R.string.register_empty_name));
    }

    @Override
    public void passwordEmpty() {
        mCreatePageDetailsPasswordLayout.setError(getString(R.string.register_empty_password));
    }

    @Override
    public void startMainActivity() {
        ActivityCompat.finishAffinity(this);
        startActivity(MainActivity.newIntent(this));
    }

    @Override
    public void nameEmpty() {
        mCreatePageDetailsNameLayout.setError(getString(R.string.register_empty_name));
    }

    @Override
    public void setToolbarTitle(String title) {
        mCreatePageCategoryToolbar.setTitle(title);
    }

    @OnClick(R.id.activity_login_feedback)
    public void onFeedbackClick() {
        UserVoice.launchContactUs(this);
    }

    @OnClick(R.id.activity_login_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(this);
    }

    @OnClick(R.id.activity_login_about)
    public void onAboutClick() {
        startActivity(AboutActivity.newIntent(this));
    }


}