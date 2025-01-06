package com.example.myarea;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.HalfFloat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.ContentHandler;
import java.util.HashMap;
import java.util.Map;

public class SearchSuggestionContentProvider extends ContentProvider {
    private UriMatcher uriMatcher;
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        DBHandler dbHandler = new DBHandler(requireContext(),"Yoana");
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        if (selectionArgs != null && selectionArgs.length>0 && selectionArgs[0].length()>0){
            builder.setTables(dbHandler.getDB_NAME());

            Map<String,String> projectionMap = new HashMap<>();
            projectionMap.put(dbHandler.getDB_NAME(),dbHandler.getDB_NAME() + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
            String cords = dbHandler.getLONG_COL() + dbHandler.getLAT_COL();
            projectionMap.put(cords,cords + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
            projectionMap.put(dbHandler.getID_COL(),dbHandler.getID_COL() + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
            builder.setProjectionMap(projectionMap);
            Cursor cursor = builder.query(db,projection,selection,selectionArgs,null,null,sortOrder);
            if(cursor!=null){
                cursor.setNotificationUri(requireContext().getContentResolver(),uri);
            }
            return cursor;
        }
        else {;
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
