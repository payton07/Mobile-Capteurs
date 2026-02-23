package com.example.capteurs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        
        // Initialiser la config Osmdroid AVANT le setContentView
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
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
        map.setTileSource(TileSourceFactory.MAPNIK); // Source de la carte (OpenStreetMap standard)
        map.setMultiTouchControls(true); // Activer le zoom (Pinch-to-zoom)
        
        IMapController mapController = map.getController();
        mapController.setZoom(18.0); // Zoom initial plus proche
        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522); // Paris par défaut
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
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 5, this);
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
        marker.setPosition(currentPoint);
        
        if (!initialCenteringDone) {
            map.getController().animateTo(currentPoint);
            initialCenteringDone = true;
        }
        
        map.invalidate(); // Forcer le rafraîchissement de la carte
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
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
        if (locationManager != null) locationManager.removeUpdates(this);
    }
}