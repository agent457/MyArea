package com.example.myarea;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapActivity extends BaseActivity implements RotationGestureDetector.OnRotationGestureListener, View.OnTouchListener {
    BottomNavigationView navbar;
    SubsamplingScaleImageView mapImage;
    private RotationGestureDetector mRotationDetector;

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
        mapImage = findViewById(R.id.map);
        mapImage.setOnTouchListener(this);
        mRotationDetector = new RotationGestureDetector(this);
        mapImage.setImage(ImageSource.resource(R.drawable.checkerboard));
        mapImage.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_OUTSIDE);
    }

    @Override
    public void OnRotation(RotationGestureDetector rotationDetector) {
        float angle = rotationDetector.getAngle();
        mapImage.setRotation(mapImage.getRotation() + (-angle));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mRotationDetector.onTouchEvent(event);
        return false;
    }
}