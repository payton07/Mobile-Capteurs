package com.example.capteurs;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private MaterialCardView warningCard;
    private TextView warningText;
    private MaterialCardView resultCard;
    private TextView resultText;
    private TextInputEditText searchEditText;
    private Button checkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkMandatorySensors();
        setupSearchLogic();
    }

    private void initViews() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        warningCard = findViewById(R.id.warning_card);
        warningText = findViewById(R.id.warning_text);
        resultCard = findViewById(R.id.result_card);
        resultText = findViewById(R.id.result_text);
        searchEditText = findViewById(R.id.sensor_search_edit_text);
        checkButton = findViewById(R.id.check_sensor_button);
    }

    private void checkMandatorySensors() {
        StringBuilder missingSensors = new StringBuilder();

        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            missingSensors.append(getString(R.string.sensor_barometer)).append(", ");
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) == null) {
            missingSensors.append(getString(R.string.sensor_thermometer)).append(", ");
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) == null) {
            missingSensors.append(getString(R.string.sensor_humidity)).append(", ");
        }

        if (missingSensors.length() > 0) {
            String missingList = missingSensors.substring(0, missingSensors.length() - 2);
            warningCard.setVisibility(View.VISIBLE);
            warningText.setText(getString(R.string.sensor_missing_warning, missingList));
        } else {
            warningCard.setVisibility(View.GONE);
        }
    }

    private void setupSearchLogic() {
        checkButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim().toLowerCase();
            if (query.isEmpty()) {
                updateResultView(getString(R.string.error_empty_search), Color.GRAY);
                return;
            }
            performSensorSearch(query);
        });
    }

    private void performSensorSearch(String query) {
        String translatedQuery = getTranslatedQuery(query);
        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        
        Sensor foundSensor = null;
        for (Sensor s : allSensors) {
            String sensorName = s.getName().toLowerCase();
            String sensorType = s.getStringType().toLowerCase();

            // If query starts with 'type_', we prioritize matching it with the system string type
            if (query.startsWith("type_")) {
                if (sensorType.endsWith("." + translatedQuery) || sensorType.equals(translatedQuery)) {
                    foundSensor = s;
                    break;
                }
            } else {
                if (sensorName.contains(translatedQuery) || sensorType.contains(translatedQuery)) {
                    foundSensor = s;
                    break;
                }
            }
        }

        if (foundSensor != null) {
            updateResultView(getString(R.string.sensor_found, foundSensor.getName()), Color.parseColor("#22C55E"));
        } else {
            updateResultView(getString(R.string.sensor_not_found, query), Color.parseColor("#EF4444"));
        }
    }

    private void updateResultView(String message, int color) {
        resultCard.setVisibility(View.VISIBLE);
        resultText.setText(message);
        resultText.setTextColor(color);
        resultCard.setStrokeColor(color);
    }

    private String getTranslatedQuery(String query) {
        // Handle TYPE_ format by removing the prefix
        if (query.startsWith("type_")) {
            return query.substring(5);
        }

        String[] keys = getResources().getStringArray(R.array.sensor_query_keys);
        String[] values = getResources().getStringArray(R.array.sensor_query_values);

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(query)) return values[i];
        }
        return query;
    }
}