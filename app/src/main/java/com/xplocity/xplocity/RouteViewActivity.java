package com.xplocity.xplocity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

import api_classes.RouteDownloader;
import api_classes.interfaces.RouteDownloaderInterface;
import managers.RouteMapManager;
import managers.interfaces.MapManagerInterface;
import models.Location;
import models.Route;
import utils.StorageHelper;
import utils.UI.WaitWheel;

public class RouteViewActivity
        extends XplocityMenuActivity
        implements RouteDownloaderInterface,
        MapManagerInterface,
        RouteStatLocationsFragment.FragmentListener {

    private WaitWheel mWaitWheel;

    private RouteMapManager mMapManager = null;
    private int mRouteId;
    private android.support.v4.app.FragmentManager mFragmentManager;

    private RouteStatLocationsFragment mLocationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);

        Bundle recdData = getIntent().getExtras();
        mRouteId = recdData.getInt(getString(R.string.route_id_key));

        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);
        mFragmentManager = getSupportFragmentManager();

        initMapManager();
        downloadRoute();
    }


    public void initMapManager() {
        MapView map = (MapView) findViewById(R.id.map);
        mMapManager = new RouteMapManager(map, findViewById(android.R.id.content), this);
    }

    @Override
    public void onMarkerClicked(models.Location location) {
        ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(false);
        mLocationsFragment.scrollToLocation(location);
    }

    @Override
    public void onFocusDropped() {
        mLocationsFragment.dropFocus();
    }

    @Override
    public void onLocationSelected(Location location) {
        mMapManager.focusOnLocation(location);
    }

    @Override
    public void onLocationUnselected() {
        mMapManager.dropFocus();
    }

    @Override
    public int getHiddenMapHeight() {
        return 0;
    }


    public void downloadRoute() {
        RouteDownloader loader = new RouteDownloader(this);
        loader.downloadRoute(mRouteId);
    }

    @Override
    public void onRouteDownloaded(Route route) {
        if (mMapManager != null) {
            mMapManager.setRoute(route);

            showResultNumbers((int) route.distance, route.duration);
            showProgress(route.loc_cnt_total, route.loc_cnt_explored);
            showLocations(route.locations);

            mMapManager.zoomToRouteBoundingBox();
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

        mLocationsFragment = RouteStatLocationsFragment.newInstance(locations, true);
        fragmentTransaction.replace(R.id.fragment_locations_list, mLocationsFragment);
        fragmentTransaction.commit();
    }


    /**********   Menu  **************/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.miShareRoute).setVisible(true);
        menu.findItem(R.id.miShareRoute).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mWaitWheel.showWaitAnimation();
                shareRouteImage();
                mWaitWheel.hideWaitAnimation();
                return false;
            }
        });

        return true;
    }

    private void shareRouteImage() {
        Bitmap bmp = generateRouteImage();

        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        share.setType("image/*");

        // Create the URI from the media
        Uri uri = StorageHelper.saveImage(bmp);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }

    private Bitmap generateRouteImage() {
        View v = findViewById(R.id.appbar);
        v.buildDrawingCache();
        Bitmap bitmap = v.getDrawingCache();
        return bitmap;
    }


}
