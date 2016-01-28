package com.shoutit.app.android.view.webview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

import com.shoutit.app.android.R;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class HtmlAssetViewerActivity extends AppCompatActivity {

    private static final String ASSETS_FILE_PATH = "file:///android_asset/";
    private static final String EXTRA_HTML_ASSET_NAME = "asset_name";
    private static final String EXTRA_TITLE = "title";

    @Bind(R.id.activity_html_toolbar)
    Toolbar toolbar;
    @Bind(R.id.activity_html_webview)
    WebView webView;

    private String assetName;
    private String toolbarTitle;

    @Nonnull
    public static Intent newIntent(@Nonnull Context context,
                                   @Nonnull String assetName,
                                   @Nullable String toolbarName) {
        return new Intent(context, HtmlAssetViewerActivity.class)
                .putExtra(EXTRA_HTML_ASSET_NAME, assetName)
                .putExtra(EXTRA_TITLE, toolbarName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_asset_viewer);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        checkNotNull(intent);
        toolbarTitle = intent.getStringExtra(EXTRA_TITLE);
        assetName = intent.getStringExtra(EXTRA_HTML_ASSET_NAME);
        checkNotNull(assetName);

        loadWebPage();

        setUpToolbar();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(toolbarTitle);
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

    private void loadWebPage() {
        webView.getSettings().setJavaScriptEnabled(false);
        webView.loadUrl(ASSETS_FILE_PATH + assetName);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
    }
}
