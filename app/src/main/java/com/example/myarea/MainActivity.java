package com.example.myarea;

import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    Fragment MapFragment, EditorFragment, SettingsFragment;


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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().popBackStackImmediate();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("Main Fragment");
                if(fragment != null && fragment.isVisible()){
                    Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });
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
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back));
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (item.getItemId() == R.id.settings){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, SettingsFragment).addToBackStack(null).commit();
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back));
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    @NonNull
    @Override
    public OnBackInvokedDispatcher getOnBackInvokedDispatcher() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("Main Fragment");
        if(fragment != null && fragment.isVisible()){
            getSupportFragmentManager().popBackStackImmediate();
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        return super.getOnBackInvokedDispatcher();

    }

    public void init(){
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();
        MapFragment = new MapFragment();
        EditorFragment = new EditorFragment();
        SettingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, MapFragment, "Main Fragment").commit();
    }

}