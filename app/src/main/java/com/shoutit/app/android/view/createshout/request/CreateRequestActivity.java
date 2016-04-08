package com.shoutit.app.android.view.createshout.request;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.view.createshout.DialogsHelper;
import com.shoutit.app.android.view.createshout.location.LocationActivity;
import com.shoutit.app.android.view.createshout.publish.PublishShoutActivity;
import com.shoutit.app.android.widget.ErrorTextInputLayout;
import com.shoutit.app.android.widget.SimpleCurrencySpinnerAdapter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateRequestActivity extends BaseActivity implements CreateRequestPresenter.Listener {

    private static final int LOCATION_REQUEST = 0;

    private SimpleCurrencySpinnerAdapter mAdapter;

    public static Intent newIntent(Context activity) {
        return new Intent(activity, CreateRequestActivity.class);
    }

    @Bind(R.id.create_request_descirption)
    EditText mCreateRequestDescirption;
    @Bind(R.id.create_request_budget)
    EditText mCreateRequestBudget;
    @Bind(R.id.create_request_spinner)
    Spinner mCreateRequestSpinner;
    @Bind(R.id.create_request_location)
    TextView mCreateRequestLocation;
    @Bind(R.id.create_request_progress)
    FrameLayout mCreateRequestProgress;
    @Bind(R.id.request_activity_toolbar)
    Toolbar mRequestActivityToolbar;
    @Bind(R.id.request_activity_description_layout)
    ErrorTextInputLayout mRequestActivityDescriptionLayout;
    @Bind(R.id.create_request_currency_info)
    ImageView mCreateCurrencyInfo;

    @Inject
    CreateRequestPresenter mCreateRequestPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_activity);
        ButterKnife.bind(this);

        mCreateRequestBudget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCreateRequestPresenter.onBudgetChanged(s.toString());
            }
        });

        mAdapter = new SimpleCurrencySpinnerAdapter(R.string.request_activity_currency, this);
        mCreateRequestSpinner.setAdapter(mAdapter);

        mRequestActivityToolbar.setTitle(getString(R.string.request_activity_title));
        mRequestActivityToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mRequestActivityToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCreateRequestPresenter.registerListener(this);

        mCreateCurrencyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogsHelper.showCurrencyDialog(CreateRequestActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mCreateRequestPresenter.unregister();
        super.onDestroy();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final CreateRequestComponent component = DaggerCreateRequestComponent.builder()
                .appComponent(App.getAppComponent(getApplication()))
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        return component;
    }

    @OnClick(R.id.create_request_confirm)
    public void onClick() {
        mCreateRequestPresenter.confirmClicked();
    }

    @OnClick(R.id.create_request_location_btn)
    public void onLocationClick() {
        startActivityForResult(LocationActivity.newIntent(this), LOCATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST && resultCode == Activity.RESULT_OK) {
            final UserLocation userLocation = (UserLocation) data.getSerializableExtra(LocationActivity.EXTRAS_USER_LOCATION);
            mCreateRequestPresenter.updateLocation(userLocation);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CreateRequestPresenter.RequestData getRequestData() {
        return new CreateRequestPresenter.RequestData(
                mCreateRequestDescirption.getText().toString(),
                mCreateRequestBudget.getText().toString(),
                ((PriceUtils.SpinnerCurrency) mCreateRequestSpinner.getSelectedItem()).getCode());
    }

    @Override
    public void setLocation(@DrawableRes int flag, @NonNull String name) {
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), flag);
        final RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        drawable.setCircular(true);

        mCreateRequestLocation.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        mCreateRequestLocation.setText(name);
    }

    @Override
    public void showProgress() {
        mCreateRequestProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mCreateRequestProgress.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.request_acitvity_post_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setCurrencies(@NonNull List<PriceUtils.SpinnerCurrency> list) {
        mAdapter.setData(list);
    }

    @Override
    public void showCurrenciesError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.request_acitvity_currency_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setCurrenciesEnabled(boolean enabled) {
        mCreateRequestSpinner.setEnabled(enabled);
    }

    @Override
    public void setRetryCurrenciesListener() {
        mCreateRequestSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCreateRequestPresenter.retryCurrencies();
                return true;
            }
        });
    }

    @Override
    public void removeRetryCurrenciesListener() {
        mCreateRequestSpinner.setOnTouchListener(null);
    }

    @Override
    public void showTitleTooShortError(boolean show) {
        if (show) {
            mRequestActivityDescriptionLayout.setErrorEnabled(true);
            mRequestActivityDescriptionLayout.setError(getString(R.string.create_request_activity_title_too_short));
        } else {
            mRequestActivityDescriptionLayout.setErrorEnabled(false);
        }
    }

    @Override
    public void finishActivity(String id, String webUrl) {
        finish();
        startActivity(PublishShoutActivity.newIntent(this, id, webUrl, true));
    }
}
