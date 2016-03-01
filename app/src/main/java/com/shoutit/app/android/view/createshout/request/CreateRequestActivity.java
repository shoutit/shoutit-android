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
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.createshout.location.LocationActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateRequestActivity extends BaseActivity implements CreateRequestPresenter.Listener {

    private static final int LOCATION_REQUEST = 0;

    private SpinnerAdapter mAdapter;

    public static Intent newIntent(Context activity) {
        return new Intent(activity, CreateRequestActivity.class);
    }

    private class SpinnerAdapter extends BaseAdapter {

        private List<Pair<String, String>> list = ImmutableList.of(new Pair<>("", CreateRequestActivity.this.getString(R.string.request_activity_currency)));

        public SpinnerAdapter() {
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView view = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            view.setText(list.get(position).second);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            final TextView view = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            view.setText(list.get(position).second);
            return view;
        }

        public void setData(@NonNull List<Pair<String, String>> list) {
            this.list = list;
            notifyDataSetChanged();
        }
    }

    @Bind(R.id.create_request_descirption)
    EditText mCreateRequestDescirption;
    @Bind(R.id.create_request_budget)
    EditText mCreateRequestBudget;
    @Bind(R.id.create_request_spinner)
    Spinner mCreateRequestSpinner;
    @Bind(R.id.create_request_time)
    SeekBar mCreateRequestTime;
    @Bind(R.id.create_request_location)
    TextView mCreateRequestLocation;
    @Bind(R.id.create_request_progress)
    FrameLayout mCreateRequestProgress;
    @Bind(R.id.request_activity_toolbar)
    Toolbar mRequestActivityToolbar;

    @Inject
    CreateRequestPresenter mCreateRequestPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_activity);
        ButterKnife.bind(this);

        mAdapter = new SpinnerAdapter();
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

    @Override
    public CreateRequestPresenter.RequestData getRequestData() {
        return new CreateRequestPresenter.RequestData(
                mCreateRequestDescirption.getText().toString(),
                mCreateRequestBudget.getText().toString(),
                ((Pair<String, String>) mCreateRequestSpinner.getSelectedItem()).first);
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
    public void setCurrencies(@NonNull List<Pair<String, String>> list) {
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
    public void showTitleTooShortError() {
        mCreateRequestDescirption.setError(getString(R.string.create_request_activity_title_too_short));
    }

    @Override
    public void showEmptyPriceError() {
        mCreateRequestBudget.setError(getString(R.string.create_request_activity_price_empty));
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
