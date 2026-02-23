package com.example.capteurs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView directionText, valX, valY;
    private ImageView directionIcon;
    private View indicatorCard;
    private float motionThreshold;
    private float rotUp, rotDown, rotLeft, rotRight;
    private float currentRotation = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadResources();
        setupAccelerometer();
    }

    private void initViews() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        directionText = findViewById(R.id.direction_text);
        valX = findViewById(R.id.val_x);
        valY = findViewById(R.id.val_y);
        directionIcon = findViewById(R.id.direction_icon);
        indicatorCard = findViewById(R.id.indicator_card);
    }

    private void loadResources() {
        TypedValue outValue = new TypedValue();
        
        getResources().getValue(R.dimen.motion_threshold, outValue, true);
        motionThreshold = outValue.getFloat();

        getResources().getValue(R.dimen.rotation_up, outValue, true);
        rotUp = outValue.getFloat();
        
        getResources().getValue(R.dimen.rotation_down, outValue, true);
        rotDown = outValue.getFloat();
        
        getResources().getValue(R.dimen.rotation_left, outValue, true);
        rotLeft = outValue.getFloat();
        
        getResources().getValue(R.dimen.rotation_right, outValue, true);
        rotRight = outValue.getFloat();
    }

    private void setupAccelerometer() {
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
            updateDirection(x, y);
        }
    }

    private void updateDirection(float x, float y) {
        valX.setText(String.format("%.1f", x));
        valY.setText(String.format("%.1f", y));

        String text = getString(R.string.dir_center);
        float targetRotation = currentRotation; // Par défaut, on garde la rotation actuelle
        boolean isMoving = false;

        if (Math.abs(x) > Math.abs(y)) {
            if (x > motionThreshold) {
                text = getString(R.string.dir_left);
                targetRotation = rotLeft;
                isMoving = true;
            } else if (x < -motionThreshold) {
                text = getString(R.string.dir_right);
                targetRotation = rotRight;
                isMoving = true;
            }
        } else {
            if (y > motionThreshold) {
                text = getString(R.string.dir_down);
                targetRotation = rotDown;
                isMoving = true;
            } else if (y < -motionThreshold) {
                text = getString(R.string.dir_up);
                targetRotation = rotUp;
                isMoving = true;
            }
        }

        directionText.setText(text);
        
        if (isMoving) {
            animateRotation(targetRotation);
            indicatorCard.setAlpha(1.0f);
        } else {
            indicatorCard.setAlpha(0.3f); // Estompe l'icône quand immobile
        }
    }

    private void animateRotation(float targetRotation) {
        if (currentRotation != targetRotation) {
            directionIcon.animate()
                    .rotation(targetRotation)
                    .setDuration(200)
                    .start();
            currentRotation = targetRotation;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}