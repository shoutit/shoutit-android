package com.shoutit.app.android.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.shoutit.app.android.R;
import com.shoutit.app.android.data.AssetsConstants;
import com.shoutit.app.android.view.webview.HtmlAssetViewerActivity;

public class RegisterUtils {

    public static void setUpSpans(@NonNull Context context, TextView bottomTextView) {
        final String bottomText = context.getString(R.string.register_bottom_text, context.getString(R.string.register_bottom_text_terms_of_service), context.getString(R.string.register_bottom_text_privacy_policy));
        final String textTermsOfService = context.getString(R.string.register_bottom_text_terms_of_service);
        final String textPrivacyPolicy = context.getString(R.string.register_bottom_text_privacy_policy);

        final SpannableString spannableString = SpanUtils.clickableColoredUnderlinedSpan(bottomText, textTermsOfService, ContextCompat.getColor(context, R.color.register_underline), () -> context.startActivity(HtmlAssetViewerActivity.newIntent(
                context, AssetsConstants.ASSET_TERMS_OF_SERVICE,
                context.getString(R.string.html_activity_terms))));

        final SpannableString finalSpannableString = SpanUtils.clickableColoredUnderlinedSpan(spannableString, textPrivacyPolicy, ContextCompat.getColor(context, R.color.register_underline), () -> context.startActivity(HtmlAssetViewerActivity.newIntent(
                context, AssetsConstants.ASSET_TERMS_OF_SERVICE,
                context.getString(R.string.html_activity_privacy))));

        bottomTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bottomTextView.setText(finalSpannableString);
    }

}
