package com.example.linked;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends FragmentActivity implements LocationListener, View.OnClickListener {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location mCurrentLocation;
    private Button startButton,stopButton;
    private int locationRequestCode = 1000;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    TextView txtLocation;
    public static final String URL_POST="";
    public boolean mTracking = false;
    public BackgroundLocationService gpsService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setWidgetIds();

        //prepare service
        final Intent intent = new Intent(this.getApplication(), BackgroundLocationService.class);
        this.getApplication().startService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);



    }

    private void setWidgetIds() {
        startButton = (Button) findViewById(R.id.btn_start_tracking);
        stopButton = (Button) findViewById(R.id.btn_stop_tracking);
        txtLocation = (TextView) findViewById(R.id.txtLocation);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_tracking:
                startTracking();
                break;
            case R.id.btn_stop_tracking:
                stopTracking();
                break;
        }
    }


    public void startTracking() {
            //check for permission
            if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                gpsService.startTracking();
                mTracking = true;

                txtLocation.setText(String.format(Locale.US, "%s -- %s", wayLatitude, wayLongitude));
                toggleButtons();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, locationRequestCode);
            }
        }
    public void stopTracking() {
        mTracking = false;
        gpsService.stopTracking();
        toggleButtons();
    }
    private void toggleButtons() {
        startButton.setEnabled(!mTracking);
        stopButton.setEnabled(mTracking);
        txtLocation.setText((mTracking) ? "TRACKING" : "GPS Ready");
    }
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundLocationService")) {
                gpsService = ((BackgroundLocationService.LocationServiceBinder) service).getService();
                startButton.setEnabled(true);
                txtLocation.setText("GPS Ready");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (componentName.getClassName().equals("BackgroundLocationService")) {
                gpsService = null;
            }

        }

        @Override
        public void onBindingDied(ComponentName name) {

        }

        @Override
        public void onNullBinding(ComponentName name) {

        }
    };


        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


            if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                gpsService.startTracking();
                mTracking = true;

                txtLocation.setText(String.format(Locale.US, "%s -- %s", wayLatitude, wayLongitude));
                toggleButtons();
            }



    }

    @Override
    public void onLocationChanged(Location location) {
        wayLatitude = location.getLatitude();
        wayLongitude = location.getLongitude();

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        txtLocation.setText(msg);
    }


}
