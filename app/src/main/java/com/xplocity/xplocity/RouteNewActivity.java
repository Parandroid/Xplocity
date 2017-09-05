package com.xplocity.xplocity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import api_classes.LocationCategoriesDownloader;
import api_classes.NewRouteDownloader;
import api_classes.interfaces.LocationCategoriesDownloaderInterface;
import api_classes.interfaces.NewRouteDownloaderInterface;
import managers.MapManager;
import managers.PositionManager;
import models.LocationCategory;
import models.Route;
import services.ServiceStateReceiver;
import services.interfaces.ServiceStateReceiverInterface;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

//public class FragmentBindingActivity extends ServiceBindingActivity{
//
//}

public class RouteNewActivity
        extends ServiceBindingActivity
        implements LocationCategoriesDownloaderInterface,
        NewRouteDownloaderInterface,
        OnMapReadyCallback,
        ServiceStateReceiverInterface {

    private static int TIME_SLIDER_MIN = 30; //Time slider min value(30 min)
    private static int TIME_SLIDER_MAX = 1440; //Time slider max value(24 hours)
    private static int TIME_SLIDER_DEFAULT_VALUE = 240; //Time slider default value(3.5 hours)

    // Permissions
    private static final int REQUEST_PERMISSIONS_CODE = 100;
    boolean mLocationPermissionsGranted = false;

    //Logger
    Logger mLogger;

    //Managers
    MapManager mMapManager;
    PositionManager mPositionManager;

    ServiceStateReceiver receiver;

    //UI Objects
    private ArrayList<CheckBox> mCategoryCheckboxes; //Array of checkboxes (location categories)
    private FusedLocationProviderClient mFusedLocationClient; //Location client used to get current position

    FrameLayout mWaitWheel;
    AlphaAnimation mInAnimation;

    Button mStartTrackingButton;
    Button mStopTrackingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_new);

        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());

        mStartTrackingButton = (Button) findViewById(R.id.btn_start_tracking);
        mStopTrackingButton = (Button) findViewById(R.id.btn_stop_tracking);

        if (savedInstanceState == null) {
            mStartTrackingButton.setEnabled(false);
            mStopTrackingButton.setEnabled(false);
            initRouteSettings();
            mPositionManager = new PositionManager();
        }

        mWaitWheel = (FrameLayout) findViewById(R.id.waitWheel);
        mInAnimation = new AlphaAnimation(0f, 1f);
        mInAnimation.setDuration(200);
        mWaitWheel.setAnimation(mInAnimation);


        receiver = new ServiceStateReceiver(this);

        requestPermissions(); //TODO мб вынести работу с разрешениями в отдельный класс?

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        if (mPositionManager != null) {
            savedInstanceState.putParcelable("positionManager", mPositionManager);
        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getParcelable("positionManager") != null) {
            mPositionManager = savedInstanceState.getParcelable("positionManager");

            if (mService != null) {
                mPositionManager.setPath(mService.getPath());
                onPositionChanged();
            }

            ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
            Animation inAnimation = animator.getInAnimation();
            Animation outAnimation = animator.getOutAnimation();
            animator.setInAnimation(null);
            animator.setOutAnimation(null);
            animator.setDisplayedChild(1);
            animator.setInAnimation(inAnimation);
            animator.setOutAnimation(outAnimation);

            initMap();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (receiver != null) {
            receiver.unregisterReceiver(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (receiver != null) {
            receiver.registerReceiver(this);
        }
    }


    // Permissions
    private void requestPermissions() {
        int requestResult = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (requestResult!= PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.info_location_perm_request_head))
                        .setMessage(getString(R.string.info_location_perm_request_text))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mLogger.logInfo("Requesting location permissions");
                                ActivityCompat.requestPermissions(getParent(),
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_PERMISSIONS_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                mLogger.logInfo("Requesting location permissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_CODE);
            }
        } else {
            mLocationPermissionsGranted = true;
            initLocationClient();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionsGranted = true;
                    initLocationClient();
                    mLogger.logInfo("Location permissions granted");
                } else {
                    mLogger.logError(getString(R.string.error_location_perm_request_failed));
                    Toast.makeText(getApplicationContext(), getString(R.string.error_location_perm_request_failed), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void initLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    // Called when "Get locations" button is pressed
    public void createRoute(View view) {
        if(mLocationPermissionsGranted) {
            try {
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(100)        // 10 seconds, in milliseconds
                        .setFastestInterval(100); // 1 second, in milliseconds


                final LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        hideWaitAnimation();
                        if (!locationResult.getLocations().isEmpty()) {
                            Location location = locationResult.getLocations().get(0);
                            requestNewRoute(location.getLatitude(), location.getLongitude());
                            mPositionManager.lastPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            mFusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                };

                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                showWaitAnimation();
            } catch (SecurityException e) {
                mLogger.logError("Failed to create mRoute", e);
            }
        }
        else {
            mLogger.logVerbose("Failed to create mRoute: permissions not granted");
            Toast.makeText(getApplicationContext(), getString(R.string.info_location_perm_request_text), Toast.LENGTH_LONG).show();
        }
    }

    private void requestNewRoute(final Double lat, final Double lon) {
        SeekBar sb = (SeekBar)findViewById(R.id.SelectTimeSlider);

        int locCount = (sb.getProgress() + TIME_SLIDER_MIN) / 15;

        RadioGroup travelType = (RadioGroup) findViewById(R.id.travel_type);
        int selectedId = travelType.getCheckedRadioButtonId();

        double optimalDistance = 3d;
        if (selectedId == R.id.radio_cycling) {
            optimalDistance = 3d;
        } else if (selectedId == R.id.radio_walking) {
            optimalDistance = 1d;
        } else if (selectedId == R.id.radio_running) {
            optimalDistance = 2d;
        }

        // Populate array with IDs of checked categories
        ArrayList<Integer> checkedLocationCategories = new ArrayList<>();
        for (CheckBox checkBox : mCategoryCheckboxes) {
            if (checkBox.isChecked()) {
                checkedLocationCategories.add(((LocationCategory) checkBox.getTag()).id);
            }
        }

        NewRouteDownloader downloader = new NewRouteDownloader(this);
        downloader.downloadNewRoute(lat, lon, locCount, optimalDistance, checkedLocationCategories);
        showWaitAnimation();
    }

    @Override
    public void onNewRouteDownloaded(Route route) {
        hideWaitAnimation();
        mPositionManager.route = route;

        initMap();

        ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
        animator.showNext();
    }

    private void initMap() {
        //Create the google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapManager = new MapManager(googleMap);

        if (mLocationPermissionsGranted) {
            try {
                googleMap.setMyLocationEnabled(true);

                mMapManager.setRoute(mPositionManager.route);

                if (mPositionManager.trackingActive) {
                    mMapManager.setTrackingCamera(mPositionManager.lastPosition);
                }
                else {
                    mMapManager.setOverviewCamera(mPositionManager.route.locations.get(0).position);
                }

            } catch (SecurityException e) {
                //TODO обрабатывать отсутствие прав
                mLogger.logError("Cannot set up map", e);
            }
        }
        else {
            mLogger.logError("Cannot set up map: permissions not granted");
            Toast.makeText(getApplicationContext(), getString(R.string.info_location_perm_request_text), Toast.LENGTH_LONG).show();
        }
    }


    private void initRouteSettings() {
        timeSliderInit();
        LocationCategoriesDownloader downloader = new LocationCategoriesDownloader(this);
        downloader.downloadLocationCategories();
    }

    @Override
    public void onLocationCategoriesDownloaded(ArrayList<LocationCategory> p_loc_categories) {
        locationCategoriesInit(p_loc_categories);
    }

    private void locationCategoriesInit(ArrayList<LocationCategory> location_categories) {
        mCategoryCheckboxes = new ArrayList<>();

        try {
            LinearLayout locationCategoriesList = (LinearLayout) findViewById(R.id.location_categories_list);
            for (LocationCategory cat : location_categories) {
                CheckBox checkbox = new CheckBox(this);
                checkbox.setTag(cat);
                checkbox.setText(cat.name);

                locationCategoriesList.addView(checkbox);
                mCategoryCheckboxes.add(checkbox);
            }
        } catch (Throwable e) {
            mLogger.logError("Failed to set up location categories", e);
        }
    }

    private static String formatHoursAndMinutes(final int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + " h " + minutes + " m";
    }

    private void timeSliderInit() {
        SeekBar timeSlider = (SeekBar) findViewById(R.id.SelectTimeSlider);
        timeSlider.setMax(TIME_SLIDER_MAX - TIME_SLIDER_MIN);

        final TextView textTime = (TextView) findViewById(R.id.text_time);
        timeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textTime.setText(formatHoursAndMinutes(progress + TIME_SLIDER_MIN));
            }
        });

        timeSlider.setProgress(TIME_SLIDER_DEFAULT_VALUE - TIME_SLIDER_MIN);
    }


    public void startTracking(View view) {
        if(mIsBound && !mService.trackingActive())
        {
            mService.startTracking();
            mPositionManager.trackingActive = true;
            mStartTrackingButton.setEnabled(false);
            mStopTrackingButton.setEnabled(true);

            mMapManager.setTrackingCamera(mPositionManager.lastPosition);
        }
    }

    public void stopTracking(View view) {
        if(mIsBound && mService.trackingActive())
        {
            //get last position from service then stop the service
            onPositionChanged();
            Intent intent = new Intent(getApplicationContext(), RouteSaveActivity.class);
            intent.putExtra("route",   mPositionManager.route);


            mService.stopTracking();
            mPositionManager.trackingActive = false;
            mStartTrackingButton.setEnabled(true);
            mStopTrackingButton.setEnabled(false);


            startActivity(intent);
        }
    }

    @Override
    protected void onServiceBound(){
        boolean active = mService.trackingActive();
        if (mPositionManager != null) {
            mPositionManager.setPath(mService.getPath());
            onPositionChanged();
        }
        mStartTrackingButton.setEnabled(!active);
        mStopTrackingButton.setEnabled(active);
    }

    @Override
    protected void onServiceUnbound(){
        mStartTrackingButton.setEnabled(false);
        mStopTrackingButton.setEnabled(false);
    }


    @Override
    public void onPositionChanged() {
        if (mPositionManager != null && mMapManager != null) {
            if (mPositionManager.trackingActive) {
                mPositionManager.setPath(mService.getPath());
                mMapManager.drawPath(mPositionManager.route.path);
            }
        }
    }

    private void showWaitAnimation(){
        mWaitWheel.setVisibility(View.VISIBLE);
    }

    private void hideWaitAnimation(){
        mWaitWheel.setVisibility(View.GONE);
    }
}
