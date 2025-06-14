package com.example.myarea;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        DBHandler dbHandler = new DBHandler(requireContext(), sharedPreferences.getString("chosenDB","new DB") );
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String query = selectionArgs != null && selectionArgs.length > 0 ? selectionArgs[0] : "";
        return db.query("POIs", // the table to search in
                new String[]{
                        "id AS _id",
                        "name AS suggest_text_1", // returns the name of the POI in the intent
                        "description AS suggest_text_2", // returns the description of the POI in the intent
                        "id AS suggest_intent_data"}, // returns the id of the POI with the intent
                "name LIKE ?", // the filter with which to search in the table
                new String[]{"%" + query + "%"},
                null, null, null);

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
