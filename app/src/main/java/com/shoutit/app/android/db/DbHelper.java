package com.shoutit.app.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.shoutit.app.android.dagger.ForApplication;

import javax.annotation.Nonnull;


public class DbHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;

    public static final String NAME = "shoutit_db";

    public DbHelper(@ForApplication Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        createDb(db);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        recreateDb(db);
    }

    private static void createDb(@Nonnull SQLiteDatabase db) {
        db.execSQL(SuggestionsTable.getCreateStatemant());
    }

    private static void recreateDb(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + SuggestionsTable.TABLE);
        createDb(db);
    }
}
