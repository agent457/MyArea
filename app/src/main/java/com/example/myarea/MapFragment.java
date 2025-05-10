package com.example.myarea;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.android.layers.MyLocationOverlay;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements LocationListener {

    private static final int SELECT_MAP_FILE = 0;
    public static final int PERMISSIONS_FINE_LOCATION = 99;
    private MyLocationOverlay myLocationOverlay;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private SharedPreferences sharedPreferences, defaultPreferences;
    private MapView map;
    private ArrayList<POI> POIs;
    private SearchView searchView;
    private Context context;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        AndroidGraphicFactory.createInstance(requireActivity().getApplication());
        init(view);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        String mapFileName = sharedPreferences.getString("selected_map_file", null);
        if(mapFileName!=null){
            File savedFile = new File(context.getFilesDir(), mapFileName);
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", savedFile);
            openMap(fileUri);
        }else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, SELECT_MAP_FILE);
        }

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        if(searchManager!=null){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(100)
                .build();
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateMap(locationResult.getLastLocation());
            }
        };

        StartLocationUpdates();

        return view;
    }

    void handleSuggestionClick(String id) {
        // Find the POI with the given ID
        POI selectedPOI = POIs.stream().filter(poi -> String.valueOf(poi.getId()).equals(id)).findFirst().orElse(null);

        if (selectedPOI != null) {
            // Get current location from GPS
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(requireActivity(), location -> {
                            if (location != null) {
                                // Set destination to selected POI
                                LatLong destination = new LatLong(selectedPOI.getLat(), selectedPOI.getLong());

                                // Set current location
                                LatLong current = new LatLong(location.getLatitude(), location.getLongitude());

                                // Calculate and display path
                                calculatePath(current, destination);
                            } else {
                                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "POI not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculatePath(LatLong current, LatLong destination) {

        String apiKey = defaultPreferences.getString("GH_APIKEY", null);
        if(apiKey == null){
            Toast.makeText(context, "No API key provided, please provide one in the settings", Toast.LENGTH_SHORT).show();
        }
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                
                
                // Create the URL with the coordinates and API key
                String url = "https://graphhopper.com/api/1/route?" +
                        "point="+ destination.latitude +"," +
                        destination.longitude +
                        "&point="+ current.latitude +"," +
                        current.longitude+
                        "&profile=foot&points_encoded=false" +
                        "&key="+apiKey;

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(context, "Error: " + response.code(), Toast.LENGTH_LONG).show()
                    );
                    Log.e("GraphHopper",response.message());
                    return;
                }

                assert response.body() != null;
                String responseData = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseData);
                JSONArray paths = jsonResponse.getJSONArray("paths");
                if (paths.length() == 0) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(context, "No path found", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                JSONObject path = paths.getJSONObject(0);
                JSONArray points = path.getJSONObject("points").getJSONArray("coordinates");

                // Create paint for the polyline
                Paint paint = getPaint(AndroidGraphicFactory.INSTANCE.createColor(255, 0, 0, 255),10,Style.STROKE);

                // Create and add polyline to map
                Polyline routePolyLine = new Polyline(paint, AndroidGraphicFactory.INSTANCE);
                for (int i = 0; i < points.length(); i++) {
                    JSONArray point = points.getJSONArray(i);
                    double lon = point.getDouble(0);
                    double lat = point.getDouble(1);
                    routePolyLine.getLatLongs().add(new LatLong(lat, lon));
                }

                requireActivity().runOnUiThread(() -> {
                    map.getLayerManager().getLayers().add(routePolyLine);
                    map.getLayerManager().redrawLayers();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }                                                                                                                 
        }).start();
    }

    public void init(View view){
        context = requireContext();
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences = context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        map = view.findViewById(R.id.map);
        DBHandler db = new DBHandler(context,sharedPreferences.getString("chosenDB","new DB"));
        POIs = db.loadDB();
        searchView = view.findViewById(R.id.search);
        updateGPS();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_MAP_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                assert uri != null;
                String fileName = getFileNameFromUri(uri);
                saveFileFromUri(uri, fileName);
                SharedPreferences sharedPreferences = context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("selected_map_file", fileName);
                editor.apply();
                openMap(uri);
            }
        }
    }

    private void openMap(Uri uri) {
        try {
            /*
             * We then make some simple adjustments, such as showing a scale bar and zoom controls.
             */
            map.getMapScaleBar().setVisible(true);
            map.setBuiltInZoomControls(true);
            map.setZoomLevelMin((byte)18);
            /*
             * To avoid redrawing all the tiles all the time, we need to set up a tile cache with an
             * utility method.
             */
            TileCache tileCache = AndroidUtil.createTileCache(
                    context,
                    "mapcache",
                    map.getModel().displayModel.getTileSize(),
                    1f,
                    map.getModel().frameBufferModel.getOverdrawFactor());

            /*
             * Now we need to set up the process of displaying a map. A map can have several layers,
             * stacked on top of each other. A layer can be a map or some visual elements, such as
             * markers. Here we only show a map based on a mapsforge map file. For this we need a
             * TileRendererLayer. A TileRendererLayer needs a TileCache to hold the generated map
             * tiles, a map file from which the tiles are generated and RenderTheme that defines the
             * appearance of the map.
             */

            FileInputStream fis = (FileInputStream) context.getContentResolver().openInputStream(uri);
            if(fis==null){
                throw new IllegalArgumentException("Failed to open map file.");
            }
            MapDataStore mapDataStore = new MapFile(fis);
            TileRendererLayer tileRendererLayer = new TileRendererLayer(
                    tileCache,
                    mapDataStore,
                    map.getModel().mapViewPosition,
                    AndroidGraphicFactory.INSTANCE);
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

            /*
             * On its own a tileRendererLayer does not know where to display the map, so we need to
             * associate it with our map.
             */
            map.getLayerManager().getLayers().add(tileRendererLayer);

            /*
             * The map also needs to know which area to display and at what zoom level.
             * Note: this map position is specific to Berlin area.
             */

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(requireActivity(), location -> {
                            if (location != null) {
                                // Set current location
                                LatLong current = new LatLong(location.getLatitude(), location.getLongitude());
                                map.setCenter(current);
                                Bitmap bitmap = new AndroidBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_maps_indicator_current_position));
                                Marker marker = new Marker(current, bitmap, 0, 0);
                                myLocationOverlay = new MyLocationOverlay(marker);
                                map.getLayerManager().getLayers().add(myLocationOverlay);

                            } else {
                                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show();
                                Bitmap bitmap = new AndroidBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_maps_indicator_current_position));
                                Marker marker = new Marker(null, bitmap, 0, 0);
                                myLocationOverlay = new MyLocationOverlay(marker);
                                map.getLayerManager().getLayers().add(myLocationOverlay);
                            }
                        });
            } else {
                Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }

            map.setZoomLevel((byte) 19);

            loadPOIs();

        } catch (Exception e) {
            /*
             * In case of map file errors avoid crash, but developers should handle these cases!
             */
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        /*
         * Whenever your activity exits, some cleanup operations have to be performed lest your app
         * runs out of memory.
         */
        map.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }
    public void loadPOIs(){
        Paint infill = getPaint(AndroidGraphicFactory.INSTANCE.createColor(255, 255, 0, 0),10, Style.FILL);
        Paint outline = getPaint(AndroidGraphicFactory.INSTANCE.createColor(255, 0, 0, 0),10, Style.STROKE);
        for(POI poi:POIs) {
            FixedPixelCircle fixedPixelCircle = new FixedPixelCircle(new LatLong(poi.getLat(),poi.getLong()),5,infill,outline, true);
            map.getLayerManager().getLayers().add(fixedPixelCircle);
        }
    }
    public void saveFileFromUri(Uri uri, String fileName) {
        try {
            // Get InputStream from URI
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            // Define the destination file
            File destinationFile = new File(context.getFilesDir(), fileName);

            // Write to the destination file
            OutputStream outputStream = Files.newOutputStream(destinationFile.toPath());
            byte[] buffer = new byte[1024];
            int bytesRead;

            while (true) {
                assert inputStream != null;
                if ((bytesRead = inputStream.read(buffer)) == -1) break;
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close streams
            inputStream.close();
            outputStream.close();

            Log.d("SaveFile", "File saved successfully at " + destinationFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SaveFile", "Error saving file: " + e.getMessage());
        }
    }
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;

        // Check if the URI scheme is content
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Get the display name from the cursor
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // If the scheme is file, extract the name directly
            fileName = new File(Objects.requireNonNull(uri.getPath())).getName();
        }

        return fileName;
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), this::updateMap);
        }
        else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    private void updateMap(Location location) {
    }

    private static Paint getPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(context, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void StartLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
        updateGPS();
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        myLocationOverlay.setPosition(location.getLatitude(), location.getLongitude(), location.getAccuracy());

        map.setCenter(new LatLong(location.getLatitude(),location.getLongitude()));
    }

}