package com.example.linked;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class BackgroundLocationService extends Service {
    String date;
    //  private final Binder binder = new Binder();
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private static final String TAG = "BackgroundLocationServi";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager notificationManager;
    public static final String CHANNEL_ID = "channel";
    private  FusedLocationProviderClient mFusedLocationClient;
    private LocationRepository locationRepository;
    private final int LOCATION_INTERVAL = 1000*60*10;
    private final int LOCATION_DISTANCE = 1;
    private final String URL_POST=	"";
    DateFormat df;
    SimpleDateFormat simpleDateFormat;
    private LocationRequest mLocationRequest;
    LocationCallback locationCallback=new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            // do work here
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    public BackgroundLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        Log.i(TAG, "onCreate");
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);


                //locationRepository = null;
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listeners, ignore", ex);
            }
        }
    }



    public void onLocationChanged(Location location) {

        Log.i(TAG, "LocationChanged: " + location);
        date=df.format(location.getTime());
        Toast.makeText(BackgroundLocationService.this, "LAT: " + location.getLatitude() + "\n LONG: " + location.getLongitude() + "\n" + date, Toast.LENGTH_SHORT).show();
        InsertSV(location,date);

    }
    private void InsertSV(final Location location, final String date){
       StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_POST, new Response.Listener<String>() {
           @Override
           public void onResponse(String response) {
               Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
           }
       }, new Response.ErrorListener() {
           @Override
           public void onErrorResponse(VolleyError error) {
               Toast.makeText(getApplicationContext(),error+"",Toast.LENGTH_SHORT).show();
           }
       }){

           @Override
           protected Map<String, String> getParams()
           {
               Map<String, String> params = new HashMap<String, String>();
               params.put("Latitude", String.valueOf(location.getLatitude()));
               params.put("Longitude", String.valueOf(location.getLongitude()));
               params.put("Time",date);
              // params.put("Username","Success..ed?");

               return params;
           }
           @Override
           public Map<String, String> getHeaders() throws AuthFailureError {
               Map<String,String> params = new HashMap<String, String>();
               params.put("Content-Type","application/x-www-form-urlencoded");
               return params;
           }
       };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startTracking() {
        Log.e("This is 1", "Lookie here");
        startForeground(12345678, getNotification());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

       mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback,
                Looper.myLooper());


    }
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }
    public void stopTracking() {
        stopLocationUpdates();
        stopForeground(true);
        this.onDestroy();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {

        NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);

        return builder.build();
    }
    public class LocationServiceBinder extends Binder {
        public BackgroundLocationService getService() {
            return BackgroundLocationService.this;
        }
    }
}

