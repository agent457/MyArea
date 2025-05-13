package com.example.myarea;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
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
 * Use the {@link EditorFragment#newInstance} factory method to
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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditorFragment newInstance(String param1, String param2) {
        EditorFragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_editor, container, false);
        init(view);

        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(requireContext(),"Loading " + item + " POI database", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("chosenDB", item);
            editor.apply();
            rename.setText(item);
            db = new DBHandler(requireContext(), item);
            POIs = db.loadDB();
            adapter = new PoiAdapter(requireContext(), POIs);
            listView.setAdapter(adapter);
        });
        addPOI.setOnClickListener(v -> {
            if (editName.getText() == null || editDes.getText() == null || editLon.getText() == null || editLat.getText() == null) {
                Toast.makeText(requireContext(), "Please enter all the data", Toast.LENGTH_SHORT).show();
                return;
            }
            db.addPOI(String.valueOf(editName.getText()), String.valueOf(editDes.getText()), Double.parseDouble(String.valueOf(editLon.getText())), Double.parseDouble(String.valueOf(editLat.getText())));
            POIs.add(db.getLast());
            adapter.notifyDataSetChanged();

            Toast.makeText(requireContext(), "Added POI", Toast.LENGTH_SHORT).show();
        });
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
        });
        rename.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)){
                String newName = v.getText().toString();
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
                String chosenDB = sharedPreferences.getString("chosenDB", "");
                Set<String> items_set = new HashSet<>(
                        sharedPreferences.getStringSet("databases", new HashSet<>())
                );
                items_set.remove(chosenDB);
                items_set.add(newName);
                renameDatabase(db.getDB_NAME(),newName);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("databases",items_set);
                editor.putString("chosenDB", newName);
                editor.apply();
                String[] items_array = items_set.toArray(new String[0]);
                adapterItems = new ArrayAdapter<>(requireContext(), R.layout.list_item, items_array);
                autoCompleteTextView.setAdapter(adapterItems);
                autoCompleteTextView.setText(newName,false);
                String input = rename.getText().toString();
                Log.d("EditTextInput", "User pressed enter with input: " + input);
                return true;
            }
            return false;
        });
        newDB.setOnClickListener(v -> {
            String DB_name = "New DB";
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
            Set<String> item = new HashSet<>(
                    sharedPreferences.getStringSet("databases", new HashSet<>())
            );
            int i = 1;
            while(item.contains(DB_name)){
                DB_name = DB_name + i;
            }
            item.add(DB_name);
            String[] items = item.toArray(new String[0]);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("databases", item);
            editor.apply();
            adapterItems = new ArrayAdapter<>(requireContext(), R.layout.list_item, items);
            autoCompleteTextView.setAdapter(adapterItems);
            autoCompleteTextView.setText(DB_name,false);
            db = new DBHandler(requireContext(), DB_name);
            POIs = db.loadDB();
            adapter = new PoiAdapter(requireContext(), POIs);
            listView.setAdapter(adapter);
        });



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

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        Set<String> items = sharedPreferences.getStringSet("databases", new HashSet<>());
        String chosenDB = sharedPreferences.getString("chosenDB", "");
        String[] item = items.toArray(new String[0]);
        String DB_Name = item.length == 0 ? "New DB" : chosenDB;

        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.list_item, item);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setText(chosenDB,false);


        String text = getString(R.string.name,"");
        editName.setHint(text);
        text = getString(R.string.longitude,"");
        editLon.setHint(text);
        text = getString(R.string.latitude,"");
        editLat.setHint(text);
        text = getString(R.string.description,"");
        editDes.setHint(text);


        db = new DBHandler(requireContext(), DB_Name);
        POIs = db.loadDB();
        adapter = new PoiAdapter(requireContext(), POIs);
        listView.setAdapter(adapter);
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
    }
}