package com.example.myarea;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class DatabaseEditor extends BaseActivity {
    private AutoCompleteTextView editName, editDes, editLon, editLat, searchID;
    private Button addPOI, deleteID;
    private TextView title;
    private ScrollView scroll;
    private BottomNavigationView navbar;
    private DBHandler db;

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
                if(editName.getText()==null || editDes.getText()==null || editLon.getText()==null || editLat.getText()==null){
                    Toast.makeText(Objects.requireNonNull(getCurrentFocus()).getContext(), "Please enter all the data",Toast.LENGTH_SHORT).show();
                    return;
                }
                db.addPOI(String.valueOf(editName.getText()),String.valueOf(editDes.getText()),Double.parseDouble(String.valueOf(editLon.getText())),Double.parseDouble(String.valueOf(editLat.getText())));

            }
        });

    }
    public void init(){
        editName = findViewById(R.id.editName);
        editDes = findViewById(R.id.editDescription);
        editLon = findViewById(R.id.editLon);
        editLat = findViewById(R.id.editLat);
        searchID = findViewById(R.id.searchID);
        addPOI = findViewById(R.id.addPOI);
        deleteID = findViewById(R.id.deleteID);
        title = findViewById(R.id.title);
        scroll = findViewById(R.id.scroll);
        navbar = findViewById(R.id.bottomNavigationView);
        db = new DBHandler(Objects.requireNonNull(getCurrentFocus()).getContext(),"Yoana");
    }
}