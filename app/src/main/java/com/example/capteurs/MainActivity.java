package com.example.capteurs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private CameraManager cameraManager;
    private String cameraId;

    private TextView flashStatusText;
    private ImageView flashIcon;
    private MaterialCardView flashIconCard;

    private boolean isFlashOn = false;
    private float shakeThreshold;
    private long lastShakeTime = 0;
    private static final int SHAKE_COOLDOWN_MS = 1000; // Délai entre deux secousses pour éviter les répétitions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadResources();
        setupHardware();
    }

    private void initViews() {
        flashStatusText = findViewById(R.id.flash_status_text);
        flashIcon = findViewById(R.id.flash_icon);
        flashIconCard = findViewById(R.id.flash_icon_card);
    }

    private void loadResources() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.shake_threshold, outValue, true);
        shakeThreshold = outValue.getFloat();
    }

    private void setupHardware() {
        // Sensor setup
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Camera setup for Flash
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Log.e("ShakeFlash", "Failed to access Camera.", e);
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Accéléromètre non disponible !", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        // Turn off flash when app is paused for safety
        if (isFlashOn) toggleFlash();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            long currentTime = System.currentTimeMillis();

            if (magnitude > shakeThreshold) {
                if (currentTime - lastShakeTime > SHAKE_COOLDOWN_MS) {
                    lastShakeTime = currentTime;
                    toggleFlash();
                }
            }
        }
    }

    private void toggleFlash() {
        try {
            isFlashOn = !isFlashOn;
            cameraManager.setTorchMode(cameraId, isFlashOn);
            updateUI();
        } catch (CameraAccessException e) {
            Log.e("ShakeFlash", "Failed to toggle Flash.", e);
        }
    }

    private void updateUI() {
        if (isFlashOn) {
            flashStatusText.setText(getString(R.string.flash_on));
            flashStatusText.setTextColor(ContextCompat.getColor(this, R.color.accel_low)); // Réutilisation du vert
            flashIcon.setColorFilter(ContextCompat.getColor(this, R.color.accel_low));
            flashIconCard.setStrokeColor(ContextCompat.getColor(this, R.color.accel_low));
        } else {
            flashStatusText.setText(getString(R.string.flash_off));
            flashStatusText.setTextColor(ContextCompat.getColor(this, R.color.white));
            flashIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
            flashIconCard.setStrokeColor(ContextCompat.getColor(this, R.color.outline));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}