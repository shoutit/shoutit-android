package com.shoutit.app.android.view.search.main;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.KeyboardHelper;
import com.shoutit.app.android.utils.RtlUtils;
import com.shoutit.app.android.utils.UpNavigationHelper;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.SearchQueryPresenter;
import com.shoutit.app.android.view.search.categories.SearchCategoriesFragment;
import com.shoutit.app.android.view.search.results.profiles.SearchProfilesResultsActivity;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;


public class MainSearchActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    @Bind(R.id.search_toolbar)
    Toolbar toolbar;
    @Bind(R.id.search_toolbar_shadow_view)
    View toolbarShadow;
    @Bind(R.id.search_view_pager)
    ViewPager viewPager;
    @Bind(R.id.search_tab_layout)
    TabLayout tabLayout;
    @Bind(R.id.search_view_pager_container)
    View pagerContainer;
    @Bind(R.id.search_categories_fragment_container)
    View categoriesFragmentContainer;

    @Inject
    MainSearchPagerAdapter pagerAdapter;
    @Inject
    SearchQueryPresenter searchQueryPresenter;

    private boolean wasViewRotated = false;
    private SearchView searchView;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainSearchActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setUpToolbar();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.search_categories_fragment_container, SearchCategoriesFragment.newInstance())
                    .commit();
        } else {
            wasViewRotated = true;
        }

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        searchQueryPresenter.getQuerySubmittedObservable()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String query) {
                        if (isShoutTabSelected()) {
                            startActivity(SearchShoutsResultsActivity.newIntent(
                                    MainSearchActivity.this, query, null, SearchPresenter.SearchType.SHOUTS));
                        } else {
                            startActivity(SearchProfilesResultsActivity.newIntent(
                                    MainSearchActivity.this, query));
                        }
                    }
                });

        searchQueryPresenter.getEmptyQuerySubmittedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(this), R.string.search_empty_query));

        searchQueryPresenter.getFillSearchWithSuggestionObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String suggestion) {
                        searchView.setQuery(suggestion, false);
                    }
                });
    }

    private boolean isShoutTabSelected() {
        return viewPager.getCurrentItem() == MainSearchPagerAdapter.SHOUTS_FRAGMENT_POSITION;
    }

    private void setUpToolbar() {
        toolbar.setNavigationIcon(RtlUtils.isRtlEnabled(this) ? R.drawable.ic_blue_arrow_rtl : R.drawable.ic_blue_arrow);
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new UpNavigationHelper(this).onUpButtonClicked();
                return true;
            case R.id.search:
                showPagerAdapter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setQueryHint(getString(R.string.search_base_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        if (!wasViewRotated) {
            searchView.clearFocus();
        }
        setSearchViewBackground(searchView);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showPagerAdapter();
            }
        });

        return true;
    }

    @Override
    public void finish() {
        KeyboardHelper.hideSoftKeyboard(this);
        super.finish();
    }

    @SuppressLint("PrivateResource")
    private void setSearchViewBackground(ViewGroup parent) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                setSearchViewBackground((ViewGroup) child);
            } else if (child instanceof EditText) {
                child.setBackground(getResources().getDrawable(R.drawable.abc_textfield_search_material));
                child.getBackground().mutate().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.SRC_OVER);
            }
        }
    }

    private void showPagerAdapter() {
        toolbarShadow.setVisibility(View.GONE);
        final Fragment fragment = getSupportFragmentManager().getFragments().get(0);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchQueryPresenter.getQuerySubmittedSubject().onNext(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchQueryPresenter.getQuerySubject().onNext(newText);
        return true;
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final MainSearchActivityComponent component = DaggerMainSearchActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
