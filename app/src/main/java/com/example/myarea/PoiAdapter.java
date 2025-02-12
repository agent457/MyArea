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
        this.context = context;
        this.list = a;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        POI poi = list.get(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.db_row, parent,false);
        }
        TextView name = convertView.findViewById(R.id.lv_name);
        TextView id = convertView.findViewById(R.id.lv_id);
        TextView lon = convertView.findViewById(R.id.lv_long);
        TextView lat = convertView.findViewById(R.id.lv_lat);
        TextView des = convertView.findViewById(R.id.lv_des);
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
        return convertView;
    }
}
