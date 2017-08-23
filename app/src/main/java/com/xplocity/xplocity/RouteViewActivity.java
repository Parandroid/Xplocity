package com.xplocity.xplocity;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import adapters.LocationToMapAdapter;
import api_classes.RouteDownloader;
import api_classes.interfaces.RouteDownloaderInterface;
import managers.MapManager;
import models.Route;

public class RouteViewActivity extends FragmentActivity implements OnMapReadyCallback, RouteDownloaderInterface {

    private MapManager mMapManager = null;
    private int mRouteId;
    private Route mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle recdData = getIntent().getExtras();
        mRouteId =  recdData.getInt(getString(R.string.route_id_key));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setInfoWindowAdapter(new LocationToMapAdapter(this.getBaseContext()));
        mMapManager = new MapManager(googleMap);

        RouteDownloader loader = new RouteDownloader(this);
        loader.downloadRoute(mRouteId);
    }

    @Override
    public void onRouteDownloaded(Route route) {
        mRoute = route;
        if( mMapManager != null ) {
            mMapManager.setRoute(route);
        }
    }
}
