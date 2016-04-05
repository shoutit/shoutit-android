package com.shoutit.app.android.view.createshout.edit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Category;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.view.createshout.DialogsHelper;
import com.shoutit.app.android.view.createshout.ShoutMediaPresenter;
import com.shoutit.app.android.view.createshout.location.LocationActivity;
import com.shoutit.app.android.view.media.RecordMediaActivity;
import com.shoutit.app.android.widget.CurrencySpinnerAdapter;
import com.shoutit.app.android.widget.ErrorTextInputLayout;
import com.shoutit.app.android.widget.SimpleCurrencySpinnerAdapter;
import com.shoutit.app.android.widget.SimpleSpinnerAdapter;
import com.shoutit.app.android.widget.SpinnerAdapter;
import com.shoutit.app.android.widget.StateSpinner;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditShoutActivity extends BaseActivity implements EditShoutPresenter.Listener, ShoutMediaPresenter.MediaListener {

    private static final int LOCATION_REQUEST = 0;
    private static final int MEDIA_REQUEST_CODE = 1;

    private static final String ARGS_ID = "args_id";

    @Bind(R.id.edit_toolbar)
    Toolbar mEditToolbar;
    @Bind(R.id.edit_shout_title)
    EditText mTitle;
    @Bind(R.id.edit_description_layout)
    ErrorTextInputLayout mEditLayout;
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
    @Bind(R.id.edit_shout_description)
    EditText mEditShoutDescription;
    @Bind(R.id.edit_currency_info)
    ImageView mEditCurrencyInfo;
    @Bind(R.id.edit_media_container)
    LinearLayout mEditMediaContainer;

    @Inject
    EditShoutPresenter mEditShoutPresenter;
    @Inject
    ShoutMediaPresenter mShoutMediaPresenter;
    @Inject
    Picasso mPicasso;

    private CurrencySpinnerAdapter mCurrencyAdapter;
    private SpinnerAdapter mCategoryAdapter;

    public static Intent newIntent(@NonNull String id, @NonNull Context context) {
        return new Intent(context, EditShoutActivity.class)
                .putExtra(ARGS_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shout_activity);
        ButterKnife.bind(this);

        mEditBudget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mEditShoutPresenter.onBudgetChanged(s.toString());
            }
        });

        mCurrencyAdapter = new SimpleCurrencySpinnerAdapter(R.string.request_activity_currency, this);
        mEditCurrencySpinner.setAdapter(mCurrencyAdapter);

        mCategoryAdapter = new SimpleSpinnerAdapter(R.string.edit_shout_category, this);
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

        mEditToolbar.setTitle(getString(R.string.edit_shout_title));
        mEditToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mEditToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mEditShoutPresenter.registerListener(this);
        mShoutMediaPresenter.register(this);

        mEditCurrencyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogsHelper.showCurrencyDialog(EditShoutActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mEditShoutPresenter.unregister();
        mShoutMediaPresenter.unregister();
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
        mShoutMediaPresenter.send();
    }

    @OnClick(R.id.edit_location_btn)
    public void onLocationClick() {
        startActivityForResult(LocationActivity.newIntent(this), LOCATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST && resultCode == RESULT_OK) {
            final UserLocation userLocation = (UserLocation) data.getSerializableExtra(LocationActivity.EXTRAS_USER_LOCATION);
            mEditShoutPresenter.updateLocation(userLocation);
        } else if (requestCode == MEDIA_REQUEST_CODE && resultCode == RESULT_OK) {
            final Bundle extras = data.getExtras();
            final boolean isVideo = extras.getBoolean(RecordMediaActivity.EXTRA_IS_VIDEO);
            final String media = extras.getString(RecordMediaActivity.EXTRA_MEDIA);
            Preconditions.checkNotNull(media);
            mShoutMediaPresenter.addMediaItem(media, isVideo);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
    public void setCurrencies(@NonNull List<PriceUtils.SpinnerCurrency> list) {
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
        mEditShoutDescription.setText(description);
    }

    @Override
    public void setCategories(@NonNull List<Pair<String, String>> list) {
        mCategoryAdapter.setData(list);
    }

    @Override
    public void setOptions(@NonNull List<CategoryFilter> options) {
        final boolean setStartSelection = mEditShoutContainer.getChildCount() == 0;
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

            final SpinnerAdapter adapter = new SimpleSpinnerAdapter(R.string.edit_shout_option, this);
            spinner.setAdapter(adapter);

            adapter.setData(ImmutableList.<Pair<String, String>>builder()
                    .add(new Pair<>("", getString(R.string.option_not_set)))
                    .addAll(optionsPairs)
                    .build());
            if (setStartSelection && categoryFilter.getSelectedValue() != null) {
                spinner.setSelection(adapter.getPosition(categoryFilter.getSelectedValue().getSlug()));
            }

            title.setText(name);
            view.setTag(categoryFilter.getSlug());

            mEditShoutContainer.addView(view, mEditShoutContainer.getChildCount());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Pair<String, String>> getSelectedOptions() {
        final int childCount = mEditShoutContainer.getChildCount();
        final List<Pair<String, String>> list = Lists.newArrayList();
        for (int i = 0; i < childCount; i++) {
            final View view = mEditShoutContainer.getChildAt(i);
            final StateSpinner spinner = (StateSpinner) view.findViewById(R.id.option_spinner);

            final Pair<String, String> selectedItem = (Pair<String, String>) spinner.getSelectedItem();
            if (selectedItem != null && !Strings.isNullOrEmpty(selectedItem.first)) {
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
    public void setCategory(@Nullable Category category) {
        if (category != null) {
            mPicasso.load(category.getIcon())
                    .fit()
                    .into(mEditCategoryIcon);

            mEditCategorySpinner.setSelection(mCategoryAdapter.getPosition(category.getSlug()));
        }
    }

    @Override
    public void setActionbarTitle(@NonNull String title) {
        mEditToolbar.setTitle(title);
    }

    @Override
    public void setMedia(@NonNull List<String> images, @NonNull List<Video> videos) {
        mShoutMediaPresenter.addRemoteMedia(images, videos);
    }

    @Override
    public void setImages(@NonNull Map<Integer, ShoutMediaPresenter.Item> mediaElements) {
        mEditMediaContainer.removeAllViews();

        for (int i = 0; i < mediaElements.size(); i++) {
            final ShoutMediaPresenter.Item item = mediaElements.get(i);
            final LayoutInflater layoutInflater = getLayoutInflater();
            final View view;

            if (item instanceof ShoutMediaPresenter.AddImageItem) {
                view = layoutInflater.inflate(R.layout.edit_media_add, mEditMediaContainer, false);
            } else if (item instanceof ShoutMediaPresenter.MediaItem) {
                view = layoutInflater.inflate(R.layout.edit_media_item, mEditMediaContainer, false);
                final ImageView imageView = (ImageView) view.findViewById(R.id.edit_media_item_image);
                mPicasso.load(Uri.parse(((ShoutMediaPresenter.MediaItem) item).getThumb()))
                        .centerCrop()
                        .fit()
                        .into(imageView);
            } else if (item instanceof ShoutMediaPresenter.BlankItem) {
                view = layoutInflater.inflate(R.layout.edit_media_blank, mEditMediaContainer, false);
            } else {
                throw new RuntimeException();
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.click();
                }
            });
            mEditMediaContainer.addView(view);
        }
    }

    @Override
    public void openSelectMediaActivity() {
        startActivityForResult(RecordMediaActivity.newIntent(this, true), MEDIA_REQUEST_CODE);
    }

    @Override
    public void onlyOneVideoAllowedAlert() {
        DialogsHelper.showOnlyOneVideoDialog(this);
    }

    @Override
    public void thumbnailCreateError() {
        ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.edit_thumbnail_error, Snackbar.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mediaEditionCompleted(@NonNull List<String> images, @NonNull List<Video> videos) {
        final EditShoutPresenter.RequestData requestData = new EditShoutPresenter.RequestData(
                mTitle.getText().toString(),
                mEditShoutDescription.getText().toString(),
                mEditBudget.getText().toString(),
                ((PriceUtils.SpinnerCurrency) mEditCurrencySpinner.getSelectedItem()).getCode(),
                ((Pair<String, String>) mEditCategorySpinner.getSelectedItem()).first,
                getSelectedOptions(), images, videos);
        mEditShoutPresenter.dataReady(requestData);
    }

    @Override
    public void showMediaProgress() {
        mEditProgress.setVisibility(View.VISIBLE);
    }
}
