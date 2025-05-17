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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment{

    private ActivityResultLauncher<Intent> mapFilePickerLauncher;
    private MyLocationOverlay myLocationOverlay;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private SharedPreferences sharedPreferences, defaultPreferences;
    private MapView map;
    private Polyline routePolyLine;
    private ArrayList<POI> POIs;
    private SearchView searchView;
    private Context context;
    private int lock = 0;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        AndroidGraphicFactory.createInstance(requireActivity().getApplication());
        init(view); // Initiates all variables and views

        String mapFileName = sharedPreferences.getString("selected_map_file", null);
        if (mapFileName != null) {
            File savedFile = new File(context.getFilesDir(), mapFileName);
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", savedFile);
            openMap(fileUri);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*"); //sets the MIME filter to wildcard (all file types)
            mapFilePickerLauncher.launch(intent);
        }
        // Check if a map is already picked from previous instances
        // If true open it, otherwise request a map file

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        if(searchManager!=null){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        }
        // Connects the searchConfig xml to the searchView using the searchManager

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
        // Connects a QueryTextListener to the search view
        // It is needed to update the results with changes to the query

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLong current = new LatLong(location.getLatitude(), location.getLongitude());
                            map.setCenter(current);
                            // Set current location
                        } else {
                            Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
        // When moving from different fragments this sets the the mapCenter

        return view;
    }

    void handleSuggestionClick(String id) {
        // Find the POI with the given ID
        POI selectedPOI = POIs.stream().filter(
                poi -> String.valueOf(poi.getId()).equals(id)).findFirst().orElse(null);

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
                            }
                        });
            } else {
                Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void calculatePath(LatLong current, LatLong destination) {

        String apiKey = defaultPreferences.getString("GH_APIKEY", null);
        // get the apiKey from the default sharedPreferences
        if(apiKey == null){
            Toast.makeText(context, "No API key provided, please provide one in the settings", Toast.LENGTH_SHORT).show();
            return;
        }
        // get the apiKey from the default sharedPreferences. If it's null return
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                // create the client used for the API requests

                String url =
                        "https://graphhopper.com/api/1/route?" +
                        "point="+ destination.latitude +"," +
                        destination.longitude +
                        "&point="+ current.latitude +"," +
                        current.longitude+
                        "&profile=foot&points_encoded=false" +
                        "&key="+apiKey;
                // Create the URL with the coordinates and API key

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                // build the request

                Response response = client.newCall(request).execute();
                // send the request and record the response
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(context, "Error: " + response.code(), Toast.LENGTH_LONG).show()
                        // Tell the user that the request failed
                    );
                    Log.e("GraphHopper",response.message());
                    return;
                }

                assert response.body() != null;
                String responseData = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseData);
                JSONArray paths = jsonResponse.getJSONArray("paths");
                // acquire the paths (JSONArray) to the destination
                if (paths.length() == 0) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(context, "No path found", Toast.LENGTH_LONG).show()
                        // Tell the user that no path has been found between his location and the destination
                    );
                    return;
                }

                JSONObject path = paths.getJSONObject(0);
                JSONArray points = path.getJSONObject("points").getJSONArray("coordinates");
                // acquire a JSONArray of all the points in path

                routePolyLine.clear(); // clear previous route (if there was one)
                for (int i = 0; i < points.length(); i++) {
                    JSONArray point = points.getJSONArray(i);
                    double lon = point.getDouble(0);
                    double lat = point.getDouble(1);
                    routePolyLine.getLatLongs().add(new LatLong(lat, lon));
                }
                // Create and add polyline to routeLayer

                requireActivity().runOnUiThread(() -> map.getLayerManager().redrawLayers());
                // Redraw the layers now that a new route is present

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    // Tell the user something has gone wrong
                );
                Log.e("calculatePath", Objects.requireNonNull(e.getMessage()));
            }                                                                                                                 
        }).start();
    }

    public void init(View view){
        context = requireContext();
        map = view.findViewById(R.id.map);
        searchView = view.findViewById(R.id.search);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences = context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        // Connecting views and initiating some basic variables

        Bitmap bitmap = new AndroidBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_maps_indicator_current_position));
        Marker marker = new Marker(new LatLong(0,0), bitmap, 0, 0);
        myLocationOverlay = new MyLocationOverlay(marker);
        // defining the base locationOverlay

        try (DBHandler db = new DBHandler(context,sharedPreferences.getString("chosenDB","New DB"))){
            POIs = db.DbToArrayList();
        }
        // loading all the POIs in the database to an ArrayList

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(100)
                .build();
        // defining the LocationRequest
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateMap(Objects.requireNonNull(locationResult.getLastLocation()));
            }
        };
        // defining the LocationCallback

        mapFilePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                        Uri uri = result.getData().getData();
                        // request data in the form of a Uri
                        if(uri!=null){
                            String fileName = getFileNameFromUri(uri);
                            saveFileFromUri(uri,fileName); // Save the map in storage
                            sharedPreferences = context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
                            sharedPreferences.edit().putString("selected_map_file",fileName).apply();
                            // Save the map file name in sharedPreferences
                            openMap(uri); // Open the map Uri
                        }
                    }
                }
        );
        // defining the mapFilePickerLauncher
    }

    private void openMap(Uri uri) {
        try {
            map.getMapScaleBar().setVisible(true);
            map.setBuiltInZoomControls(true);
            map.setZoomLevelMin((byte)18);
            map.setZoomLevel((byte) 19);
            // Setting some arbitrary base settings for the map

            TileCache tileCache = AndroidUtil.createTileCache(
                    context,
                    "mapcache",
                    map.getModel().displayModel.getTileSize(),
                    1f,
                    map.getModel().frameBufferModel.getOverdrawFactor());
            // To avoid redrawing all the tiles all the time,
            // we need to set up a tile cache with an utility method.

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
             * Now we need to set up the process of displaying a map. A map can have several layers,
             * stacked on top of each other. A layer can be a map or some visual elements, such as
             * markers. Here we only show a map based on a mapsforge map file. For this we need a
             * TileRendererLayer. A TileRendererLayer needs a TileCache to hold the generated map
             * tiles, a map file from which the tiles are generated and RenderTheme that defines the
             * appearance of the map.
             */

            map.getLayerManager().getLayers().add(tileRendererLayer);
            map.getLayerManager().getLayers().add(myLocationOverlay);
            routePolyLine = new Polyline(
                    getPaint(AndroidGraphicFactory.INSTANCE.createColor(255, 0, 0, 255),12,Style.STROKE),
                    AndroidGraphicFactory.INSTANCE);
            map.getLayerManager().getLayers().add(routePolyLine);
            // Adding the locationOverlay layer and the routePolyline layer

            /*
             * On its own a tileRendererLayer does not know where to display the map, so we need to
             * associate it with our map.
             */

            StartLocationUpdates();
            // Starting the location updates

            loadPOIs();
            // Loading and displaying all the POIs in the database

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
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
            FixedPixelCircle fixedPixelCircle = new FixedPixelCircle(
                    new LatLong(poi.getLat(),poi.getLong()),5,infill,outline, true);
            map.getLayerManager().getLayers().add(fixedPixelCircle);
        }
        // Adds a circle of fixed size in pixels for every POI in the database
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
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), this::updateMap);
            // request last cached location and pass it onto updateMap
        }
    }

    private void updateMap(Location location) {
        if(location==null){
            Log.w("updateMap", "Location is null. Skipping update.");
            return;
        }
        // Sometimes the first few location requests return null

        if(lock<3){
            LatLong center = new LatLong(location.getLatitude(),location.getLongitude());
            map.setCenter(center);
            Log.d("newCenter", center.toString());
            lock++;
        }
        // Sometimes the first few location request are very inaccurate
        // I set the center a few times to ensure accuracy

        myLocationOverlay.setPosition(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        Log.d("current location", location.toString());
        // Set the position of the location overlay
    }

    private static Paint getPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    @SuppressLint("MissingPermission")
    private void StartLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
        updateGPS();
    }

}