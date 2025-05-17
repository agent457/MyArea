package com.example.myarea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    Fragment MapFragment, EditorFragment, SettingsFragment;
    ActivityResultLauncher<String> locationPermissionLauncher;


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
        init(); // initializes all the variables
        toolbar.setNavigationOnClickListener(v -> {
            // defines what to do when the in-app "back" button is pressed
            getSupportFragmentManager().popBackStackImmediate();
            // returns to previous fragment
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
            if(fragment instanceof MapFragment){
                // if on the MapFragment hides the "back" button
                Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // defines what to do when the phone's "back" button is pressed (not the in-app button)
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
                if(fragment instanceof MapFragment){
                    finishAffinity();
                    return;
                }
                getSupportFragmentManager().popBackStackImmediate();
                // returns to previous fragment (exits the app if on MapFragment)
                fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
                if(fragment instanceof  MapFragment){
                    // if on the MapFragment hides the "back" button
                    Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.editor) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment,EditorFragment).addToBackStack(null).commit();
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.back));
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // if editor fragment is chosen start it and enable the in-app "back" button
        }
        if (item.getItemId() == R.id.settings){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, SettingsFragment).addToBackStack(null).commit();
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.back));
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // if settings fragment is chosen start it and enable the in-app "back" button
        }
        return true;
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
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        MapFragment = new MapFragment();
        EditorFragment = new EditorFragment();
        SettingsFragment = new SettingsFragment();
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if(isGranted){
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment,
                                MapFragment).commit();
                    }
                    else {
                        Toast.makeText(this,"This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

}