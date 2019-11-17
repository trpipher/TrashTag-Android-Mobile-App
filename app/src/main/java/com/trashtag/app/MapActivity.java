package com.trashtag.app;

import android.Manifest;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//--------For Google Map API---------------

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,OnMapClickListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
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




    // The geographical location where the device is currently located.
    // That is the last-knownlocation retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private DatabaseReference databaseReference;
    private ArrayList<DataSnapshot> sever_pin_list=new ArrayList<DataSnapshot>();




    private FloatingActionButton fab_add;
    private FloatingActionButton fab_del;
    private FloatingActionButton fabConfirm;
    private FloatingActionButton fabCancel;
    private LinearLayout fab_add_Layout;
    private LinearLayout fab_del_Layout;
    private TextView fab_del_Word;
    private boolean fab_del_show = false;



    private boolean create_Pin = false;
    private boolean delete_Pin = false;
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

        fab_add = findViewById(R.id.fab_add);
        fab_add_Layout = findViewById(R.id.fab_add_Layout);

        fab_del = findViewById(R.id.fab_del);
        fab_del_Word = findViewById(R.id.fab_del_Text);
        fab_del_Layout = findViewById(R.id.fab_del_Layout);
        fabCancel = findViewById(R.id.fabPinCancel);
        fabConfirm = findViewById(R.id.fabPinConfirm);

        closeFabConfirms();

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_Pin = true;
                typeOfPin = "Trash";
                iconID = R.drawable.ic_recycle;
                dropPinOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude(),true,null,null);
                showFabConfirms();

            }
        });

        fab_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_Pin = true;
                showFabConfirms();
            }
        });


        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(create_Pin ==true)
                {
                    create_Pin = false;
                    if(lastPin != null) {
                        lastPin.remove();
                        lastPin = null;

                    }
                }
                else if(delete_Pin == true)
                {
                    delete_Pin = false;
                    if(lastPin != null) {
                        lastPin = null;
                    }
                }

                closeFabConfirms();
            }
        });

        fabConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastPin!= null) {
                    LatLng l = new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                    String state = getLocation(l);
                    DatabaseReference loadRef = FirebaseDatabase.getInstance().getReference("Pins/"+state);
                    if (create_Pin)
                    {
                        DataSnapshot snapshot;
                        create_Pin = false;

                        customMarker c = new customMarker(lastPin.getTitle(),
                                lastPin.getSnippet(), "Trash",lastPin.getPosition());
                        databaseReference.child("Pins").child(getLocation(lastPin.getPosition()))
                                .push().setValue(c);

                    }
                    else if(delete_Pin)
                    {
                        delete_Pin=false;
                        delete_pin(lastPin);
                        lastPin.remove();
                        closeFabDel();
                    }
                    lastPin = null;
                    closeFabConfirms();
                }
                else{
                    Toast.makeText(getBaseContext(),"Didn't Place a pin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.pro);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapActivity.this, Profile.class));
            }
        });
        FloatingActionButton faba = (FloatingActionButton) findViewById(R.id.home);
        faba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapActivity.this, MainMenu.class));
            }
        });
        FloatingActionButton fabb = (FloatingActionButton) findViewById(R.id.reward);
        fabb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapActivity.this, Profile.class));
            }
        });




    }
    //Delete a pin from server & local
    private void delete_pin(Marker marker)
    {
        if(sever_pin_list.isEmpty())
            return;
       int size=sever_pin_list.size();
       DataSnapshot snapshot;
       String key="";
       for(int i=0;i<size;i++)
       {
           snapshot=sever_pin_list.get(i);
           customMarker c = snapshot.getValue(customMarker.class);
           c.rationalize();
           if( (c.latude==marker.getPosition().latitude)&&(c.lotude==marker.getPosition().longitude))
           {
               // Remove from sever
               key=snapshot.getKey();
               databaseReference.child("Pins").child(getLocation(lastPin.getPosition()))
                       .child(key).removeValue();
               // remove from local list
               sever_pin_list.remove(i);
               // Finish delete
               return;
           }



       }
    }
    private void showFabConfirms(){
        fabConfirm.show();
        fabCancel.show();
    }

    private void closeFabConfirms(){
        fabConfirm.hide();
        fabCancel.hide();
    }

    private void showFabDel(){
        if(!fab_del_show) {
            fab_add.setClickable(false);
            fab_del_show = true;
            Log.i("RAN","showFabDel");
            fab_del_Layout.setVisibility(View.VISIBLE);
            fab_del_Word.setVisibility(View.VISIBLE);
        }

    }

    private void closeFabDel(){
        if(fab_del_show) {
            fab_add.setClickable(true);
            fab_del_show = false;
            Log.i("RAN", "closeFabDel");
            fab_del_Word.setVisibility(View.INVISIBLE);
            fab_del_Layout.setVisibility(View.GONE);
        }
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
     *      NewPin   : boolean type, whether this is new pin or load pin from database
     *      Title    : String type, only when NewPin==False needed.
     *      Snippet  : String type, only when NewPin==False needed.
     *
     **/
    private void dropPinOnMap(double Latitude,double Longitude,boolean NewPin,String Title,String Snippet) {
        //Draw a tag on the map
        String snippet;
        LatLng latLng;
        String title;

        if ( NewPin == true)
        {
            //This is a new pin
            Date d = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat(("MM-dd-YY HH:mm"));
            latLng = new LatLng(Latitude, Longitude);
            title = typeOfPin;
            snippet="Created on: "+sdf.format(d);
        }
        else
        {
            //This is old pin,load from database
            latLng = new LatLng(Latitude, Longitude);
            title = Title;
            snippet=Snippet;
        }
        //Set Custom InfoWindow Adapter
        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(MapActivity.this);
        mMap.setInfoWindowAdapter(adapter);
        lastPin = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(AppResources.getBitmapfromVector(this,iconID)))
                .title(title)
                .snippet(snippet));


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
        mMap.setOnMarkerClickListener(this);

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
        if(create_Pin) {
            if(lastPin != null)
                lastPin.remove();
            else
            {
                lastPin = null;
                closeFabDel();
            }
            dropPinOnMap(point.latitude, point.longitude,true,null,null);
        }
        else
        {
            lastPin = null;
            closeFabDel();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        marker.showInfoWindow();
        showFabDel();
        lastPin=marker;
        return false;
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
                    sever_pin_list.add(snapshot);
                    customMarker c = snapshot.getValue(customMarker.class);
                    c.rationalize();

                    iconID = R.drawable.ic_recycle;
                    dropPinOnMap(c.retLoc().latitude,c.retLoc().longitude,false,c.Title,c.Snippet);
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

