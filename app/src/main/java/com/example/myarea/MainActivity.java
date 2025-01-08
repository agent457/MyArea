package com.example.myarea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navbar;
    Fragment MapFragment, CoordinatesFragment, EditorFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        navbar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId()==R.id.map){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, MapFragment).commit();
                }
                if (item.getItemId()==R.id.coordinates){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, CoordinatesFragment).commit();
                }
                if (item.getItemId()==R.id.editor){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, EditorFragment).commit();
                }
                return true;
            }
        });

    }
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        if (Intent.ACTION_VIEW.equals(intent.getAction())){
            String id = intent.getDataString();
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);

            if (fragment instanceof MapFragment){
                ((MapFragment) fragment).handleSuggestionClick(id);
            }
        }
    }
    public void init(){
        navbar = findViewById(R.id.navbar);
        MapFragment = new MapFragment();
        CoordinatesFragment = new CoordinatesFragment();
        EditorFragment = new EditorFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, MapFragment).commit();
    }
}