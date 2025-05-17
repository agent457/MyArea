package com.example.myarea;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class EditorFragment extends Fragment {

    private EditText editName, editDes, editLon, editLat, rename;
    private Button addPOI, current;
    private ImageButton newDB;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;
    private ListView listView;
    private DBHandler db;
    private ArrayList<POI> POIs;
    private PoiAdapter adapter;

    public EditorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_editor, container, false);
        init(view); // Initiate all Views and Variables

        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String item = parent.getItemAtPosition(position).toString(); // get the clicked item
            Toast.makeText(requireContext(),"Loading " + item + " POI database", Toast.LENGTH_SHORT).show();
            requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE)
                    .edit().putString("chosenDB", item).apply(); // Set the new chosenDB to be the one picked
            rename.setText(item); // Set the text in the rename editText view to the new database's name
            db = new DBHandler(requireContext(), item);
            POIs = db.DbToArrayList();
            adapter = new PoiAdapter(requireContext(), POIs);
            listView.setAdapter(adapter); // creates and attach the new PoiAdapter with the chosen database
        }); // load the new chosen database

        addPOI.setOnClickListener(v -> {
            if (editName.getText() == null || editDes.getText() == null || editLon.getText() == null || editLat.getText() == null) {
                Toast.makeText(requireContext(), "Please enter all the data", Toast.LENGTH_SHORT).show();
                return;
                // makes sure all fields are filled
            }
            db.addPOI(
                    String.valueOf(editName.getText()),
                    String.valueOf(editDes.getText()),
                    Double.parseDouble(String.valueOf(editLon.getText())),
                    Double.parseDouble(String.valueOf(editLat.getText())));
            POIs.add(db.getLast());
            adapter.notifyDataSetChanged();

            Toast.makeText(requireContext(), "Added POI", Toast.LENGTH_SHORT).show();
        }); // Adds a new POI to the database

        current.setOnClickListener(new View.OnClickListener() {
            @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
            @Override
            public void onClick(View v) {
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
                fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null).addOnSuccessListener(location -> {
                    editLat.setText(String.valueOf(location.getLatitude()));
                    editLon.setText(String.valueOf(location.getLongitude()));
                });
            }
        }); // Fills in the current coordinates in the Latitude and Longitude fields

        rename.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)){
                String newName = v.getText().toString();
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
                String chosenDB = sharedPreferences.getString("chosenDB", "New DB");
                HashSet<String> base = new HashSet<>();
                base.add("New DB");
                // base items set should have "New DB" in it
                Set<String> items = new HashSet<>(
                        sharedPreferences.getStringSet("databases", base)
                );

                items.remove(chosenDB);
                items.add(newName);
                // remove old database name and add the new one

                renameDatabase(db.getDB_NAME(),newName); // renames the database file to the new name
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("databases",items); // Sets the databases set to the new one
                editor.putString("chosenDB", newName); // Sets the chosenDB to the new name
                editor.apply();

                String[] items_array = items.toArray(new String[0]);
                adapterItems = new ArrayAdapter<>(requireContext(), R.layout.list_item, items_array);
                autoCompleteTextView.setAdapter(adapterItems);
                autoCompleteTextView.setText(newName,false);
                // create a new adapter for the autoCompleteTextView, attach it,
                // and set the text in it to be the new database name

                String input = rename.getText().toString();
                Log.d("EditTextInput", "User pressed enter with input: " + input);
                return true;
            }
            return false;
        }); // Listens for a "DONE" action and renames the database to the new name in the field

        newDB.setOnClickListener(v -> {
            String DB_name = "New DB";
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
            HashSet<String> base = new HashSet<>();
            base.add("New DB");
            // base items set should have "New DB" in it
            Set<String> item = new HashSet<>(
                    sharedPreferences.getStringSet("databases", base)
            );

            int i = 1;
            while(item.contains(DB_name)){
                DB_name = "New DB" + i;
                i++;
            } // creates the unique name

            item.add(DB_name);
            String[] items = item.toArray(new String[0]);
            sharedPreferences.edit().putStringSet("databases", item).putString("chosenDB", DB_name).apply();
            // Adds the new database to the databases set and sets it to be the chosenDB

            adapterItems = new ArrayAdapter<>(requireContext(), R.layout.list_item, items);
            autoCompleteTextView.setAdapter(adapterItems);
            autoCompleteTextView.setText(DB_name,false);
            // create a new adapter for the autoCompleteTextView, attach it,
            // and set the text in it to be the new database's name

            db = new DBHandler(requireContext(), DB_name);
            POIs = db.DbToArrayList();
            adapter = new PoiAdapter(requireContext(), POIs);
            listView.setAdapter(adapter);
            // creates and attach the new PoiAdapter with the chosen database
        });
        // creates and switches to a new database with the name "New DB" + i
        // i makes sure all database names are unique

        return view;
    }
    public void init(View view) {
        newDB = view.findViewById(R.id.newDB);
        rename = view.findViewById(R.id.rename);
        editName = view.findViewById(R.id.editName);
        editDes = view.findViewById(R.id.editDescription);
        editLon = view.findViewById(R.id.editLon);
        editLat = view.findViewById(R.id.editLat);
        addPOI = view.findViewById(R.id.addPOI);
        current = view.findViewById(R.id.current);
        listView = view.findViewById(R.id.scroll);
        autoCompleteTextView = view.findViewById(R.id.autoComplete);
        // initiate all Views

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        HashSet<String> base = new HashSet<>();
        base.add("New DB");
        // base items set should have "New DB" in it
        Set<String> items = sharedPreferences.getStringSet("databases", base);
        String chosenDB = sharedPreferences.getString("chosenDB", "New DB");
        String[] item = items.toArray(new String[0]);
        // find which database is currently chosen and if none is then "New DB" is set

        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.list_item, item);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setText(chosenDB,false);
        rename.setText(chosenDB);
        // create an adapter for the autoCompleteTextView, attach it,
        // and set the text in it to be the chosen database

        String text = getString(R.string.name,"");
        editName.setHint(text);
        text = getString(R.string.longitude,"");
        editLon.setHint(text);
        text = getString(R.string.latitude,"");
        editLat.setHint(text);
        text = getString(R.string.description,"");
        editDes.setHint(text);
        // set the hints in the editText views

        db = new DBHandler(requireContext(), chosenDB);
        // Load the database if chosen one already exists
        // otherwise create a new with that name
        POIs = db.DbToArrayList();
        adapter = new PoiAdapter(requireContext(), POIs);
        listView.setAdapter(adapter);
        // create and attach the PoiAdapter to the ListView
    }
    private void renameDatabase(String oldDbName, String newDbName) {
        File oldDbFile = requireContext().getDatabasePath(oldDbName);
        File newDbFile = new File(oldDbFile.getParent(), newDbName);
        if (oldDbFile.exists()) {
            boolean success = oldDbFile.renameTo(newDbFile);
            if (success) {
                Log.d("Database Rename", "Database renamed successfully.");
            } else {
                Log.e("Database Rename", "Failed to rename database.");
            }
        } else {
            Log.e("Database Rename", "Old database file does not exist.");
        }
    } // renames the database's whose name is oldDbName to newDbName
}