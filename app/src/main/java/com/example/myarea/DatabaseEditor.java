package com.example.myarea;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;

public class DatabaseEditor extends BaseActivity {
    private EditText editName, editDes, editLon, editLat, searchID;
    private Button addPOI, deleteID;
    private TextView title;
    private ListView listView;
    private BottomNavigationView navbar;
    private DBHandler db;
    private ArrayList<POI> POIs;
    PoiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_database);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        addPOI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editName.getText() == null || editDes.getText() == null || editLon.getText() == null || editLat.getText() == null) {
                    Toast.makeText(DatabaseEditor.this, "Please enter all the data", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.addPOI(String.valueOf(editName.getText()), String.valueOf(editDes.getText()), Double.parseDouble(String.valueOf(editLon.getText())), Double.parseDouble(String.valueOf(editLat.getText())));
                Toast.makeText(DatabaseEditor.this, "Added POI", Toast.LENGTH_SHORT).show();
            }
        });
        deleteID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deletePOI(Integer.parseInt(String.valueOf(searchID.getText())));
            }
        });
        navbar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
    }

    public void init() {
        editName = findViewById(R.id.editName);
        editDes = findViewById(R.id.editDescription);
        editLon = findViewById(R.id.editLon);
        editLat = findViewById(R.id.editLat);
        searchID = findViewById(R.id.list);
        addPOI = findViewById(R.id.addPOI);
        deleteID = findViewById(R.id.deleteID);
        title = findViewById(R.id.title);
        listView = findViewById(R.id.scroll);
        navbar = findViewById(R.id.bottomNavigationView);
        db = new DBHandler(DatabaseEditor.this, "Yoana");
        POIs = loadDB(db);
        adapter = new PoiAdapter(DatabaseEditor.this, POIs);
        listView.setAdapter(adapter);
    }
    public ArrayList<POI> loadDB(DBHandler dbH){
        ArrayList<POI> POIs = new ArrayList<>();
        SQLiteDatabase db = dbH.getReadableDatabase();
        Cursor cursor = null;
        String query = "SELECT id, name, description, lon, lat FROM POIs";
        try {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(dbH.getID_COL()));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(dbH.getNAME_COL()));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(dbH.getDESCRIPTION_COL()));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(dbH.getLONG_COL()));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(dbH.getLAT_COL()));

                POI poi = new POI(id, name, description, lon, lat);
                POIs.add(poi);
            }
        }finally {
            if (cursor!=null){
                cursor.close();
            }
        }
        return POIs;
    }
}