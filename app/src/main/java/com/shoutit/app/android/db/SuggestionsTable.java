package com.shoutit.app.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.appunite.rx.operators.MoreOperators;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public class SuggestionsTable {

    public static final String TABLE = "suggstions";
    public static final String COLUMN_GUID = "_id";
    public static final String COLUMN_SUGGESTION = "suggestion";

    @NonNull
    private final DbHelper dbHelper;
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    @Inject
    public SuggestionsTable(@NonNull DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @NonNull
    public static String getCreateStatemant() {
        return new DbTableSqlBuilder().createTable(TABLE)
                .addColumn(COLUMN_GUID, DbTableSqlBuilder.Type.TEXT)
                .setNotNull(true)
                .isPrimaryKey(true)
                .buildColumn()
                .addColumn(COLUMN_SUGGESTION, DbTableSqlBuilder.Type.TEXT)
                .setNotNull(true)
                .buildColumn()
                .setUniqueColumn(COLUMN_SUGGESTION)
                .setOnConflictIgnore(true)
                .build();
    }

    @NonNull
    private List<String> getAllSuggestions() {
        Cursor cursor = null;

        try {
            final SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(
                    TABLE, null, null, null,
                    null, null, COLUMN_SUGGESTION + " ASC");

            final ImmutableList.Builder<String> builder = ImmutableList.builder();
            while (cursor.moveToNext()) {
                builder.add(cursor.getString(cursor.getColumnIndex(COLUMN_SUGGESTION)));
            }

            return builder.build();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void saveSuggestion(@NonNull String suggestion) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SUGGESTION, suggestion);

        long itemId = db.insert(TABLE, null, contentValues);
        if (itemId != -1) {
            refreshSuggestions();
        }
    }

    public void removeSuggestion(@NonNull String suggestion) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        final int deletedCount = db.delete(TABLE, COLUMN_SUGGESTION + "=?", new String[]{suggestion});
        if (deletedCount > 0) {
            refreshSuggestions();
        }
    }

    public void refreshSuggestions() {
        refreshSubject.onNext(null);
    }

    public Observable<List<String>> getAllSuggestionsObservable() {
        return Observable.just(getAllSuggestions())
                .compose(MoreOperators.<List<String>>refresh(refreshSubject));
    }
}
