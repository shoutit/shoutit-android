package com.shoutit.app.android.view.about;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.shoutit.app.android.R;
import com.shoutit.app.android.data.AssetsConstants;
import com.shoutit.app.android.view.webview.HtmlAssetViewerActivity;

import javax.annotation.Nonnull;

public class AboutFragment extends PreferenceFragment {

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_about);

        final PreferenceScreen termsPref = (PreferenceScreen) findPreference(getString(R.string.pref_key_terms_of_service));
        termsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startWebPage(AssetsConstants.ASSET_TERMS_OF_SERVICE,
                        getString(R.string.terms_of_service));
                return true;
            }
        });

        final PreferenceScreen privacyPref = (PreferenceScreen) findPreference(getString(R.string.pref_key_privacy_policy));
        privacyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startWebPage(AssetsConstants.ASSET_PRIVACY_POLICY,
                        getString(R.string.about_privacy_policy_title));
                return true;
            }
        });

        final PreferenceScreen legalPref = (PreferenceScreen) findPreference(getString(R.string.pref_key_legal));
        legalPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startWebPage(AssetsConstants.ASSET_LEGAL,
                        getString(R.string.about_legal_title));
                return true;
            }
        });
    }

    private void startWebPage(@Nonnull String assetName, @Nonnull String toolbarTitle) {
        startActivity(HtmlAssetViewerActivity.newIntent(getActivity(), assetName, toolbarTitle));
    }

}