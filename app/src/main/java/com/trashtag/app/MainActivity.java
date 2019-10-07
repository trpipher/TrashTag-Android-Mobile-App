package com.trashtag.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



//--------For Google Map API---------------

public class MainActivity extends AppCompatActivity
        implements
        OnMapReadyCallback {

    TextView textBox;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    /**
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


}
