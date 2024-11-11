package com.example.myarea;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "PointsOfInterestDB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "POIs";
    private static final String ID_COL = "id";
    private static final String NAME_COL = "name";
    private static final String DESCRIPTION_COL = "description";
    private static final String LONG_COL = "long";
    private static final String LAT_COL = "lat";


    // below variable is for our course tracks column.
    private static final String TRACKS_COL = "tracks";

    public DBHandler(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
