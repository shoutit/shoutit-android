package com.shoutit.app.android.view.webview;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.utils.UpNavigationHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebViewActivity extends AppCompatActivity {

    private static final String SHOUTIT_DOMAIN = "shoutit.com";

    @Bind(R.id.activity_webview_toolbar)
    Toolbar toolbar;
    @Bind(R.id.activity_webview_webview)
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        final Uri uri = checkNotNull(getIntent().getData());
        final String pageUrl = checkNotNull(uri.getQueryParameter("page"));
        final String title = uri.getQueryParameter("title");

        setUpToolbar(title);
        loadWebPage(pageUrl);
    }

    private void loadWebPage(@NonNull String pageUrl) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final boolean isNetworkUrl = URLUtil.isNetworkUrl(url);
                if (isNetworkUrl && url.contains(SHOUTIT_DOMAIN)) {
                    return false;
                } else if (isNetworkUrl) {
                    startActivity(IntentHelper.websiteIntent(url));
                    return true;
                } else {
                    startActivity(IntentHelper.appLinkIntent(url));
                    return true;
                }
            }
        });
        webView.loadUrl(pageUrl);
    }

    private void setUpToolbar(String title) {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new UpNavigationHelper(this)
                        .onUpButtonClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
