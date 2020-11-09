package com.smartlamp_v2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper  extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DataCollection";
    public static final String TABLE_DATA_SEN = "data_sen";

    public static final String KEY_ID = "_id";
    public static final String KEY_TEMP = "temperature";
    public static final String KEY_HUM = "humidity";
    public static final String KEY_BAR = "bar";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_DATA_SEN + "(" + KEY_ID
                + " integer primary key," + KEY_TEMP + " integer," + KEY_HUM
                + " integer," + KEY_BAR + " integer " + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_DATA_SEN);

        onCreate(db);

    }
}