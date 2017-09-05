package com.xplocity.xplocity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import adapters.LocationToMapAdapter;
import api_classes.RouteDownloader;
import managers.MapManager;
import models.Route;

public class RouteSaveActivity
        extends AppCompatActivity
        implements OnMapReadyCallback {

    private MapManager mMapManager;
    private Route mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_save);

        mRoute = getIntent().getParcelableExtra("route");
        initMap();
    }

    private void initMap() {
        //Create the google map

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setInfoWindowAdapter(new LocationToMapAdapter(this.getBaseContext()));
        mMapManager = new MapManager(googleMap);

        mMapManager.setRoute(mRoute);
    }
}