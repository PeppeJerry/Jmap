package com.thesis.jmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;
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
import android.os.Handler;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thesis.jmap.GPS.GPS;
import com.thesis.jmap.GPS.IGPS;
import com.thesis.jmap.localdb.Dot;
import com.thesis.jmap.localdb.Settings;
import com.thesis.jmap.localdb.databasedots;
import com.thesis.jmap.remotedb.sendData;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Dichiarazione delle variabili e costanti

    // 1. Localizzazione
    private static final int PERMISSION_LOCATION = 69;
    private static final int DECIMAL_GPS_PRECISION = 6;
    IGPS igps;
    TextView tv_address, tv_lat, tv_lon, tv_alt;
    Switch sw_location;
    Geocoder geocoder;
    GPS gps;
    double lat,lon,alt;
    String address;

    // 2. Sensore - Accelerometro
    private static final int DECIMAL_ACCELEROMETER_PRECISION = 9;
    SensorManager sensorManager;
    Sensor accelerometer;
    TextView tv_x,tv_y,tv_z,tv_m;
    double x,y,z,m;

    // 3. Database - Room
    databasedots database;

    // 4. Variabili globali varie
    Handler h = new Handler();
    sendData syncData;

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
        setupDatabase();
        setupAccelerometer();
        setupGPS();

        syncData = new sendData(database);

        // Inizializzo il nuovo thread per la sincronizzazione dei dati
        Thread ThreadSyncData = new Thread(syncData);
        sw_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swLocationCheck();
                if(sw_location.isChecked()){
                    swLocationCheck();
                }
            }
        });
        updateLocalDB.run();
        ThreadSyncData.start();
    }// Fine onCreate

    private void setupDatabase() {
        database = Room.databaseBuilder(MainActivity.this, databasedots.class, "Jmap")
                .allowMainThreadQueries()
                .build();
        if(database.SettingsDao().check("Location") == 0)
            database.SettingsDao().setting(new Settings("Location",false));
    }

    // Evento da implementare obbligatoriamente (implements SensorEventListener in alto)
    // Al variare dei valori del sensore aggiorno i valori della UI
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        x = round(sensorEvent.values[0],DECIMAL_ACCELEROMETER_PRECISION);
        y = round(sensorEvent.values[1],DECIMAL_ACCELEROMETER_PRECISION);
        z = round(sensorEvent.values[2],DECIMAL_ACCELEROMETER_PRECISION);
        m = round(Math.pow(x*x+y*y+z*z,0.5),DECIMAL_ACCELEROMETER_PRECISION);

        tv_x.setText(x+"");
        tv_y.setText(y+"");
        tv_z.setText(z+"");
        tv_m.setText(m+"");
    }
    // Evento da implementare obbligatoriamente (implements SensorEventListener in alto)
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void setupAccelerometer(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    };

    public void GpsUpdateUI(Location location){
        if(!sw_location.isChecked()){
            gps.Location_OFF();
            return;
        }
        lat = round(location.getLatitude(),DECIMAL_GPS_PRECISION);
        lon = round(location.getLongitude(),DECIMAL_GPS_PRECISION);
        alt = round(location.getAltitude(),DECIMAL_GPS_PRECISION);
        address = "-";
        geocoder = new Geocoder(this);
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
        if(database.SettingsDao().status("Location").state){
            sw_location.toggle();
            swLocationCheck();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSION_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    database.SettingsDao().setting(new Settings("Location",true));
                }
                else{
                    database.SettingsDao().setting(new Settings("Location",false));
                    if(sw_location.isChecked())
                        sw_location.toggle();
                    Toast.makeText(this,"Sono richiesti i permessi di geolocalizzazione per funzionare",Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void swLocationCheck(){
        if(checkGpsPermission()){

            if(sw_location.isChecked()) {
                gps = new GPS(MainActivity.this, MainActivity.this, igps);
                Dot.setupActiveUuid();
                gps.Location_ON();
                database.SettingsDao().setting(new Settings("Location",true));
            }
            else{
                if(gps != null) {
                    gps.Location_OFF();
                    tv_lat.setText("-");
                    tv_lon.setText("-");
                    tv_alt.setText("-");
                    tv_address.setText("-");
                    database.SettingsDao().setting(new Settings("Location",false));
                }
            }
        }
        else
        if(sw_location.isChecked()){
            sw_location.toggle();
        }
    }

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

    Runnable updateLocalDB = new Runnable() {
        @Override
        public void run() {
            if (sw_location.isChecked()) {
                database.dotDao().addDot(new Dot(x, y, z, lat, lon, alt, null, 0));
                if (database.dotDao().num_rows()%300 == 0)
                    Toast.makeText(MainActivity.this, database.dotDao().num_rows()+"", Toast.LENGTH_SHORT).show();
            }
            h.postDelayed(updateLocalDB,50);
        }
    };

    public static double round(double value, int places) {
        if(places <0)
            return value;
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
    /*Runnable test = new Runnable() {
        @Override
        public void run() {
            for(int i=0;i<5;i++) database.dotDao().forceAddDot(new Dot(0,0,0,0,0,0,null,0));
            Dot.setupActiveUuid();
            if(i%100==0)
                Toast.makeText(MainActivity.this, i+"", Toast.LENGTH_SHORT).show();
            i++;
            h.postDelayed(test,50);
        }
    };*/