package com.trashtag.app;

import android.Manifest;
import android.animation.Animator;
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
import com.google.firebase.auth.FirebaseAuth;
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
    private ArrayList<DataSnapshot> server_pin_list=new ArrayList<DataSnapshot>();




    private FloatingActionButton fab_NewOrDel;
    private FloatingActionButton fab_trash;
	private FloatingActionButton fab_recyclable;
    private FloatingActionButton fabConfirm;
    private FloatingActionButton fabCancel;
    private LinearLayout fab_trash_Layout;
    private LinearLayout fab_recyclable_Layout;
    private TextView fab_NewOrDel_Word;
    private TextView fab_trash_Word;
    private TextView fab_recyclable_Word;

	private boolean showFabMenu = false;
    private boolean isDelFab = false;
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

        fab_NewOrDel = findViewById(R.id.fab_NewOrDel);
        fab_NewOrDel_Word = findViewById(R.id.fab_NewOrDel_Text);

        fab_trash = findViewById(R.id.fab_trash);
        fab_trash_Word = findViewById(R.id.fab_trash_Text);
        fab_trash_Layout = findViewById(R.id.fab_trash_Layout);
        fabCancel = findViewById(R.id.fabPinCancel);
        fabConfirm = findViewById(R.id.fabPinConfirm);
		fab_recyclable = findViewById(R.id.fab_recyclable);
		fab_recyclable_Word = findViewById(R.id.fab_recyclable_Text);
		fab_recyclable_Layout = findViewById(R.id.fab_recyclable_Layout);

        closeFabConfirms();

        fab_NewOrDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDelFab==true)
                {
					delete_Pin = true;
                	showFabConfirms();
                }
                else
                {
                    if(!showFabMenu)
                        showFabMenu();
                    else
                        closeFabMenu();
                }

            }
        });

        fab_trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_Pin = true;
                typeOfPin = "Trash";
                iconID = R.drawable.ic_trashicon;
                dropPinOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude(),true,null,null);
                showFabConfirms();

            }
        });

        fab_recyclable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_Pin = true;
                typeOfPin = "Recycling";
                iconID = R.drawable.ic_recycle;
                dropPinOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude(),true,null,null);
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
                    switchMainButton(false);
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
						String type = "";
						switch (iconID){
                        case R.drawable.ic_recycle:
                            type = "Recycle";
                            break;
                        case R.drawable.ic_trashicon:
                            type = "Trash";
                            break;

                    	}
                        customMarker c = new customMarker(lastPin.getTitle(),
                                lastPin.getSnippet(), type,lastPin.getPosition());
                        databaseReference.child("Pins").child(getLocation(lastPin.getPosition()))
                                .push().setValue(c);
                        User.user.updatePinScore(typeOfPin);

                    }
                    else if(delete_Pin)
                    {
                        delete_Pin=false;
                        delete_pin(lastPin);
                        User.user.updatePinScore("Pickup");
                        switchMainButton(false);
                        lastPin.remove();

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
                startActivity(new Intent(MapActivity.this, Leaderboard.class));
            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();
        getDeviceLocation();
    }

    //Delete a pin from server & local
    private void delete_pin(Marker marker)
    {
        if(server_pin_list.isEmpty())
            return;
        String key="";
        for(DataSnapshot snapshot : server_pin_list){
           customMarker c = snapshot.getValue(customMarker.class);
           c.rationalize();
           if( (c.latude==marker.getPosition().latitude)&&(c.lotude==marker.getPosition().longitude))
           {
               // Remove from sever
               key=snapshot.getKey();
               databaseReference.child("Pins").child(getLocation(lastPin.getPosition())).child(key).removeValue();
               // remove from local list
               server_pin_list.remove(snapshot);
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
    private void switchMainButton(boolean isSwitch2Del)
    {
        if (isSwitch2Del== true)
        {
            isDelFab=true;
            closeFabMenu();
            fab_NewOrDel_Word.setText(R.string.fab_del_Name);
            fab_NewOrDel.setBackgroundTintList(getResources().getColorStateList(R.color.red));
            fab_NewOrDel.setImageResource(R.drawable.ic_delete);

        }
        else
        {
            isDelFab=false;
            fab_NewOrDel_Word.setText(R.string.fab_new_Name);
            fab_NewOrDel.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            fab_NewOrDel.setImageResource(R.drawable.ic_new);
        }
    }
    private void showFabMenu(){
        if(showFabMenu==false)
        {
            showFabMenu=true;
            Log.i("RAN","showFabMenu");
            fab_trash_Layout.setVisibility(View.VISIBLE);
            fab_recyclable_Layout.setVisibility(View.VISIBLE);
            fab_trash_Layout.animate().translationY(-getResources().getDimension(R.dimen.fab1_translate));
            fab_recyclable_Layout.animate().translationY(-getResources().getDimension(R.dimen.fab2_translate));
        }

    }

    private void closeFabMenu(){
        if(showFabMenu==true)
        {
            showFabMenu=false;
            Log.i("RAN","closeFabMenu");
            fab_trash_Word.setVisibility(View.INVISIBLE);
            fab_recyclable_Word.setVisibility(View.INVISIBLE);
            fab_trash_Layout.animate().translationY(0);
            fab_recyclable_Layout.animate().translationY(0).setListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(!showFabMenu)
                    {
                        fab_trash_Layout.setVisibility(View.GONE);
                        fab_recyclable_Layout.setVisibility(View.GONE);
                        fab_trash_Word.setVisibility(View.VISIBLE);
                        fab_recyclable_Word.setVisibility(View.VISIBLE);
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
                switchMainButton(false);
            }
            dropPinOnMap(point.latitude, point.longitude,true,null,null);
        }
        else
        {
            lastPin = null;
            switchMainButton(false);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // If add a new pin, then don't show info window
        if (create_Pin == true)
            return true;
        marker.showInfoWindow();
        switchMainButton(true);
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
                    server_pin_list.add(snapshot);
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

