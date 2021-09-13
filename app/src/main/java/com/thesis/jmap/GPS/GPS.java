package com.thesis.jmap.GPS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class GPS {

    LocationCallback locationCallBack;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    public long time = 100;    // Senza risparmio energetico
    private double lat=0, lon=0, alt=0;
    private Context c;
    private Activity a;
    private IGPS iGps;

    public GPS(Context context, Activity activity, IGPS iGps){
        this.c = context;
        this.a = activity;
        this.iGps = iGps;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(time);
        locationRequest.setFastestInterval(time);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                iGps.updateUI(locationResult.getLastLocation());
            }
        };

    }

    public void Location_OFF() {
        lat = lon = alt = 0;
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("MissingPermission")
    public void Location_ON() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(a);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(a, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    iGps.updateUI(location);
                }
            }
        });


    }
}