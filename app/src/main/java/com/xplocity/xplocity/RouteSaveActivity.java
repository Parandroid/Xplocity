package com.xplocity.xplocity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import adapters.RouteSaveLocationsListAdapter;
import api_classes.RouteUploader;
import api_classes.interfaces.RouteUploaderInterface;
import managers.routeMapManager;
import models.Location;
import models.Route;
import utils.Factory.LogFactory;
import utils.Formatter;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.UI.WaitWheel;


public class RouteSaveActivity
        extends ServiceBindingActivity
        implements RouteUploaderInterface {

    private routeMapManager mMapManager;
    private RouteSaveLocationsListAdapter mLocationAdapter;

    private WaitWheel mWaitWheel;
    private View mSaveBtn;

    private Route mRoute;

    //Logger
    Logger mLogger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_save);

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

        ((TextView) findViewById(R.id.distance)).setText(Formatter.formatDistance((int) mRoute.distance) + " km");
        ((TextView) findViewById(R.id.duration)).setText(Formatter.formatHours(mRoute.duration/60000) + " h");
        updateSpeed();

        int percentVisited = Math.round(mRoute.loc_cnt_explored * 100f / mRoute.loc_cnt_total);
        ((TextView) findViewById(R.id.locations_explored)).setText(Integer.toString(mRoute.loc_cnt_explored));
        ((ProgressBar) findViewById(R.id.progressBar)).setProgress(percentVisited);

        // fill location list
        LinearLayout locationListLayout = findViewById(R.id.locations_list);

        ArrayList<Location> locationsExplored = new ArrayList<Location>();
        for (Location loc: mRoute.locations) {
            if (loc.explored()) {
                locationsExplored.add(loc);
            }
        }


        Collections.sort(locationsExplored, new Comparator<Location>() {
            public int compare(Location o1, Location o2) {
                return o1.dateReached.compareTo(o2.dateReached);
            }
        });


        for (int i = 0; i < locationsExplored.size(); i++) {
            Location location = locationsExplored.get(i);
            View v = LayoutInflater.from(this).inflate(R.layout.route_save_location_list_item, null);

            TextView txtLocationName = (TextView) v.findViewById(R.id.txt_location_name);
            TextView txtLocationDescription = (TextView) v.findViewById(R.id.txt_location_description);
            TextView txtLocationTime = (TextView) v.findViewById(R.id.txt_location_time);

            txtLocationName.setText(location.name);
            txtLocationDescription.setText(location.description);
            if (location.dateReached != null) {
                txtLocationTime.setText(Integer.toString(location.dateReached.getHourOfDay()) + ":" + Integer.toString(location.dateReached.getMinuteOfHour()));
            } else
            {
                txtLocationTime.setText("");
            }

            View rect = v.findViewById(R.id.icon_rect_up);
            if (i == 0) {
                int paddingDp = 8;
                float density = getResources().getDisplayMetrics().density;
                int paddingPixel = (int)(paddingDp * density);
                rect.setPadding(0, paddingPixel,0,0);
            }
            else if (i == locationsExplored.size() - 1) {
                int paddingDp = 25;
                float density = getResources().getDisplayMetrics().density;
                int paddingPixel = (int)(paddingDp * density);
                rect.setPadding(0, 0,0,paddingPixel);
            }

            locationListLayout.addView(v);

        }




    }

    @Override
    protected void onServiceUnbound() {

    }

    private void updateSpeed() {
        if (mRoute != null) {
            float speed;
            if (mRoute.duration != 0) {
                speed = mRoute.distance / (mRoute.duration / 1000f);
            } else {
                speed = 0f;
            }

            ((TextView) findViewById(R.id.speed)).setText(Formatter.formatSpeed(speed) + " km/h");
        }
    }


    public void initMapManager() {
        MapView map = findViewById(R.id.map);
        mMapManager = new routeMapManager(map, null, null, findViewById(android.R.id.content));
        mMapManager.setRoute(mRoute);

        if (mRoute.path.size() > 0)
            mMapManager.setOverviewCamera(mRoute.path.get(mRoute.path.size() - 1));
        else
            mMapManager.setOverviewCamera(mService.getLastposition());
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