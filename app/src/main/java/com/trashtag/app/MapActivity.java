package com.trashtag.app;

import android.animation.Animator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.location.Location;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

//--------For Google Map API---------------
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//--------For Google Map API---------------

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,OnMapClickListener{
    //Debug use only
    private String TAG="TrashTag";
    //Handle of the google map
    private GoogleMap mMap;

    // A default location (Sydney, Australia) and default zoom to use
    // when location permission isn't granted.
    private final LatLng mDefaultLocation = new LatLng(31.329749, -81.334187);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    //Flag of whether permission is granted by user
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located.
    // That is the last-knownlocation retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    //Total Tag number on  the map
    private int TotalNum=0;

    //private FirebaseAuth mAuth;

    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private LinearLayout fabMLayout;
    private LinearLayout fab1Layout;
    private LinearLayout fab2Layout;
    private boolean fabMenu = false;

    @Override
    public void onStart(){
        super.onStart();
       // FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve location and camera position from saved instance state.

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.fabMain);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fabMLayout = findViewById(R.id.fabMainLayout);
        fab1Layout = findViewById(R.id.fab1Layout);
        fab2Layout = findViewById(R.id.fab2Layout);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fabMenu)
                {
                    fabMenu = true;
                    showFabMenu();
                }
                else{
                    fabMenu = false;
                    closeFabMenu();
                }

            }
        });

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showFabMenu(){
        Log.i("RAN","showFabMenu");
        fab1Layout.setVisibility(View.VISIBLE);
        fab2Layout.setVisibility(View.VISIBLE);
        fab1Layout.animate().translationY(-getResources().getDimension(R.dimen.fab1_translate));
        fab2Layout.animate().translationY(-getResources().getDimension(R.dimen.fab2_translate));
    }
    private void closeFabMenu(){
        Log.i("RAN","closeFabMenu");
        fab1Layout.animate().translationY(0);
        fab2Layout.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!fabMenu)
                {
                    fab1Layout.setVisibility(View.GONE);
                    fab2Layout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     *  usage:
     *      When our app lost focus,this app will be callback to save the map/tag info.
     *
     **/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }
    /**
     *  usage:
     *      Get user's current location. Call at onMapReady function
     *
     **/
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            //Drap a tag in current location
                            dropPinOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            //Drap a tag in default location
                            dropPinOnMap(mDefaultLocation.latitude,mDefaultLocation.longitude);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    /**
     *  usage:
     *      Customer function. Use this function to draw a tag at a given location
     *  variable:
     *      Latitude: double type. Latitude of the location.
     *      Longitude: double type. Longitude of the location.
     *
     **/
    private void dropPinOnMap(double Latitude,double Longitude) {
        //Trash tag number ++
        TotalNum++;
        //Draw a tag on the map
        mMap.addMarker(new MarkerOptions().
                position(new LatLng(Latitude,Longitude))
                .title("Trash NO."+String.valueOf(TotalNum)));
    }

     // --------------Google API.--------------------
    /**
     *  usage:
     *      When the google map is ready to be used, This function will be callback.
     *  variable:
     *      googleMap: It's GoogleMap type. It a handle to the google map
     *
     **/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Set the main activity as the click listener
        mMap.setOnMapClickListener(this);
        // Prompt the user for permission.
        getLocationPermission();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }
    /**
     *  usage:
     *      When user click the map. This function will be callback by google map.
     *  variable:
     *      point: It's LatLng type. Contain the location info of where user clicked.
     *
     **/
    @Override
    public void onMapClick(LatLng point) {
        //Just drop a tag where the user clicked
        dropPinOnMap(point.latitude,point.longitude);
    }
    /**
     *  usage:
     *      Ask user for permission state in AndroidManifest.xml.
     *  variable:
     *      NULL
     *
     **/
    private void getLocationPermission(){
        //Request location permission, so that we can get the location of the
        //device. The result of the permission request is handled by a callback,
        //onRequestPermissionsResult.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    /**
     *  usage:
     *      If user respond to the permission request, this function will be callback
     *
     **/
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

    }
    /**
     * --------------Google API.--------------------
     */

}

