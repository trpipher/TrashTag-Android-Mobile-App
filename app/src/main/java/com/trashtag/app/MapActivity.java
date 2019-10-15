package com.trashtag.app;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.location.Location;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

//--------For Google Map API---------------
import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.internal.maps.zzt;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//--------For Google Map API---------------

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,OnMapClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    //Debug use only
    private String TAG="TrashTag";
    //Handle of the google map
    private GoogleMap mMap;
    private Geocoder geocoder;
    List<Address> addresses;

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
    private DatabaseReference databaseReference;


    //private FirebaseAuth mAuth;

    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private LinearLayout fabMLayout;
    private LinearLayout fab1Layout;
    private LinearLayout fab2Layout;
    private TextView fab1Word;
    private TextView fab2Word;
    private boolean fabMenu = false;



    @Override
    public void onStart(){
        super.onStart();
       // getLocationPermission();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }



        geocoder = new Geocoder(this);


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //loadPins();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fab = findViewById(R.id.fabMain);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fab1Word = findViewById(R.id.fab1Text);
        fab2Word = findViewById(R.id.fab2Text);
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
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
        fab1Word.setVisibility(View.INVISIBLE);
        fab2Word.setVisibility(View.INVISIBLE);
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
                    fab1Word.setVisibility(View.VISIBLE);
                    fab2Word.setVisibility(View.VISIBLE);
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
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Log.d(TAG, "Current location is found.");
                            mLastKnownLocation = task.getResult();
                            loadPins();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            //Drap a tag in current location
                            //dropPinOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            //Drap a tag in default location
                            //dropPinOnMap(mDefaultLocation.latitude,mDefaultLocation.longitude);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s","Didnt catch" + e.getLocalizedMessage());
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
        Date d = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(("MM-dd HH:mm:ss"));

        LatLng latLng = new LatLng(Latitude, Longitude);
        String Title = "Trash NO."+String.valueOf(TotalNum);
        customMarker c = new customMarker(Title, latLng);
        databaseReference.child("Pins").child(getLocation(latLng)).push().setValue(c);
        mMap.addMarker(new MarkerOptions().
                position(new LatLng(Latitude,Longitude))
                .title(Title));


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
    // Prompt the user for permission.
        //getLocationPermission();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        //Set the main activity as the click listener
        mMap.setOnMapClickListener(this);


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

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    /**
     *  usage:
     *      Ask user for permission state in AndroidManifest.xml.
     *  variable:
     *      NULL
     *
     *
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




    private void updateLocationUI()
    {
        if(mMap ==null)
            return;
        try{
            if(mLocationPermissionGranted){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }else{
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        }catch (SecurityException e)
        {
            Log.e("Exception: %s", e.getMessage());
        }
    }*/
    /**
     * --------------Google API.--------------------
     */

    private void loadPins(){
        LatLng l = new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
        String state = getLocation(l);
        DatabaseReference loadRef = FirebaseDatabase.getInstance().getReference("Pins/"+state);
        Log.i("Got","here");
        loadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    customMarker c = snapshot.getValue(customMarker.class);
                    c.rationalize();
                    mMap.addMarker(new MarkerOptions().
                            position(c.retLoc())
                            .title(c.Title));
                    Log.i("marker",c.toString());
                }
                Log.i("Got","here3");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @NotNull
    private String getLocation(LatLng L)
    {
        try{
            addresses =  geocoder.getFromLocation(L.latitude,L.longitude,10);
        }catch (Exception e)
        {
            Log.e("Error:",e.getLocalizedMessage());
        }
        for (int i = 0; i < addresses.size();i++)
        {
            if(addresses.get(i).getAdminArea() != null)
                return addresses.get(i).getAdminArea().replaceAll("[^a-zA-Z]","");

            Log.e("Address:",(addresses.get(i).getAdminArea() != null) ? addresses.get(i).getAdminArea(): "NONE");
        }
        return "OOPS";
    }

}

class customMarker implements Serializable {
    private LatLng loc;
    public String Title;
    public double latude;
    public double lotude;

    customMarker(String s, LatLng l){
        loc = l;
        latude = loc.latitude;
        lotude = loc.longitude;
        Title = s;

    }

    void rationalize(){
        loc = new LatLng(latude,lotude);
    }
    customMarker(){
    }

    LatLng retLoc(){
        return loc;
    }
    @NonNull
    @Override
    public String toString() {
        return "Title: "+Title + " Latude: " + latude+" Lotude: "+lotude +" "+loc.toString();
    }

    /*
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("LatLng",loc);
        result.put("Title",Title);
        return result;
    }
    */

}

