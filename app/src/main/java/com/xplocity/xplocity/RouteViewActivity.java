package com.xplocity.xplocity;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import adapters.LocationToMapAdapter;
import api_classes.RouteDownloader;
import api_classes.interfaces.RouteDownloaderInterface;
import models.Location;
import models.Route;

public class RouteViewActivity extends FragmentActivity implements OnMapReadyCallback, RouteDownloaderInterface {

    private GoogleMap mMap;

    private int route_id;
    private Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle recdData = getIntent().getExtras();
        route_id =  recdData.getInt("route_id");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new LocationToMapAdapter(this.getBaseContext()));

        RouteDownloader route_downloader = new RouteDownloader(this);
        route_downloader.download_route(route_id);
    }

    @Override
    public void onRouteDownload(Route p_route) {
        route = p_route;
        draw_route();
    }


    //TODO: перенести в MapManager
    private void draw_route() {
        // add location markers to map
        for (Location loc : route.locations) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(loc.position)
                    .title(loc.name)
                    .snippet("Address: " + loc.address + System.getProperty("line.separator") + "Description: " +loc.description));
            if (loc.explored)
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            m.setTag(loc);
        }

        if (!route.locations.isEmpty()) {
            Location loc = route.locations.get(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc.position, 10f));
        }

        if (!route.path.isEmpty()) {
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(route.path)
                    .width(5)
                    .color(Color.RED));

        }
    }




}
