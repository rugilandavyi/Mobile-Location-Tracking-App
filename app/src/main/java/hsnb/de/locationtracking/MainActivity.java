package hsnb.de.locationtracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 1;
    public static final int FAST_UPDATE_INTERVAL = 1;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    //private static final int PERMISSIONS_COARSE_LOCATION = 99;

    //reference to UI elements

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_waypointCount;

    Button btn_newWaypoint, btn_WaypointList, btn_showMap;

    Switch sw_locationupdates, sw_gps;

    //Variable to remember we are tracking location or not
    boolean updateOn = false;

    //current location
    Location currentLocation;

    //list of saved locations
    List<Location> savedLocations;

    //Import location request class, a config file for settings of FusedLocationProviderClient
    LocationRequest locationRequest;

    LocationCallback locationCallBack;


    //Google API for Location services. Most of this App functions use this class
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //give each UI variable a value

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        btn_newWaypoint = findViewById(R.id.btn_newWaypoint);
        btn_WaypointList = findViewById(R.id.btn_showWaypointList);
        tv_waypointCount = findViewById(R.id.tv_countOfPins);
        btn_showMap = findViewById(R.id.btn_showMap);

        //Set properties of LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(500 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        //this is triggered when update interval is met
        locationCallBack = new LocationCallback() {


            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(locationResult.getLastLocation());
            }


        };
        //locationRequest.setMaxWaitTime(100);

        btn_newWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get GPS location

                //Add new location to global list
                MyApplication myApplication = (MyApplication)getApplicationContext();
                savedLocations = myApplication.getMyLocation();
                savedLocations.add(currentLocation);





            }
        });

        btn_WaypointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationsList.class);
                startActivity(i);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    //use GPS, most accurate
                    locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS");

                } else {
                    locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Cellular Network and/or Wi-Fi");


                }
            }
        });

        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()) {
                    //turn on location tracking
                    startLocationUpdates();
                } else {
                    //turn off location tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();

    }      //end OnCreate method


    private void stopLocationUpdates() {
        tv_updates.setText("Location is NOT being tracked");
        tv_lat.setText("Not tracking");
        tv_lon.setText("Not tracking");
        tv_address.setText("Not tracking");
        tv_accuracy.setText("Not tracking");
        tv_speed.setText("Not tracking");
        tv_altitude.setText("Not tracking");
        tv_sensor.setText("Not tracking");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);


    }

    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked ");

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);

        updateGPS(); //fetches and shows location data

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "This App requires permissions to be granted to work properly", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

        }
    }

    private void updateGPS() {
        //get permission from user to track GPS
        //get the current location from fused client
        //update the UI i.e set all properties in associated textview items

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //User provided permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {


                @Override
                public void onSuccess(Location location) {
                    //Permission granted. Put values of location into UI components
                    updateUIValues(location);
                    currentLocation = location; //currentLocation saved to further ot variable

                }
            });
        } else {
            //Permission denied/not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);

        }
    }

    private void updateUIValues(Location location) {
        //update all text view objects with new location details
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));


        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else {
            tv_altitude.setText("Not Available");
        }
        if (location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getSpeed()));
        } else {
            tv_speed.setText("Not Available");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e ){
            tv_address.setText("Not Available");
        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocation();

        //Show number of waypoints saved
        tv_waypointCount.setText(Integer.toString(savedLocations.size()));



    }}