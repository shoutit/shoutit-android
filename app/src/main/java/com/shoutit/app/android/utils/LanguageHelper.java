package com.shoutit.app.android.utils;

import android.os.Build;

import java.util.Locale;

public class LanguageHelper {

    public static String getAcceptLanguage() {
        final Locale locale = Locale.getDefault();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return locale.toLanguageTag();
        }

        final char SEP = '-';
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        if (language.equals("no") && region.equals("NO") && variant.equals("NY")) {
            language = "nn";
            region = "NO";
            variant = "";
        }

        if (language.isEmpty() || !language.matches("\\p{Alpha}{2,8}")) {
            language = "und";
        } else if (language.equals("iw")) {
            language = "he";
        } else if (language.equals("in")) {
            language = "id";
        } else if (language.equals("ji")) {
            language = "yi";
        }
        if (!region.matches("\\p{Alpha}{2}|\\p{Digit}{3}")) {
            region = "";
        }
        if (!variant.matches("\\p{Alnum}{5,8}|\\p{Digit}\\p{Alnum}{3}")) {
            variant = "";
        }

        StringBuilder bcp47Tag = new StringBuilder(language);
        if (!region.isEmpty()) {
            bcp47Tag.append(SEP).append(region);
        }
        if (!variant.isEmpty()) {
            bcp47Tag.append(SEP).append(variant);
        }
        return bcp47Tag.toString();
    }
}
