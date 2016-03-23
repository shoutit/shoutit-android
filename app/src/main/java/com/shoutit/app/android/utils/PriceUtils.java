package com.shoutit.app.android.utils;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Currency;

import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nonnull;

public class PriceUtils {

    @Nonnull
    public static String formatPrice(@Nullable Long price, @Nonnull Resources resources) {
        if (price == null) {
            return "";
        } else if (price == 0){
            return resources.getString(R.string.price_free);
        } else {
            final float formattedPrice = (float) price / 100;
            return new DecimalFormat("#.##").format(formattedPrice);
        }
    }

    @Nonnull
    public static String formatPriceWithCurrency(@Nullable Long price,
                                                 @Nonnull Resources resources,
                                                 @Nullable String currency) {
        final String formattedPrice = formatPrice(price, resources);
        if (price == null || price == 0) {
            return formattedPrice;
        } else {
            return resources.getString(R.string.price_with_currency, formattedPrice, Strings.nullToEmpty(currency));
        }
    }

    public static long getPriceInCents(String price) {
        final double doublePrice = Double.parseDouble(price);
        final double centsPrice = doublePrice * 100;
        return (long) centsPrice;
    }

    public static List<Pair<String, String>> transformCurrencyToPair(List<Currency> currencies) {
        return ImmutableList.copyOf(
                Iterables.transform(currencies,
                        new Function<Currency, Pair<String, String>>() {
                            @javax.annotation.Nullable
                            @Override
                            public Pair<String, String> apply(Currency input) {
                                return Pair.create(input.getCode(),
                                        String.format("%s (%s)", input.getName(), input.getCountry()));
                            }
                        }));
    }
}
