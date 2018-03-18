package com.xplocity.xplocity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.osmdroid.views.MapView;

import api_classes.RouteUploader;
import api_classes.interfaces.RouteUploaderInterface;
import managers.routeMapManager;
import models.Route;
import utils.UI.WaitWheel;


public class RouteSaveActivity
        extends AppCompatActivity
        implements RouteUploaderInterface {

    private routeMapManager mMapManager;
    private WaitWheel mWaitWheel;

    private Route mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_save);

        mRoute = getIntent().getParcelableExtra("route");

        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);
        initMapManager();
    }


    public void initMapManager() {
        MapView map = (MapView) findViewById(R.id.map);
        mMapManager = new routeMapManager(map, findViewById(android.R.id.content));
        mMapManager.setRoute(mRoute);
        mMapManager.setOverviewCamera(mRoute.path.get(mRoute.path.size()-1));
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
        Intent intent = new Intent(getApplicationContext(), RoutesListActivity.class);
        startActivity(intent);
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
                        Intent intent = new Intent(getApplicationContext(), RoutesListActivity.class);
                        startActivity(intent);
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
}