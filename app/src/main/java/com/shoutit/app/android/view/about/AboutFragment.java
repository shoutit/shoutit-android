package com.shoutit.app.android.view.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.R;
import com.shoutit.app.android.data.AssetsConstants;
import com.shoutit.app.android.view.webview.HtmlAssetViewerActivity;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutFragment extends Fragment {

    @Nonnull
    public static Fragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.about_terms)
    public void onTermsClicked() {
        openHtmlActivity(AssetsConstants.ASSET_TERMS_OF_SERVICE, R.string.about_terms_of_service_title);
    }

    @OnClick(R.id.about_privacy)
    public void onPrivacyClicked() {
        openHtmlActivity(AssetsConstants.ASSET_PRIVACY_POLICY, R.string.about_privacy_policy_title);
    }

    @OnClick(R.id.about_legal)
    public void onLegalClicked() {
        openHtmlActivity(AssetsConstants.ASSET_LEGAL, R.string.about_legal_title);
    }

    private void openHtmlActivity(@Nonnull String assetName, @StringRes int toolbarTitleResId) {
        startActivity(HtmlAssetViewerActivity.newIntent(getActivity(), assetName, getString(toolbarTitleResId)));
    }
}