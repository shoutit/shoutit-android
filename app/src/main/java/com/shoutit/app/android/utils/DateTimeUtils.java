package com.shoutit.app.android.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;

public class DateTimeUtils {

    private static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("dd - MM - yyyy", Locale.getDefault());
    private static SimpleDateFormat sChatCreatedAtFormat = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
    private static SimpleDateFormat sSlashedDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nonnull
    public static String timeAgoFromSecondsToWeek(Context context, long datePublishedInMillis) {
        return android.text.format.DateUtils.getRelativeDateTimeString(context, datePublishedInMillis,
                android.text.format.DateUtils.SECOND_IN_MILLIS, android.text.format.DateUtils.WEEK_IN_MILLIS, 0)
                .toString();
    }

    @Nonnull
    public static String timeAgoFromDate(long dateInMillis) {
        return android.text.format.DateUtils.getRelativeTimeSpanString(
                dateInMillis, System.currentTimeMillis(), android.text.format.DateUtils.SECOND_IN_MILLIS)
                .toString();
    }

    public static String getShoutDetailDate(long datePublishedInMillis) {
        return sSimpleDateFormat.format(new Date(datePublishedInMillis));
    }

    public static String getChatCreatedAtDate(long millis) {
        return sChatCreatedAtFormat.format(new Date(millis));
    }

    public static String getSlashedDate(long dateInMillis) {
        return sSlashedDateFormat.format(new Date(dateInMillis));
    }
}
