package com.example.capteurs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private TextView proximityStatusText;
    private ImageView proximityIcon;
    private MaterialCardView proximityIconCard;
    private float proximityThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadResources();
        setupProximitySensor();
    }

    private void initViews() {
        proximityStatusText = findViewById(R.id.proximity_status_text);
        proximityIcon = findViewById(R.id.proximity_icon);
        proximityIconCard = findViewById(R.id.proximity_icon_card);
    }

    private void loadResources() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.proximity_threshold, outValue, true);
        proximityThreshold = outValue.getFloat();
    }

    private void setupProximitySensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor == null) {
            Toast.makeText(this, "Capteur de proximité non disponible !", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            updateUI(distance);
        }
    }

    private void updateUI(float distance) {
        if (distance < proximityThreshold) {
            // ÉTAT PROCHE : Image de proximité (ic_near)
            proximityStatusText.setText(getString(R.string.status_near));
            proximityIcon.setImageResource(R.drawable.ic_near);
            
            // Changement visuel fort
            proximityStatusText.setTextColor(ContextCompat.getColor(this, R.color.accel_low)); 
            proximityIcon.setColorFilter(ContextCompat.getColor(this, R.color.accel_low));
            proximityIconCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface_variant));
            proximityIconCard.setStrokeColor(ContextCompat.getColor(this, R.color.accel_low));
        } else {
            // ÉTAT LOIN : Image d'éloignement (ic_far)
            proximityStatusText.setText(getString(R.string.status_far));
            proximityIcon.setImageResource(R.drawable.ic_far);
            
            // Retour à l'état normal
            proximityStatusText.setTextColor(ContextCompat.getColor(this, R.color.white));
            proximityIcon.setColorFilter(ContextCompat.getColor(this, R.color.secondary));
            proximityIconCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.background));
            proximityIconCard.setStrokeColor(ContextCompat.getColor(this, R.color.secondary));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}