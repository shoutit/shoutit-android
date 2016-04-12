package com.shoutit.app.android.utils;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;
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
        } else if (price == 0) {
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
        final Double doublePrice = Doubles.tryParse(price.replace(",", "."));

        double centsPrice = 0;
        if (doublePrice != null) {
            centsPrice = doublePrice * 100;
        }

        return (long) centsPrice;
    }

    public static List<SpinnerCurrency> transformCurrencyToPair(List<Currency> currencies) {
        return ImmutableList.copyOf(
                Iterables.transform(currencies,
                        new Function<Currency, SpinnerCurrency>() {
                            @javax.annotation.Nullable
                            @Override
                            public SpinnerCurrency apply(Currency input) {
                                return new SpinnerCurrency(
                                        input.getCode(),
                                        input.getCountry(),
                                        String.format("%s (%s)", input.getName(), input.getCountry()));
                            }
                        }));
    }

    public static class SpinnerCurrency {

        private final String code;
        private final String country;
        private final String name;

        public SpinnerCurrency(@NonNull String code, @NonNull String country, @NonNull String name) {
            this.code = code;
            this.country = country;
            this.name = name;
        }

        @NonNull
        public String getCode() {
            return code;
        }

        @NonNull
        public String getCountry() {
            return country;
        }

        @NonNull
        public String getName() {
            return name;
        }

    }
}
