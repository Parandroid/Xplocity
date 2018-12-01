package com.xplocity.xplocity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

import api_classes.RouteDownloader;
import api_classes.interfaces.RouteDownloaderInterface;
import managers.routeMapManager;
import models.Location;
import models.Route;

public class RouteViewActivity extends FragmentActivity implements RouteDownloaderInterface {

    private routeMapManager mMapManager = null;
    private int mRouteId;
    private android.support.v4.app.FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);

        Bundle recdData = getIntent().getExtras();
        mRouteId =  recdData.getInt(getString(R.string.route_id_key));

        mFragmentManager = getSupportFragmentManager();

        initMapManager();
        downloadRoute();
    }


    public void initMapManager() {
        MapView map = (MapView) findViewById(R.id.map);
        mMapManager = new routeMapManager(map, null, null, findViewById(android.R.id.content));
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

            showResultNumbers((int) route.distance, route.duration);
            showProgress(route.loc_cnt_total, route.loc_cnt_explored);
            showLocations(route.locations);
        }
    }

    private void showResultNumbers(int distance, int duration) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        RouteStatResultNumbersFragment resultFragment = RouteStatResultNumbersFragment.newInstance(distance, duration);
        fragmentTransaction.replace(R.id.fragment_result_numbers, resultFragment);
        fragmentTransaction.commit();
    }

    private void showProgress(int allLocCnt, int exploredLocCnt) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        RouteStatProgressCircleFragment progressFragment = RouteStatProgressCircleFragment.newInstance(allLocCnt, exploredLocCnt);
        fragmentTransaction.replace(R.id.fragment_progress_circle_container, progressFragment);
        fragmentTransaction.commit();
    }

    private void showLocations(ArrayList<Location> locations) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        RouteStatLocationsFragment locationsFragment = RouteStatLocationsFragment.newInstance(locations);
        fragmentTransaction.replace(R.id.fragment_locations_list, locationsFragment);
        fragmentTransaction.commit();
    }
}
