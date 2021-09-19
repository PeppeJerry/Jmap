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
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.thesis.jmap.GPS.GPS;
import com.thesis.jmap.GPS.IGPS;
import com.thesis.jmap.localdb.Dot;
import com.thesis.jmap.localdb.Settings;
import com.thesis.jmap.localdb.databasedots;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Dichiarazione delle variabili e costanti

    // 1. Localizzazione
    private static final int PERMISSION_LOCATION = 69;
    IGPS igps;
    TextView tv_address, tv_lat, tv_lon, tv_alt;
    Switch sw_location;
    Geocoder geocoder;
    GPS gps;
    double lat,lon,alt;
    String address;

    // 2. Sensore - Accelerometro
    SensorManager sensorManager;
    Sensor accelerometer;
    TextView tv_x,tv_y,tv_z,tv_m;
    double x,y,z,m;

    // 3. Database - Room
    databasedots database;

    // 4. Variabili globali varie
    Handler h = new Handler();

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
        //database.dotDao().nuke();
        //for(int i=0;i<3;i++) database.dotDao().forceAddDot(new Dot(0,0,0,0,0,0,null,0,null));
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
        eseguimi.start();
    }// Fine onCreate

    private void setupDatabase() {
        database = Room.databaseBuilder(this, databasedots.class, "Jmap")
                .allowMainThreadQueries()
                .build();
        if(database.SettingsDao().check("Location") == 0)
            database.SettingsDao().setting(new Settings("Location",false));
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
        lat = location.getLatitude();
        lon = location.getLongitude();
        alt = location.getAltitude();
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
                database.dotDao().addDot(new Dot(x, y, z, lat, lon, alt, null, 0, null));
                if (database.dotDao().num_rows()%300 == 0)
                    Toast.makeText(MainActivity.this, database.dotDao().num_rows()+"", Toast.LENGTH_SHORT).show();
            }
            h.postDelayed(updateLocalDB,50);
        }
    };

    Thread eseguimi = new Thread(new Runnable(){
        @Override
        public void run() {
            HttpURLConnection con = null;
            try {
                Gson gson = new Gson();
                JSONObject data = new JSONObject();
                List<Dot> dots = database.dotDao().all();

                JSONArray j = new JSONArray(gson.toJson(dots));
                String uuid = j.getJSONObject(0).getString("uuid");
                String model = j.getJSONObject(0).getString("model");
                String a = gson.toJson(dots);

                a = a.replace("\"model\":\""+model+"\",", "");
                a = a.replace("\"uuid\":\""+uuid+"\",", "");
                a = a.replace("\"model\":\""+model+"\"", "");
                a = a.replace("\"uuid\":\""+uuid+"\"", "");

                j = new JSONArray(a);
                data.put("uuid", uuid);
                data.put("model", model);
                data.put("dots", j);

                URL url = new URL("https://jmap.altervista.org/index.php");
                con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.connect();

                DataOutputStream localDataOutputStream = new DataOutputStream(con.getOutputStream());
                localDataOutputStream.writeBytes(data.toString());
                localDataOutputStream.flush();
                localDataOutputStream.close();

                if(con.getResponseCode()!=200)
                    return;

                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String line;
                String r = "";
                while ((line = rd.readLine()) != null) {
                    r+=line+"\n";
                }
                Log.i("data", r);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
    });

}