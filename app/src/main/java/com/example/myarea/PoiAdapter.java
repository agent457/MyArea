package com.example.myarea;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PoiAdapter extends ArrayAdapter<POI> {
    private final Context context;
    private ArrayList<POI> list;

    public PoiAdapter(@NonNull Context context, ArrayList<POI> a) {
        super(context, R.layout.db_row, a);
        this.context = context;
        this.list = a;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        Set<String> items = sharedPreferences.getStringSet("databases", new HashSet<>());
        String chosenDB = sharedPreferences.getString("chosenDB", "");
        String DB_Name = items.isEmpty() ? "New DB" : chosenDB; //true -> "New DB" | false -> chosenDB
        DBHandler db = new DBHandler(context, DB_Name);
        // if the set of all databases is empty the database the will be created will be named "New DB",
        // otherwise it will load the currently chosen database.

        POI poi = list.get(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.db_row, parent,false);
        }
        // sets the view to the layout of a row

        TextView name = convertView.findViewById(R.id.lv_name);
        TextView id = convertView.findViewById(R.id.lv_id);
        TextView lon = convertView.findViewById(R.id.lv_long);
        TextView lat = convertView.findViewById(R.id.lv_lat);
        TextView des = convertView.findViewById(R.id.lv_des);
        ImageButton delete = convertView.findViewById(R.id.deleteID);
        String text = context.getString(R.string.name,": "+poi.getName());
        name.setText(text);
        text = context.getString(R.string.id,": "+poi.getId());
        id.setText(text);
        text = context.getString(R.string.longitude,": "+poi.getLong());
        lon.setText(text);
        text = context.getString(R.string.latitude,": "+poi.getLat());
        lat.setText(text);
        text = context.getString(R.string.description,": "+poi.getDescription());
        des.setText(text);
        // initializing all the view's elements and setting their text according to the POI

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = poi.getId();
                db.deletePOI(id); // deletes the POI from the database
                for (POI poi: list) {
                    if(poi.getId()==id){
                        remove(poi);
                    }
                }
                // searches through all the POIs in the list and
                // deletes the one with equal id to the one in the view
            }
        });
        // defining what will happen if the delete button in the view is pressed

        return convertView;
    }
}
