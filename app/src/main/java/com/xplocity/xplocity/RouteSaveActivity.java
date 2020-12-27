package com.xplocity.xplocity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

import adapters.RouteSaveLocationsListAdapter;
import api_classes.RouteUploader;
import api_classes.interfaces.RouteUploaderInterface;
import managers.RouteMapManager;
import managers.interfaces.MapManagerInterface;
import models.Location;
import models.Route;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.UI.WaitWheel;


public class RouteSaveActivity
        extends ServiceBindingActivity
        implements RouteUploaderInterface,
        MapManagerInterface,
        RouteStatLocationsFragment.FragmentListener {

    private RouteMapManager mMapManager;
    private RouteSaveLocationsListAdapter mLocationAdapter;

    private WaitWheel mWaitWheel;
    private View mSaveBtn;

    private Route mRoute;

    private android.support.v4.app.FragmentManager mFragmentManager;
    private RouteStatLocationsFragment mLocationsFragment;

    //Logger
    Logger mLogger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_save);

        mFragmentManager = getSupportFragmentManager();
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);
        mSaveBtn = findViewById(R.id.btnOk);
        mSaveBtn.setEnabled(false);
    }


    @Override
    protected void onServiceBound() {
        mRoute = mService.getRoute();
        initMapManager();
        mSaveBtn.setEnabled(true);


        showResultNumbers((int) mRoute.distance, mRoute.duration);
        showProgress(mRoute.loc_cnt_total, mRoute.loc_cnt_explored);
        showLocations(mRoute.locations);
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


    @Override
    protected void onServiceUnbound() {

    }



    public void initMapManager() {
        MapView map = findViewById(R.id.map);
        mMapManager = new RouteMapManager(map, findViewById(android.R.id.content), this);
        mMapManager.setRoute(mRoute);

        if (mRoute.path.size() > 0)
            mMapManager.setOverviewCamera(mRoute.path.get(mRoute.path.size() - 1));
        else
            mMapManager.setOverviewCamera(mService.getLastposition());
    }

    @Override
    public void onMarkerClicked(models.Location location) {
        mLocationsFragment.scrollToLocation(location);
    };

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


    public void uploadRoute(View view) {
        RouteUploader routeUploader = new RouteUploader(this);
        routeUploader.uploadRoute(mRoute);
        mWaitWheel.showWaitAnimation();
    }

    public void cancelRoute(View view) {
        showCancelRouteDialog();
    }


    @Override
    public void onSuccessUploadRoute() {
        mWaitWheel.hideWaitAnimation();

        finishRoute();
    }

    @Override
    public void onErrorUploadRoute(String errorText) {
        mWaitWheel.hideWaitAnimation();
        Toast toast = Toast.makeText(this, errorText, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        showCancelRouteDialog();
    }

    private void showCancelRouteDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Unsaved route will be lost. Do you want to continue?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishRoute();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    private void finishRoute() {
        mService.destroyService();
        Intent intent = new Intent(getApplicationContext(), RoutesListActivity.class);
        startActivity(intent);
    }
}