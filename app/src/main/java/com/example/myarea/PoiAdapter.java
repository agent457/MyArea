package com.example.myarea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PoiAdapter extends ArrayAdapter<POI> {
    private Context context;
    private ArrayList<POI> list;

    public PoiAdapter(@NonNull Context context, ArrayList<POI> a) {
        super(context, R.layout.db_row, a);
        this.context=context;
        this.list=a;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        POI poi = list.get(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.db_row,parent,false);
        }
        TextView name = convertView.findViewById(R.id.lv_name);
        TextView id = convertView.findViewById(R.id.lv_id);
        TextView lon = convertView.findViewById(R.id.lv_long);
        TextView lat = convertView.findViewById(R.id.lv_lat);
        TextView des = convertView.findViewById(R.id.lv_des);
        name.setText("Name: " + poi.getName());
        id.setText("ID: " + poi.getId());
        lon.setText("Longitude: " + poi.getLong());
        lat.setText("Latitude: " + poi.getLat());
        des.setText("Description: " + poi.getDescription());
        return convertView;
    }
}
