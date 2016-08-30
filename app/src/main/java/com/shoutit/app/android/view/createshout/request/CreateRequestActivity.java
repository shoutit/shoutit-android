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
import android.support.design.widget.TextInputLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.BackPressedHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.utils.RtlUtils;
import com.shoutit.app.android.utils.TextWatcherAdapter;
import com.shoutit.app.android.view.createshout.DialogsHelper;
import com.shoutit.app.android.view.location.LocationActivityForResult;
import com.shoutit.app.android.view.location.LocationHelper;
import com.shoutit.app.android.view.createshout.publish.PublishShoutActivity;
import com.shoutit.app.android.facebook.FacebookHelper;
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
    private static final String KEY_LOCATION = "key_location";

    private SimpleCurrencySpinnerAdapter mAdapter;
    private CallbackManager mCallbackManager;

    public static Intent newIntent(Context activity) {
        return new Intent(activity, CreateRequestActivity.class);
    }

    @Bind(R.id.create_request_descirption)
    EditText mCreateRequestDescirption;
    @Bind(R.id.create_request_budget)
    EditText mCreateRequestBudget;
    @Bind(R.id.create_request_currency_spinner)
    Spinner mCurrencySpinner;
    @Bind(R.id.create_request_location)
    TextView mCreateRequestLocation;
    @Bind(R.id.create_request_progress)
    FrameLayout mCreateRequestProgress;
    @Bind(R.id.request_activity_toolbar)
    Toolbar mRequestActivityToolbar;
    @Bind(R.id.request_activity_description_layout)
    TextInputLayout mRequestActivityDescriptionLayout;
    @Bind(R.id.create_request_currency_info)
    ImageView mCreateCurrencyInfo;
    @Bind(R.id.request_activity_facebook_checkbox)
    CheckBox facebookCheckbox;

    @Inject
    CreateRequestPresenter mCreateRequestPresenter;
    @Inject
    FacebookHelper facebookHelper;
    @Inject
    UserPreferences userPreferences;

    private UserLocation changedLocation;
    private BackPressedHelper mBackPressedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_activity);
        ButterKnife.bind(this);

        mCallbackManager = CallbackManager.Factory.create();

        RtlUtils.setTextDirection(this, mCreateRequestLocation);
        mCreateRequestBudget.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                mCreateRequestPresenter.onBudgetChanged(s.toString());
            }
        });

        //noinspection ConstantConditions
        facebookCheckbox.setChecked(facebookHelper.hasRequiredPermissionInApi(
                userPreferences.getUserOrPage(), new String[]{FacebookHelper.PERMISSION_PUBLISH_ACTIONS}));
        RtlUtils.setTextDirection(this, facebookCheckbox);

        showShareInfoDialogIfNeeded();

        mAdapter = new SimpleCurrencySpinnerAdapter(R.string.request_activity_currency, this);
        mCurrencySpinner.setAdapter(mAdapter);

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

        if (savedInstanceState != null) {
            changedLocation = (UserLocation) savedInstanceState.getSerializable(KEY_LOCATION);
            if (changedLocation != null) {
                mCreateRequestPresenter.updateLocation(changedLocation);
            }
        }

        mBackPressedHelper = new BackPressedHelper(this);

        facebookCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    return;
                }

                mCreateRequestPresenter.askForFacebookPermissionIfNeeded(
                        CreateRequestActivity.this, mCallbackManager);
            }
        });
    }

    private void showShareInfoDialogIfNeeded() {
        if (!userPreferences.wasShareDialogAlreadyDisplayed() && !facebookCheckbox.isChecked()) {
            DialogsHelper.showShareInfoDialog(this);
            userPreferences.setShareDialogDisplayed();
        }
    }

    @Override
    protected void onDestroy() {
        mCreateRequestPresenter.unregister();
        mBackPressedHelper.removeCallbacks();
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
        mCreateRequestPresenter.confirmClicked(facebookCheckbox.isChecked());
    }

    @OnClick(R.id.create_request_location_btn)
    public void onLocationClick() {
        startActivityForResult(LocationActivityForResult.newIntent(this), LOCATION_REQUEST);
    }

    @OnClick(R.id.create_request_credits_info_iv)
    public void onCreditsInfoClick() {
        DialogsHelper.showShareInfoDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST && resultCode == Activity.RESULT_OK) {
            final UserLocation userLocation = LocationHelper.getLocationFromIntent(data);
            mCreateRequestPresenter.updateLocation(userLocation);
            changedLocation = userLocation;
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CreateRequestPresenter.RequestData getRequestData() {
        String currencyCode = ((PriceUtils.SpinnerCurrency) mCurrencySpinner.getSelectedItem()).getCode();
        currencyCode = currencyCode.equals(mAdapter.getStartingText()) ? null : currencyCode;

        return new CreateRequestPresenter.RequestData(
                mCreateRequestDescirption.getText().toString(),
                mCreateRequestBudget.getText().toString(),
                currencyCode);
    }

    @Override
    public void setLocation(@DrawableRes int flag, @NonNull String name) {
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), flag);
        final RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        drawable.setCircular(true);

        ImageHelper.setStartCompoundRelativeDrawable(mCreateRequestLocation, drawable);
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
    public void showApiError(Throwable throwable) {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), throwable, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setCurrencies(@NonNull List<PriceUtils.SpinnerCurrency> list) {
        mAdapter.setData(list);
        mCurrencySpinner.setEnabled(!Strings.isNullOrEmpty(mCreateRequestBudget.getText().toString()));
    }

    @Override
    public void showCurrenciesError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.request_acitvity_currency_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showCurrenciesErrorPrompt() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.request_acitvity_currency_prompt_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setCurrenciesEnabled(boolean enabled) {
        mCurrencySpinner.setEnabled(enabled);
    }

    @Override
    public void setRetryCurrenciesListener() {
        mCurrencySpinner.setEnabled(!Strings.isNullOrEmpty(mCreateRequestBudget.getText().toString()));
        mCurrencySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCreateRequestPresenter.retryCurrencies();
                return true;
            }
        });
    }

    @Override
    public void removeRetryCurrenciesListener() {
        mCurrencySpinner.setOnTouchListener(null);
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
    public void finishActivity(String id, String webUrl, String title) {
        finish();
        startActivity(PublishShoutActivity.newIntent(this, id, webUrl, true, title));
    }

    @Override
    public void uncheckFacebookCheckbox() {
        facebookCheckbox.setChecked(false);
    }

    @Override
    public void showPermissionNotGranted() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this),
                R.string.request_activity_facebook_permission_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_LOCATION, changedLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (!mBackPressedHelper.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
