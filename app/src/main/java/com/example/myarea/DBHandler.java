package com.example.myarea;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {
    private String DB_NAME;
    private static final int DB_VERSION = 1;
    // POIs Table
    private static final String POIs = "POIs";
    private static final String ID_COL = "id";
    private static final String NAME_COL = "name";
    private static final String DESCRIPTION_COL = "description";
    private static final String LONG_COL = "lon";
    private static final String LAT_COL = "lat";
    //


    public DBHandler(Context context, String NAME){
        super(context, NAME,null, DB_VERSION);
        this.DB_NAME=NAME;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + POIs + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + DESCRIPTION_COL + " TEXT,"
                + LONG_COL + " REAL,"
                + LAT_COL + " REAL)";
        db.execSQL(query);
    }
    public void addPOI(String name, String description, double lon, double lat){
        SQLiteDatabase db =  this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);
        values.put(DESCRIPTION_COL, description);
        values.put(LONG_COL, lon);
        values.put(LAT_COL, lat);
        db.insert(POIs, null, values);
        db.close();
    }
    public void deletePOI(int id){
        SQLiteDatabase db =  this.getWritableDatabase();
        String query = "DELETE FROM " + POIs + " WHERE " + ID_COL + " = " + id;
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + POIs);
        onCreate(db);
    }
}
