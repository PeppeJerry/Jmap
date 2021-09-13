package com.thesis.jmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thesis.jmap.GPS.GPS;
import com.thesis.jmap.GPS.IGPS;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Dichiarazione delle variabili e costanti
    private static final int PERMISSION_LOCATION = 69;
    IGPS igps;
    SensorManager sensorManager;
    Sensor accelerometer;
    TextView tv_address, tv_lat, tv_lon, tv_alt,tv_x,tv_y,tv_z,tv_m;
    Switch sw_location;
    GPS gps;
    double lat,lon,alt,x,y,z,m;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TextView e Switch per il GPS
        tv_lat = findViewById(R.id.lat);
        tv_lon = findViewById(R.id.lon);
        tv_alt = findViewById(R.id.alt);
        tv_address = findViewById(R.id.address);
        sw_location = findViewById(R.id.sw_location);

        // TextView per l'accelerometro
        tv_x = findViewById(R.id.x);
        tv_y = findViewById(R.id.y);
        tv_z = findViewById(R.id.z);
        tv_m = findViewById(R.id.m);

        // Separando il codice dei diversi setup rendo più leggibile il tutto
        setupAccelerometer();
        setupGPS();

        status.run();
        sw_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.run();
            }
        });

    }

    // Evento da implementare obbligatoriamente (implements SensorEventListener in alto)
    // Al variare dei valori del sensore aggiorno i valori della UI
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];
        m = Math.pow(x*x+y*y+z*z,0.5);
        tv_x.setText(x+"");
        tv_y.setText(y+"");
        tv_z.setText(z+"");
        tv_m.setText(m+"");
    }
    // Evento da implementare obbligatoriamente (implements SensorEventListener in alto)
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setupAccelerometer(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    };

    public void GpsUpdateUI(Location location){
        lat = location.getLatitude();
        lon = location.getLongitude();
        alt = location.getAltitude();
        address = "-";
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
            address = addresses.get(0).getAddressLine(0);
        }
        catch(Exception e){
            address = "-";
        }

        tv_lat.setText(lat+"");
        tv_lon.setText(lon+"");
        tv_alt.setText(alt+"");
        tv_address.setText(address+"");
    }

    public void setupGPS(){
        igps = new IGPS() {
            @Override
            public void updateUI(Location location) {
                if(location!=null)
                    GpsUpdateUI(location);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSION_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                }
                else{
                    if(sw_location.isChecked())
                        sw_location.toggle();
                    Toast.makeText(this,"Sono richiesti i permessi di geolocalizzazione per funzionare",Toast.LENGTH_SHORT).show();
                }
        }
    }

    Runnable status = new Runnable() {
        @Override
        public void run() {

            if(checkGpsPermission()){

                if(sw_location.isChecked()) {
                    gps = new GPS(MainActivity.this, MainActivity.this, igps);
                    gps.Location_ON();
                }
                else{
                    if(gps != null) {
                        gps.Location_OFF();
                    }
                }
            }
            else
            if(sw_location.isChecked()){
                sw_location.toggle();
            }

        }
    };

    private boolean checkGpsPermission(){
        // Controllo dei permessi

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // Abbiamo i permessi
            return true;
        }
        else{
            // NON abbiamo i permessi
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            }
        }
        return false;
    }

}