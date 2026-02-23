package com.example.capteurs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ConstraintLayout rootLayout;
    private TextView accelValueText, statusLabel, valX, valY, valZ;
    private float thresholdLow, thresholdHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadThresholds();
        setupAccelerometer();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.main_root);
        accelValueText = findViewById(R.id.accel_value_text);
        statusLabel = findViewById(R.id.status_label);
        valX = findViewById(R.id.val_x);
        valY = findViewById(R.id.val_y);
        valZ = findViewById(R.id.val_z);
    }

    private void loadThresholds() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.accel_threshold_low, outValue, true);
        thresholdLow = outValue.getFloat();
        
        getResources().getValue(R.dimen.accel_threshold_high, outValue, true);
        thresholdHigh = outValue.getFloat();
    }

    private void setupAccelerometer() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            
            updateUI(magnitude, x, y, z);
        }
    }

    private void updateUI(double magnitude, float x, float y, float z) {
        accelValueText.setText(String.format("%.1f", magnitude));
        valX.setText(String.format("%.1f", x));
        valY.setText(String.format("%.1f", y));
        valZ.setText(String.format("%.1f", z));

        if (magnitude < thresholdLow) {
            applyTheme(R.color.accel_low, R.string.status_calm);
        } else if (magnitude < thresholdHigh) {
            applyTheme(R.color.accel_medium, R.string.status_motion);
        } else {
            applyTheme(R.color.accel_high, R.string.status_acceleration);
        }
    }

    private void applyTheme(int colorResId, int stringResId) {
        rootLayout.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        statusLabel.setText(getString(stringResId));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}