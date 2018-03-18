package com.xplocity.xplocity;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import org.osmdroid.views.MapView;

import api_classes.RouteDownloader;
import api_classes.interfaces.RouteDownloaderInterface;
import managers.routeMapManager;
import models.Route;

public class RouteViewActivity extends FragmentActivity implements RouteDownloaderInterface {

    private routeMapManager mMapManager = null;
    private int mRouteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);

        Bundle recdData = getIntent().getExtras();
        mRouteId =  recdData.getInt(getString(R.string.route_id_key));

        initMapManager();
        downloadRoute();
    }


    public void initMapManager() {
        MapView map = (MapView) findViewById(R.id.map);
        mMapManager = new routeMapManager(map, findViewById(android.R.id.content));
    }


    public void downloadRoute() {
        RouteDownloader loader = new RouteDownloader(this);
        loader.downloadRoute(mRouteId);
    }

    @Override
    public void onRouteDownloaded(Route route) {
        if( mMapManager != null ) {
            mMapManager.setRoute(route);
            if (route.locations.size() > 0) {
                mMapManager.setOverviewCamera(route.locations.get(0).position);
            }
        }
    }
}
