package com.example.myarea;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
public class MapFragment extends Fragment {

    private static final int SELECT_MAP_FILE = 0;
    public static final int PERMISSIONS_FINE_LOCATION = 99;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    private MapView map;
    private ArrayList<POI> POIs;
    private SearchView searchView;

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

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        String mapFileName = sharedPreferences.getString("selected_map_file", null);
        if(mapFileName!=null){
            File savedFile = new File(requireContext().getFilesDir(), mapFileName);
            Uri fileUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", savedFile);
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

        updateGPS();
        StartLocationUpdates();

        return view;
    }

    void handleSuggestionClick(String id) {
        // Find the POI with the given ID
        POI selectedPOI = POIs.stream().filter(poi -> String.valueOf(poi.getId()).equals(id)).findFirst().orElse(null);

        if (selectedPOI != null) {
            // Get current location from GPS
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                                Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "POI not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculatePath(LatLong current, LatLong destination) {

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String apiKey = getString(R.string.GH_APIKEY);
                
                // Create the URL with the coordinates and API key
                String url = "https://graphhopper.com/api/1/route?" +
                        "point="+ destination.latitude +"," +
                        destination.longitude +
                        "&point="+ current.latitude +"," +
                        current.longitude+
                        "&profile=foot&points_encoded=false" +
                        "&key="+getString(R.string.GH_APIKEY);

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_LONG).show()
                    );
                    Log.e("GraphHopper",response.message());
                    return;
                }

                String responseData = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseData);
                JSONArray paths = jsonResponse.getJSONArray("paths");
                if (paths.length() == 0) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "No path found", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                JSONObject path = paths.getJSONObject(0);
                JSONArray points = path.getJSONObject("points").getJSONArray("coordinates");

                // Create paint for the polyline
                Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
                paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(255, 0, 0, 255)); // Blue color
                paint.setStrokeWidth(10);
                paint.setStyle(Style.STROKE);

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
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    public void init(View view){
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
        map = view.findViewById(R.id.map);
        DBHandler db = new DBHandler(requireContext(),sharedPreferences.getString("chosenDB","new DB"));
        POIs = db.loadDB();
        searchView = view.findViewById(R.id.search);
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
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);
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
                    requireContext(),
                    "mapcache",
                    map.getModel().displayModel.getTileSize(),
                    1f,
                    map.getModel().frameBufferModel.getOverdrawFactor());

            /*
             * Now we need to set up the process of displaying a map. A map can have several layers,
             * stacked on top of each other. A layer can be a map or some visual elements, such as
             * markers. Here we only show a map based on a mapsforge map file. For this we need a
             * TileRendererLayer. A TileRendererLayer needs a TileCache to hold the generated map
             * tiles, a map file from which the tiles are generated and Rendertheme that defines the
             * appearance of the map.
             */

            FileInputStream fis = (FileInputStream) requireContext().getContentResolver().openInputStream(uri);
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
            map.setCenter(new LatLong(31.940873, 34.824437));
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
        Drawable drawableU = ContextCompat.getDrawable(requireContext(),R.drawable.poi);
        assert drawableU != null;
        android.graphics.Bitmap bitmapU = ((BitmapDrawable)drawableU).getBitmap();
        Drawable drawableC = new BitmapDrawable(getResources(), android.graphics.Bitmap.createScaledBitmap(bitmapU,50,50,true));
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawableC);
        for(POI poi:POIs) {
            Marker marker = new Marker(new LatLong(poi.getLat(),poi.getLong()),bitmap,0,-120);
            map.getLayerManager().getLayers().add(marker);
        }
    }
    public void saveFileFromUri(Uri uri, String fileName) {
        try {
            // Get InputStream from URI
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);

            // Define the destination file
            File destinationFile = new File(requireContext().getFilesDir(), fileName);

            // Write to the destination file
            OutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
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
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Get the display name from the cursor
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // If the scheme is file, extract the name directly
            fileName = new File(uri.getPath()).getName();
        }

        return fileName;
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), this::updateMap);
        }
        else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    private void updateMap(Location location) {

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

    @SuppressLint("MissingPermission")
    private void StartLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
        updateGPS();
    }
}