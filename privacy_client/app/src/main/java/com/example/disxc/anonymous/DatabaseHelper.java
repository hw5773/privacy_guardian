package com.example.disxc.anonymous;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by disxc on 2017-01-11.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AnalyzeLog.db";

    public static class LogEntry implements BaseColumns{
        public static final String TABLE_NAME = "log";
        public static final String COLUMN_DATETIME = "time";
        public static final String COLUMN_PACKAGE_NAME = "app";
        public static final String COLUMN_HOST_ADDRESS = "address";
        public static final String COLUMN_DATA_TYPE = "datatype";
        public static final String COLUMN_DATA_VALUE = "dataval";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LogEntry.TABLE_NAME + " (" +
                    LogEntry._ID + " INTEGER PRIMARY KEY," +
                    LogEntry.COLUMN_DATETIME + TEXT_TYPE + COMMA_SEP +
                    LogEntry.COLUMN_PACKAGE_NAME + TEXT_TYPE + COMMA_SEP +
                    LogEntry.COLUMN_HOST_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    LogEntry.COLUMN_DATA_TYPE + TEXT_TYPE + COMMA_SEP +
                    LogEntry.COLUMN_DATA_VALUE + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LogEntry.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void clearDB(){
        onUpgrade(getWritableDatabase(), DATABASE_VERSION, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
