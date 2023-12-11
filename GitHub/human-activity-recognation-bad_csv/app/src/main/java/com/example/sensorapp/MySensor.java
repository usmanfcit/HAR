package com.example.sensorapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MySensor implements SensorEventListener {
    private SensorManager sensorManager;
    private List<Sensor> sensorList;
    private CSVWriter csvWriter;
    private int dataCount = 0;

    // Constructor
    public MySensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        initializeCSVWriter();
    }

    // Initialize CSVWriter
    private void initializeCSVWriter() {
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SensorData");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "SensorData_" + timestamp + ".csv";
            File file = new File(directory, fileName);

            csvWriter = new CSVWriter(new FileWriter(file));
            writeHeaders();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Write CSV headers
    private void writeHeaders() {
        String[] headers = getHeaders();
        csvWriter.writeNext(headers);
    }

    // Register sensor listeners for all sensors
    public void registerSensors() {
        for (Sensor sensor : sensorList) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // Unregister sensor listeners
    public void unregisterSensors() {
        sensorManager.unregisterListener(this);
        try {
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sensor event callback
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        // Check if sensor type is known
        if (isKnownSensorType(sensorType)) {
            writeSensorData(sensorType, values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Check if sensor type is known
        if (isKnownSensorType(sensor.getType())) {
            // Handle accuracy changes if needed
        }
    }

    // Utility method to convert sensor type to string
    private String sensorTypeToString(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetic Field";
            case Sensor.TYPE_LIGHT:
                return "Light Sensor";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity Sensor";
            case Sensor.TYPE_PRESSURE:
                return "Pressure Sensor";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Ambient Temperature";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Relative Humidity";
            // Add more cases for other sensor types as needed
            default:
                return "Unknown Sensor Type " + sensorType;
        }
    }

    // Check if sensor type is known
    private boolean isKnownSensorType(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_PROXIMITY:
            case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return true;
            default:
                return false;
        }
    }

    // Write sensor data to CSV
    // Write sensor data to CSV
    private void writeSensorData(int sensorType, float[] values) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());

        String[] dataRow = new String[values.length * 2 + 3];
        dataRow[0] = "Timestamp";
        dataRow[1] = timestamp;
        dataRow[2] = "SensorType";

        int headerIndex = 3;

        // Populate headers and data
        for (int i = 0; i < values.length; i++) {
            String sensorAxis = getSensorAxis(i);
            dataRow[headerIndex++] = sensorTypeToString(sensorType) + "-" + sensorAxis;
            dataRow[headerIndex++] = String.valueOf(values[i]);
        }

        // Write to CSV
        csvWriter.writeNext(dataRow);
        dataCount++;
    }

    // Get sensor axis label (e.g., x, y, z)
    private String getSensorAxis(int index) {
        switch (index) {
            case 0:
                return "x";
            case 1:
                return "y";
            case 2:
                return "z";
            default:
                return String.valueOf(index);
        }
    }

    // Get CSV headers
    private String[] getHeaders() {
        int totalHeaders = 3; // Timestamp, SensorType, SensorData (x, y, z for each sensor)
        for (Sensor sensor : sensorList) {
            totalHeaders += Math.min((int) sensor.getMaximumRange(), 3) * 2; // Using Math.min to limit to 3 values (x, y, z)
        }

        String[] headers = new String[totalHeaders];
        headers[0] = "Timestamp";
        headers[1] = "SensorType";
        headers[2] = "DataCount";

        int headerIndex = 3;

        // Populate headers
        for (Sensor sensor : sensorList) {
            int valuesCount = Math.min((int) sensor.getMaximumRange(), 3); // Limit to 3 values (x, y, z)
            for (int i = 0; i < valuesCount; i++) {
                String sensorAxis = getSensorAxis(i);
                headers[headerIndex++] = sensorTypeToString(sensor.getType()) + "-" + sensorAxis;
                headers[headerIndex++] = "Value";
            }
        }

        return headers;
    }
}

