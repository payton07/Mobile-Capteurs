package com.example.capteurs;

import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

    private List<Sensor> sensorList;

    public SensorAdapter(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sensor_item, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        Sensor sensor = sensorList.get(position);
        holder.sensorName.setText(sensor.getName());
        holder.sensorVendor.setText(sensor.getVendor());
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public static class SensorViewHolder extends RecyclerView.ViewHolder {
        TextView sensorName;
        TextView sensorVendor;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            sensorName = itemView.findViewById(R.id.sensor_name);
            sensorVendor = itemView.findViewById(R.id.sensor_vendor);
        }
    }
}
