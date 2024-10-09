package com.example.myarea;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapActivity extends BaseActivity implements View.OnTouchListener {
    BottomNavigationView navbar;
    RotatableImageView mapImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        navbar.setOnItemSelectedListener(this::onOptionsItemSelected);
    }
    public void init(){
        navbar=findViewById(R.id.bottom_navbar1);
        mapImage=findViewById(R.id.map);
        mapImage.setImage(ImageSource.resource(R.drawable.checkerboard));
        mapImage.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(view==mapImage){
            float startAngle = 0f;
            if (event.getPointerCount() == 2) { // Detects two fingers for rotation
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                float angle = (float) Math.toDegrees(Math.atan2(y, x));

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        startAngle = angle;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaAngle = angle - startAngle;
                        mapImage.setRotation(mapImage.getRotation() + deltaAngle);
                        startAngle = angle;
                        break;
                }
            }
            return true;
        }
        return false;
    }
}