package com.shoutit.app.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.appunite.rx.operators.MoreOperators;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.utils.LogHelper;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class RecentSearchesTable {
    private static final String TAG = RecentSearchesTable.class.getSimpleName();

    public static final String TABLE = "recent_searches";
    public static final String COLUMN_GUID = "_id";
    public static final String COLUMN_QUERY = "query";

    @NonNull
    private final DbHelper dbHelper;
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    @Inject
    public RecentSearchesTable(@NonNull DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @NonNull
    public static String getCreateStatemant() {
        return new DbTableSqlBuilder().createTable(TABLE)
                .addColumn(COLUMN_GUID, DbTableSqlBuilder.Type.INTEGER)
                .setNotNull(true)
                .isPrimaryKey(true)
                .isAutoincrement(true)
                .buildColumn()
                .addColumn(COLUMN_QUERY, DbTableSqlBuilder.Type.TEXT)
                .setNotNull(true)
                .buildColumn()
                .setUniqueColumn(COLUMN_QUERY)
                .setOnConflictIgnore(true)
                .build();
    }

    @NonNull
    private List<String> getAllRecentSearches() {
        Cursor cursor = null;

        try {
            final SQLiteDatabase db = dbHelper.getDatabase();
            cursor = db.query(
                    TABLE, null, null, null,
                    null, null, COLUMN_QUERY + " ASC");

            final ImmutableList.Builder<String> builder = ImmutableList.builder();
            while (cursor.moveToNext()) {
                builder.add(cursor.getString(cursor.getColumnIndex(COLUMN_QUERY)));
            }

            return builder.build();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void saveRecentSearch(@NonNull String suggestion) {
        final SQLiteDatabase db = dbHelper.getDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUERY, suggestion.trim());

        long itemId = db.insertWithOnConflict(TABLE, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (itemId != -1) {
            refreshRecentSearches();
        }
    }

    public void removeRecentSearch(@NonNull String suggestion) {
        final SQLiteDatabase db = dbHelper.getDatabase();

        final int deletedCount = db.delete(TABLE, COLUMN_QUERY + "=?", new String[]{suggestion});
        if (deletedCount > 0) {
            refreshRecentSearches();
        }
    }

    public void clearRecentSearch() {
        final SQLiteDatabase db = dbHelper.getDatabase();

        final int deletedCount = db.delete(TABLE, "1", null);
        if (deletedCount > 0) {
            refreshRecentSearches();
        }
    }

    public void refreshRecentSearches() {
        refreshSubject.onNext(null);
    }

    public Observable<List<String>> getAllRecentSearchesObservable() {
        return Observable
                .fromCallable(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        return getAllRecentSearches();
                    }
                })
                .compose(MoreOperators.<List<String>>refresh(refreshSubject));
    }
}
