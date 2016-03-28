package com.shoutit.app.android.view.search.subsearch;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.KeyboardHelper;
import com.shoutit.app.android.view.search.SearchAdapter;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.SearchQueryPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SubSearchActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    private static final String KEY_SEARCH_TYPE = "search_type";
    private static final String KEY_CONTEXTUAL_ITEM_ID = "contextual_item_id";
    private static final String KEY_CONTEXTUAL_ITEM_NAME = "contextual_item_name";

    @Bind(R.id.search_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.search_toolbar)
    Toolbar toolbar;

    @Inject
    SearchPresenter presenter;
    @Inject
    SearchQueryPresenter searchQueryPresenter;
    @Inject
    SearchAdapter adapter;

    private SearchView searchView;
    private boolean wasViewRotated = false;

    public static Intent newIntent(Context context, SearchPresenter.SearchType searchType,
                                   @Nonnull String contextualItemId,
                                   @Nonnull String contextualItemName) {
        return new Intent(context, SubSearchActivity.class)
                .putExtra(KEY_SEARCH_TYPE, searchType)
                .putExtra(KEY_CONTEXTUAL_ITEM_ID, contextualItemId)
                .putExtra(KEY_CONTEXTUAL_ITEM_NAME, contextualItemName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subsearch);
        ButterKnife.bind(this);

        setUpToolbar();

        if (savedInstanceState != null) {
            wasViewRotated = true;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        presenter.getSubSearchSubmittedObservable()
                .compose(this.<Intent>bindToLifecycle())
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        startActivity(intent);
                    }
                });

        presenter.getSuggestionsAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getSuggestionClickedObservable()
                .compose(this.<Intent>bindToLifecycle())
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        startActivity(intent);
                    }
                });

        searchQueryPresenter.getFillSearchWithSuggestionObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String suggestion) {
                        searchView.setQuery(suggestion, false);
                    }
                });
    }

    private void setUpToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_blue_arrow);
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
        searchView.setMaxWidth(Integer.MAX_VALUE);
        if (!wasViewRotated) {
            searchView.clearFocus();
        }
        setSearchViewBackground(searchView);

        presenter.getHintNameObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String hint) {
                        searchView.setQueryHint(hint);
                    }
                });

        return true;
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

    @Override
    public void finish() {
        KeyboardHelper.hideSoftKeyboard(this);
        super.finish();
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        final SearchPresenter.SearchType searchType = (SearchPresenter.SearchType) intent.getSerializableExtra(KEY_SEARCH_TYPE);
        final String contextualItemId = intent.getStringExtra(KEY_CONTEXTUAL_ITEM_ID);
        final String contextualItemName = intent.getStringExtra(KEY_CONTEXTUAL_ITEM_NAME);

        final SubSearchActivityComponent component = DaggerSubSearchActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .subSearchActivityModule(new SubSearchActivityModule(searchType, contextualItemId, contextualItemName))
                .build();
        component.inject(this);

        return component;
    }
}
