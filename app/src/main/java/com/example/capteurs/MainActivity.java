package com.example.capteurs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int PERMISSION_REQUEST_LOCATION = 100;
    private LocationManager locationManager;
    private TextView valLatitude, valLongitude, valAltitude, statusText;
    
    private MapView map;
    private Marker marker;
    private boolean initialCenteringDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Context ctx = getApplicationContext();
        // Set User Agent for OSM servers requirement
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));
        
        setContentView(R.layout.activity_main);

        initViews();
        setupMap();
        setupLocationManager();
    }

    private void initViews() {
        valLatitude = findViewById(R.id.val_latitude);
        valLongitude = findViewById(R.id.val_longitude);
        valAltitude = findViewById(R.id.val_altitude);
        statusText = findViewById(R.id.status_text);
        map = findViewById(R.id.map_view);
    }

    private void setupMap() {
        if (map == null) return;

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        
        IMapController mapController = map.getController();
        mapController.setZoom(18.0);
        
        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522);
        mapController.setCenter(startPoint);

        marker = new Marker(map);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Votre position");
        map.getOverlays().add(marker);
    }

    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        try {
            if (locationManager == null) return;
            
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 5, this);
            } else {
                Toast.makeText(this, "Activez votre localisation !", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Erreur de permission !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        statusText.setText("Position en direct");
        valLatitude.setText(String.format("%.6f", lat));
        valLongitude.setText(String.format("%.6f", lng));
        valAltitude.setText(String.format("Altitude: %.1f m", location.getAltitude()));

        GeoPoint currentPoint = new GeoPoint(lat, lng);
        if (marker != null) {
            marker.setPosition(currentPoint);
        }
        
        if (!initialCenteringDone && map != null) {
            map.getController().animateTo(currentPoint);
            initialCenteringDone = true;
        }
        
        if (map != null) map.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume(); 
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}