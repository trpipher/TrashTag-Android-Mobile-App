package com.trashtag.app;

<<<<<<< HEAD
import android.Manifest;
=======
>>>>>>> c6e80d546e6b743b593c1ebe8cd47baabe98d962
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
<<<<<<< HEAD
=======
import android.location.Location;
>>>>>>> c6e80d546e6b743b593c1ebe8cd47baabe98d962
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
<<<<<<< HEAD



//--------For Google Map API---------------

public class MainActivity extends AppCompatActivity
        implements
        OnMapReadyCallback {
=======
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//--------For Google Map API---------------

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,OnMapClickListener{
    //Debug use only
    private String TAG="TrashTag";
    //Handle of the google map
    private GoogleMap mMap;
>>>>>>> c6e80d546e6b743b593c1ebe8cd47baabe98d962

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_main);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    /**
<<<<<<< HEAD
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {

        /*
         - Call the function to load Hattiesburg

         TODO:
         1. Ask for User's current location
         2. Navigate to exact Lat, Lon
         3. Zoom in closer to User to seem more localized

         */
        GoogleMap gMap = googleMap;
        OnMapLoaded(gMap);

    }


    public void OnMapLoaded(GoogleMap gMap) {

        /*
         - Setting the latitude and longitude of Hattiesburg
         - Adds a marker to Hattiesburg (optional)
         - Move the camera to the Lat, Lon and zooms in closer (zoom: 13) Lower zoom = farther away, Higher zoom: closer
         */

        LatLng hattiesburg = new LatLng(31.3271, -89.2903);
        //gMap.addMarker(new MarkerOptions().position(hattiesburg).title("Marker in Hattiesburg"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hattiesburg, 13));
    }

=======
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
        mMap = googleMap;;
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
>>>>>>> c6e80d546e6b743b593c1ebe8cd47baabe98d962

}

