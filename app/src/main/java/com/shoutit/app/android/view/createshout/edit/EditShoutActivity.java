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
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.createshout.location.LocationActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditShoutActivity extends BaseActivity implements EditShoutPresenter.Listener {

    private static final int LOCATION_REQUEST = 0;

    private static final String ARGS_ID = "args_id";

    @Bind(R.id.edit_toolbar)
    Toolbar mEditToolbar;
    @Bind(R.id.edit_shout_title)
    EditText mTitle;
    @Bind(R.id.edit_description_layout)
    TextInputLayout mEditLayout;
    @Bind(R.id.edit_budget)
    EditText mEditBudget;
    @Bind(R.id.edit_currency_spinner)
    Spinner mEditCurrencySpinner;

    @Bind(R.id.edit_request_category_spinner)
    Spinner mEditCategorySpinner;

    @Bind(R.id.edit_request_category_icon)
    ImageView mEditCategoryIcon;
    @Bind(R.id.edit_shout_container)
    LinearLayout mEditShoutContainer;
    @Bind(R.id.edit_location)
    TextView mEditLocation;
    @Bind(R.id.edit_progress)
    FrameLayout mEditProgress;

    @Inject
    EditShoutPresenter mEditShoutPresenter;
    @Inject
    Picasso mPicasso;

    private SpinnerAdapter mCurrencyAdapter;
    private SpinnerAdapter mCategoryAdapter;

    public static Intent newIntent(@NonNull String id, @NonNull Context context) {
        return new Intent(context, EditShoutActivity.class)
                .putExtra(ARGS_ID, id);
    }

    private class SpinnerAdapter extends BaseAdapter {

        private List<Pair<String, String>> list;

        public SpinnerAdapter(@StringRes int startingText) {
            list = ImmutableList.of(new Pair<>("", EditShoutActivity.this.getString(startingText)));
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shout_activity);
        ButterKnife.bind(this);

        mCurrencyAdapter = new SpinnerAdapter(R.string.request_activity_currency);
        mEditCurrencySpinner.setAdapter(mCurrencyAdapter);

        mCategoryAdapter = new SpinnerAdapter(R.string.edit_shout_category);
        mEditCategorySpinner.setAdapter(mCategoryAdapter);
        mEditCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Pair<String, String> item = (Pair<String, String>) mEditCategorySpinner.getItemAtPosition(position);
                mEditShoutPresenter.categorySelected(item.first);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEditToolbar.setTitle(getString(R.string.request_activity_title));
        mEditToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mEditToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mEditShoutPresenter.registerListener(this);
    }

    @Override
    protected void onDestroy() {
        mEditShoutPresenter.unregister();
        super.onDestroy();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String id = getIntent().getStringExtra(ARGS_ID);

        final EditShoutComponent component = DaggerEditShoutComponent.builder()
                .appComponent(App.getAppComponent(getApplication()))
                .activityModule(new ActivityModule(this))
                .editShoutActivityModule(new EditShoutActivityModule(id))
                .build();
        component.inject(this);
        return component;
    }

    @OnClick(R.id.edit_confirm)
    public void onClick() {
        mEditShoutPresenter.confirmClicked();
    }

    @OnClick(R.id.edit_location_btn)
    public void onLocationClick() {
        startActivityForResult(LocationActivity.newIntent(this), LOCATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST && resultCode == Activity.RESULT_OK) {
            final UserLocation userLocation = (UserLocation) data.getSerializableExtra(LocationActivity.EXTRAS_USER_LOCATION);
            mEditShoutPresenter.updateLocation(userLocation);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public EditShoutPresenter.RequestData getRequestData() {
        return new EditShoutPresenter.RequestData(
                mTitle.getText().toString(),
                mEditBudget.getText().toString(),
                ((Pair<String, String>) mEditCurrencySpinner.getSelectedItem()).first, ((Pair<String, String>) mEditCategorySpinner.getSelectedItem()).first, getSelectedOptions());
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
    public void showPostError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.request_acitvity_post_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showCategoriesError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.edit_shout_categories_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showCurrenciesError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.edit_shout_currencies_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showBodyError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), getString(R.string.edit_shout_body_error), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setCurrencies(@NonNull List<Pair<String, String>> list) {
        mCurrencyAdapter.setData(list);
    }

    @Override
    public void setCurrenciesEnabled(boolean enabled) {
        mEditCurrencySpinner.setEnabled(enabled);
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
    public void finishActivity() {
        finish();
    }

    @Override
    public void setDescription(@Nullable String description) {

    }

    @Override
    public void setCategories(@NonNull List<Pair<String, String>> list) {
        mCategoryAdapter.setData(list);
    }

    @Override
    public void setOptions(@NonNull List<CategoryFilter> options) {
        mEditShoutContainer.removeAllViews();
        final LayoutInflater layoutInflater = getLayoutInflater();

        for (CategoryFilter categoryFilter : options) {
            final String name = categoryFilter.getName();
            final List<CategoryFilter.FilterValue> optionsList = categoryFilter.getValues();

            final View view = layoutInflater.inflate(R.layout.options_layout, mEditShoutContainer, false);
            final TextView title = (TextView) view.findViewById(R.id.option_title);
            final Spinner spinner = (Spinner) view.findViewById(R.id.option_spinner);

            final List<Pair<String, String>> optionsPairs = ImmutableList.copyOf(Iterables.transform(optionsList,
                    new Function<CategoryFilter.FilterValue, Pair<String, String>>() {
                        @Nullable
                        @Override
                        public Pair<String, String> apply(@Nullable CategoryFilter.FilterValue input) {
                            assert input != null;
                            return Pair.create(input.getSlug(), input.getName());
                        }
                    }));

            final SpinnerAdapter adapter = new SpinnerAdapter(R.string.edit_shout_option);
            spinner.setAdapter(adapter);
            adapter.setData(optionsPairs);

            title.setText(name);
            view.setTag(categoryFilter.getSlug());

            mEditShoutContainer.addView(view, mEditShoutContainer.getChildCount());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Pair<String, String>> getSelectedOptions() {
        final int childCount = mEditShoutContainer.getChildCount();
        List<Pair<String, String>> list = Lists.newArrayList();
        for (int i = 0; i < childCount; i++) {
            final View view = mEditShoutContainer.getChildAt(i);
            final Spinner spinner = (Spinner) view.findViewById(R.id.option_spinner);

            final Pair<String, String> selectedItem = (Pair<String, String>) spinner.getSelectedItem();
            if (selectedItem != null) {
                list.add(Pair.create((String) view.getTag(), selectedItem.first));
            }
        }
        return ImmutableList.copyOf(list);
    }

    @Override
    public void setSelectedCurrency(int currencyPostion) {
        mEditCurrencySpinner.setSelection(currencyPostion);
    }

    @Override
    public void setTitle(@NonNull String title) {
        mTitle.setText(title);
    }

    @Override
    public void setPrice(@NonNull String price) {
        mEditBudget.setText(price);
    }

    @Override
    public void setCategoryImage(@Nullable String image) {
        mPicasso.load(image)
                .fit()
                .into(mEditCategoryIcon);
    }
}
