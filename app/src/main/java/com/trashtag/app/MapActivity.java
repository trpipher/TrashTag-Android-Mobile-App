package com.trashtag.app;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

//--------For Google Map API---------------
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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


    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fabConfirm;
    private FloatingActionButton fabCancel;
    private LinearLayout fabMLayout;
    private LinearLayout fab1Layout;
    private LinearLayout fab2Layout;
    private TextView fab1Word;
    private TextView fab2Word;


    private boolean fabMenu = false;
    private boolean creatingPin = false;
    private int iconID;
    private Marker lastPin;
    private String typeOfPin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
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

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fab = findViewById(R.id.fabMain);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fabCancel = findViewById(R.id.fabPinCancel);
        fabConfirm = findViewById(R.id.fabPinConfirm);
        fab1Word = findViewById(R.id.fab1Text);
        fab2Word = findViewById(R.id.fab2Text);
        fabMLayout = findViewById(R.id.fabMainLayout);
        fab1Layout = findViewById(R.id.fab1Layout);
        fab2Layout = findViewById(R.id.fab2Layout);

        closeFabConfirms();

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
                creatingPin = true;
                typeOfPin = "Trash";
                iconID = R.drawable.ic_trashicon;
                dropPinOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                showFabConfirms();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatingPin = true;
                typeOfPin = "Recycling";
                iconID = R.drawable.ic_recycle;
                dropPinOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                showFabConfirms();
            }
        });

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatingPin = false;
                if(lastPin != null) {
                    lastPin.remove();
                    lastPin = null;

                }
                closeFabConfirms();
            }
        });

        fabConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastPin!= null) {
                    creatingPin = false;
                    String type = "";
                    switch (iconID){
                        case R.drawable.ic_recycle:
                            type = "Recycle";
                            break;
                        case R.drawable.ic_trashicon:
                            type = "Trash";
                            break;

                    }
                    customMarker c = new customMarker(lastPin.getTitle(),lastPin.getSnippet(), type,lastPin.getPosition());
                    databaseReference.child("Pins").child(getLocation(lastPin.getPosition())).push().setValue(c);
                    lastPin = null;
                    closeFabConfirms();
                }
                else{
                    Toast.makeText(getBaseContext(),"Didn't Place a pin", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }

    private void showFabConfirms(){
        fabConfirm.show();
        fabCancel.show();
    }

    private void closeFabConfirms(){
        fabConfirm.hide();
        fabCancel.hide();
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

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
        //Draw a tag on the map
        Date d = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(("MM-dd-YY HH:mm"));
        LatLng latLng = new LatLng(Latitude, Longitude);
        String Title = typeOfPin;
        lastPin = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(AppResources.getBitmapfromVector(this,iconID)))
                .title(Title)
                .snippet("Created on: "+sdf.format(d)));


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
       /* if(creatingPin) {
            if(lastPin != null)
                lastPin.remove();
            dropPinOnMap(point.latitude, point.longitude);
        }

        */
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {

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
     **
     * --------------Google API.--------------------
     */

    private void loadPins(){
        LatLng l = new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
        String state = getLocation(l);
        DatabaseReference loadRef = FirebaseDatabase.getInstance().getReference("Pins/"+state);
        loadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    customMarker c = snapshot.getValue(customMarker.class);
                    c.rationalize();
                    switch (c.Type)
                    {
                        case "Recycle":
                            iconID = R.drawable.ic_recycle;
                            break;
                        case "Trash":
                            iconID = R.drawable.ic_trashicon;
                            break;
                    }
                    mMap.addMarker(new MarkerOptions().
                            position(c.retLoc())
                            .icon(BitmapDescriptorFactory.fromBitmap(AppResources.getBitmapfromVector(getBaseContext(),iconID)))
                            .title(c.Title)
                            .snippet(c.Snippet));
                }
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
            for (int i = 0; i < addresses.size();i++)
            {
                if(addresses.get(i).getAdminArea() != null)
                    return addresses.get(i).getAdminArea().replaceAll("[^a-zA-Z]","");

                Log.e("Address:",(addresses.get(i).getAdminArea() != null) ? addresses.get(i).getAdminArea(): "NONE");
            }
        }catch (Exception e)
        {
            Log.e("Error:",e.getLocalizedMessage());
        }

        return "OOPS";
    }

}

class customMarker implements Serializable {
    private LatLng loc;
    public String Title;
    public String Snippet;
    public String Type;
    public double latude;
    public double lotude;

    customMarker(String s, String x,String t, LatLng l){
        loc = l;
        latude = loc.latitude;
        lotude = loc.longitude;
        Title = s;
        Snippet = x;
        Type = t;

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
        return "Title: "+Title + " Snippet: "+Snippet +" Latude: " + latude+" Lotude: "+lotude +" "+loc.toString();
    }
}

