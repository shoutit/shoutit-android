package com.shoutit.app.android.view.about;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.StringRes;

import com.shoutit.app.android.R;
import com.shoutit.app.android.data.AssetsConstants;
import com.shoutit.app.android.view.webview.HtmlAssetViewerActivity;

import javax.annotation.Nonnull;

public class AboutFragment extends PreferenceFragment {

    @Nonnull
    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_about);

        setOnPreferenceClickListener(R.string.pref_key_terms_of_service,
                AssetsConstants.ASSET_TERMS_OF_SERVICE,
                R.string.terms_of_service);

        setOnPreferenceClickListener(R.string.pref_key_privacy_policy,
                AssetsConstants.ASSET_PRIVACY_POLICY,
                R.string.about_privacy_policy_title);

        setOnPreferenceClickListener(R.string.pref_key_legal,
                AssetsConstants.ASSET_LEGAL,
                R.string.about_legal_title);
    }

    private void setOnPreferenceClickListener(@StringRes int prefKey,
                                              @Nonnull final String assetName,
                                              @StringRes final int toolbarTitleId) {
        findPreference(getString(prefKey)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(HtmlAssetViewerActivity.newIntent(getActivity(), assetName, getString(toolbarTitleId)));
                return true;
            }
        });
    }
}