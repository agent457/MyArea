package com.example.myarea;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int SELECT_MAP_FILE = 1001;
    SharedPreferences sharedPreferences;
    EditTextPreference apiKeyPreference;
    Preference mapFilePreference;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        init();
        mapFilePreference.setOnPreferenceClickListener(preference -> {
            pickMapFile();
            return true;
        });
    }

    private void pickMapFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,SELECT_MAP_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_MAP_FILE && resultCode == Activity.RESULT_OK && data != null){
            Uri uri = data.getData();

            if (uri != null) {
                String fileName = getFileNameFromUri(uri);
                mapFilePreference.setSummary(fileName);

                sharedPreferences.edit().putString("selected_map_file",fileName).apply();
                saveFileFromUri(uri, fileName);


            }
        }

    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;

        // Check if the URI scheme is content
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Get the display name from the cursor
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // If the scheme is file, extract the name directly
            fileName = new File(uri.getPath()).getName();
        }

        return fileName;
    }

    public void saveFileFromUri(Uri uri, String fileName) {
        try {
            // Get InputStream from URI
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);

            // Define the destination file
            File destinationFile = new File(requireContext().getFilesDir(), fileName);

            // Write to the destination file
            OutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close streams
            inputStream.close();
            outputStream.close();

            Log.d("SaveFile", "File saved successfully at " + destinationFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SaveFile", "Error saving file: " + e.getMessage());
        }
    }

    private void init() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        apiKeyPreference = findPreference("GH_APIKEY");
        mapFilePreference = findPreference("Chosen_Map");
        assert mapFilePreference != null;
        mapFilePreference.setSummary(sharedPreferences.getString("selected_map_file","No Map File Selected"));
    }

}