package com.shoutit.app.android.view.createshout.edit;

import android.annotation.SuppressLint;
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
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.shoutit.app.android.view.createshout.request.CreateRequestPresenter;
import com.shoutit.app.android.view.createshout.request.DaggerCreateRequestComponent;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditShoutActivity extends BaseActivity implements CreateRequestPresenter.Listener {

    private static final int LOCATION_REQUEST = 0;
    @Bind(R.id.edit_toolbar)
    Toolbar mEditToolbar;
    @Bind(R.id.edit_descirption)
    EditText mEditDescirption;
    @Bind(R.id.edit_description_layout)
    TextInputLayout mEditLayout;
    @Bind(R.id.edit_budget)
    EditText mEditBudget;
    @Bind(R.id.edit_budget_layout)
    TextInputLayout mEditBudgetLayout;
    @Bind(R.id.edit_spinner)
    Spinner mEditSpinner;
    @Bind(R.id.edit_request)
    ImageView mEditRequest;
    @Bind(R.id.edit_shout_container)
    LinearLayout mEditShoutContainer;
    @Bind(R.id.edit_location)
    TextView mEditLocation;
    @Bind(R.id.edit_progress)
    FrameLayout mEditProgress;

    private SpinnerAdapter mAdapter;

    public static Intent newIntent(Context activity) {
        return new Intent(activity, EditShoutActivity.class);
    }

    private class SpinnerAdapter extends BaseAdapter {

        private List<Pair<String, String>> list = ImmutableList.of(new Pair<>("", EditShoutActivity.this.getString(R.string.request_activity_currency)));

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

        @SuppressLint("ViewHolder")
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


    @Inject
    CreateRequestPresenter mCreateRequestPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shout_activity);
        ButterKnife.bind(this);

        mAdapter = new SpinnerAdapter();
        mEditSpinner.setAdapter(mAdapter);

        mEditToolbar.setTitle(getString(R.string.request_activity_title));
        mEditToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mEditToolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
        final EditShoutComponent component = DaggerEditShoutComponent.builder()
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
                mEditDescirption.getText().toString(),
                mEditBudget.getText().toString(),
                ((Pair<String, String>) mEditSpinner.getSelectedItem()).first);
    }

    @Override
    public void setLocation(@DrawableRes int flag, @NonNull String name) {
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), flag);
        final RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        drawable.setCircular(true);

        mEditLocation.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        mEditLocation.setText(name);
    }

    @Override
    public void showProgress() {
        mEditProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mEditProgress.setVisibility(View.GONE);
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
        mEditSpinner.setEnabled(enabled);
    }

    @Override
    public void setRetryCurrenciesListener() {
        mEditSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCreateRequestPresenter.retryCurrencies();
                return true;
            }
        });
    }

    @Override
    public void removeRetryCurrenciesListener() {
        mEditSpinner.setOnTouchListener(null);
    }

    @Override
    public void showTitleTooShortError(boolean show) {
        if (show) {
            mEditLayout.setErrorEnabled(true);
            mEditLayout.setError(getString(R.string.create_request_activity_title_too_short));
        } else {
            mEditLayout.setErrorEnabled(false);
        }
    }

    @Override
    public void showEmptyPriceError(boolean show) {
        if (show) {
            mEditBudgetLayout.setErrorEnabled(true);
            mEditBudgetLayout.setError(getString(R.string.create_request_activity_price_empty));
        } else {
            mEditBudgetLayout.setErrorEnabled(false);
        }
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
