package com.example.myarea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Executor;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CoordinatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoordinatesFragment extends Fragment {

    public static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates;
    SwitchCompat sw_locationUpdates, sw_gps;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CoordinatesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CoordinatesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoordinatesFragment newInstance(String param1, String param2) {
        CoordinatesFragment fragment = new CoordinatesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_coordinates, container, false);
        init(view);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(100)
                .build();
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValues(locationResult.getLastLocation());
            }
        };
        sw_gps.setOnClickListener(v -> {
            if(sw_gps.isChecked()){
                locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdateDelayMillis(100)
                        .build();
                tv_sensor.setText("Using GPS sensors");
            }
            else{
                locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 100)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdateDelayMillis(100)
                        .build();
                tv_sensor.setText("Using Towers + WIFI");
            }
        });

        sw_locationUpdates.setOnClickListener(v -> {
            if(sw_locationUpdates.isChecked()){
                StartLocationUpdates();
            }
            else{
                stopLocationUpdates();
            }
        });

        updateGPS();

        return view;
    }
    private void init(View view){
        tv_lat= view.findViewById(R.id.tv_lat);
        tv_lon= view.findViewById(R.id.tv_lon);
        tv_altitude= view.findViewById(R.id.tv_altitude);
        tv_accuracy= view.findViewById(R.id.tv_accuracy);
        tv_speed= view.findViewById(R.id.tv_speed);
        tv_sensor= view.findViewById(R.id.tv_sensor);
        tv_updates= view.findViewById(R.id.tv_updates);
        sw_locationUpdates = view.findViewById(R.id.sw_locationsupdates);
        sw_gps = view.findViewById(R.id.sw_gps);
    }
    private void stopLocationUpdates() {
        tv_updates.setText("Location is not being tracked");
        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("MissingPermission")
    private void StartLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(requireContext(), "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void updateGPS(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), this::updateUIValues);
        }
        else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    private void updateUIValues(Location location){
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_altitude.setText("Not available");
        }
        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else {
            tv_speed.setText("Not available");
        }

    }
}