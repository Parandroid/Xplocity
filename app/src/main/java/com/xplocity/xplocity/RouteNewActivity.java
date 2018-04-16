package com.xplocity.xplocity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

import adapters.RouteLocationsListAdapter;
import adapters.interfaces.RouteLocationsListAdapterInterface;
import api_classes.LocationCategoriesDownloader;
import api_classes.NewRouteDownloader;
import api_classes.interfaces.LocationCategoriesDownloaderInterface;
import api_classes.interfaces.NewRouteDownloaderInterface;
import app.XplocityApplication;
import managers.routeMapManager;
import models.LocationCategory;
import models.Route;
import services.ServiceStateReceiver;
import services.interfaces.ServiceStateReceiverInterface;
import utils.Factory.LogFactory;
import utils.Formatter;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.ResourceGetter;
import utils.UI.WaitWheel;


public class RouteNewActivity
        extends ServiceBindingActivity
        implements LocationCategoriesDownloaderInterface,
        NewRouteDownloaderInterface,
        ServiceStateReceiverInterface,
        RouteLocationsListAdapterInterface {

    private static int TIME_SLIDER_MIN = 30; //Time slider min value(30 min)
    private static int TIME_SLIDER_MAX = 1440; //Time slider max value(24 hours)
    private static int TIME_SLIDER_DEFAULT_VALUE = 240; //Time slider default value(3.5 hours)

    // Permissions
    private static final int REQUEST_ACCESS_FINE_LOCATION_CODE = 100;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_CODE = 101;
    boolean mLocationPermissionsGranted = false;
    boolean mWriteExternalStoragePermissionsGranted = false;

    //Logger
    Logger mLogger;

    //Managers
    routeMapManager mMapManager;

    ServiceStateReceiver receiver;

    //UI Objects
    private ArrayList<CheckBox> mCategoryCheckboxes; //Array of checkboxes (location categories)
    private FusedLocationProviderClient mFusedLocationClient; //Location client used to get current position

    TextView mTxtDistance;
    TextView mTxtDuration;
    TextView mTxtSpeed;

    WaitWheel mWaitWheel;

    Button mStartTrackingButton;
    Button mStopTrackingButton;

    NewRoutePagerAdapter mPagerAdapter;
    private RouteLocationsListAdapter mLocationAdapter;
    ViewPager mPager;


    private GeoPoint mCurrentPosition;
    private boolean mRouteReady = false;


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
        }

        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);


        mTxtDistance = (TextView) findViewById(R.id.textDistance);
        mTxtDuration = (TextView) findViewById(R.id.textDuration);
        mTxtSpeed = (TextView) findViewById(R.id.textSpeed);

        mPagerAdapter = new NewRoutePagerAdapter(getSupportFragmentManager(), this);
        mPagerAdapter.addFragment(RouteLocationList.class.getName());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        receiver = new ServiceStateReceiver(this);

        requestPermissions(); //TODO мб вынести работу с разрешениями в отдельный класс?
        if (savedInstanceState == null) {
            getCurrentPosition();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XplocityApplication.activityPaused();

        if (receiver != null) {
            receiver.unregisterReceiver(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        XplocityApplication.activityResumed();

        if (receiver != null) {
            receiver.registerReceiver(this);
        }

        // if some location was reached, we need to check it and recolor marker
        if (mService != null && mMapManager != null) {
            if (mService.trackingActive()) {
                mMapManager.updateLocationMarkers();
            }
        }

        //Get updates from service
        updateRouteUI();
    }


    @Override
    protected void onServiceBound() {
        if (mService.trackingActive()) {
            restoreActivityState();
        }

        updateRouteUI();

        boolean active = mService.trackingActive();
        mStartTrackingButton.setEnabled(!active);
        mStopTrackingButton.setEnabled(active);
    }

    @Override
    protected void onServiceUnbound() {
        mStartTrackingButton.setEnabled(false);
        mStopTrackingButton.setEnabled(false);
    }


    private void restoreActivityState() {
        ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
        Animation inAnimation = animator.getInAnimation();
        Animation outAnimation = animator.getOutAnimation();
        animator.setInAnimation(null);
        animator.setOutAnimation(null);
        animator.setDisplayedChild(1);
        animator.setInAnimation(inAnimation);
        animator.setOutAnimation(outAnimation);

        onRouteReady();
    }


    // Executed when route downloaded initially or when it was initialized after restoring the app
    private void onRouteReady() {
        fillLocationsList(mService.getRoute().locations);
        initMapManager();

        mRouteReady = true;
        invalidateOptionsMenu(); // to show options that need a route
    }


    // Permissions
    private void requestPermissions() {
        requestFineLocationPermission();
        requestWriteExternalStoragePermission();
    }


    private void requestFineLocationPermission() {
        int requestResult = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (requestResult != PackageManager.PERMISSION_GRANTED) {
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
                                        REQUEST_ACCESS_FINE_LOCATION_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                mLogger.logInfo("Requesting location permissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION_CODE);
            }
        } else {
            mLocationPermissionsGranted = true;
            initLocationClient();
        }
    }


    private void requestWriteExternalStoragePermission() {
        int requestResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (requestResult != PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.info_write_external_storage_perm_request_head))
                        .setMessage(getString(R.string.info_write_external_storage_perm_request_text))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mLogger.logInfo("Requesting write to external storage permissions");
                                ActivityCompat.requestPermissions(getParent(),
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                mLogger.logInfo("Requesting write to external permissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
            }
        } else {
            mWriteExternalStoragePermissionsGranted = true;
            initLocationClient();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionsGranted = true;
                    initLocationClient();
                    mLogger.logInfo("Location permissions granted");
                } else {
                    mLogger.logError(getString(R.string.error_location_perm_request_failed));
                    Toast.makeText(getApplicationContext(), getString(R.string.error_location_perm_request_failed), Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_WRITE_EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mWriteExternalStoragePermissionsGranted = true;
                    mLogger.logInfo("Write to external storage permission granted");
                } else {
                    mLogger.logError(getString(R.string.error_write_external_storage_perm_request_failed));
                    Toast.makeText(getApplicationContext(), getString(R.string.error_write_external_storage_perm_request_failed), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void initLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void getCurrentPosition() {
        if (mLocationPermissionsGranted) {
            try {
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(100)        // 10 seconds, in milliseconds
                        .setFastestInterval(100); // 1 second, in milliseconds


                final LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        mWaitWheel.hideWaitAnimation();
                        if (!locationResult.getLocations().isEmpty()) {
                            Location location = locationResult.getLocations().get(0);
                            //mService.setLastPosition(new GeoPoint(location.getLatitude(), location.getLongitude()));
                            mFusedLocationClient.removeLocationUpdates(this);
                            mCurrentPosition = new GeoPoint(location);
                            ((Button) findViewById(R.id.btn_create_chain)).setEnabled(true);
                        }
                    }
                };

                mWaitWheel.showWaitAnimation();
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            } catch (SecurityException e) {
                mLogger.logError("Failed to create mRoute", e);
            }
        } else {
            mLogger.logVerbose("Failed to create mRoute: permissions not granted");
            Toast.makeText(getApplicationContext(), getString(R.string.info_location_perm_request_text), Toast.LENGTH_LONG).show();
        }
    }


    // Called when "Get locations" button is pressed
    public void createRoute(View view) {
        requestNewRoute(mCurrentPosition.getLatitude(), mCurrentPosition.getLongitude());
    }

    private void requestNewRoute(final Double lat, final Double lon) {
        SeekBar sb = (SeekBar) findViewById(R.id.SelectTimeSlider);

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
        mWaitWheel.showWaitAnimation();
    }

    @Override
    public void onNewRouteDownloaded(Route route) {
        mWaitWheel.hideWaitAnimation();
        mService.setRoute(route);

        onRouteReady();

        ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
        animator.showNext();
    }



    public void initMapManager() {
        MapView map = (MapView) findViewById(R.id.map);
        mMapManager = new routeMapManager(map, findViewById(android.R.id.content));

        if (mLocationPermissionsGranted) {
            try {
                mMapManager.initMyLocation();

                mMapManager.setRoute(mService.getRoute());
                updateRouteUI();

                if (mService.trackingActive()) {
                    mMapManager.setTrackingCamera(mService.getLastposition());
                } else {
                    mMapManager.setOverviewCamera(mService.getRoute().locations.get(0).position);
                }

            } catch (SecurityException e) {
                //TODO обрабатывать отсутствие прав
                mLogger.logError("Cannot set up map", e);
            }
        } else {
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
                textTime.setText(Formatter.formatHoursAndMinutes(progress + TIME_SLIDER_MIN));
            }
        });

        timeSlider.setProgress(TIME_SLIDER_DEFAULT_VALUE - TIME_SLIDER_MIN);
    }


    public void startTrackingBtnPressed(View view) {
        if (mIsBound && !mService.trackingActive()) {
            mService.startTracking();

            mStartTrackingButton.setEnabled(false);
            mStopTrackingButton.setEnabled(true);

            mMapManager.animateTrackingCamera(mCurrentPosition);
        }
    }

    public void stopTrackingBtnPressed(View view) {
        if (mIsBound && mService.trackingActive()) {
            //get last path update from service then stop the service
            updateRouteUI();
            Intent intent = new Intent(getApplicationContext(), RouteSaveActivity.class);
            intent.putExtra("route", mService.getRoute());
            stopTracking();
            startActivity(intent);
        }
    }

    private void stopTracking() {
        mService.stopTracking();
        mStartTrackingButton.setEnabled(true);
        mStopTrackingButton.setEnabled(false);
    }


    @Override
    public void onPositionChanged() {
        updateRouteUI();
    }


    private void updateRouteUI() {
        if (mMapManager != null && mService != null) {
            if (mService.trackingActive()) {
                mMapManager.drawPath(mService.getRoute().path);
                updateDistance();
                updateDuration();
                updateSpeed();
            }
        }
    }

    private void updateDistance() {
        if (mService.getRoute() != null) {
            mTxtDistance.setText(Formatter.formatDistance((int) mService.getRoute().distance));
        }
    }

    private void updateDuration() {
        if (mService.getRoute() != null) {
            mTxtDuration.setText(Formatter.formatDuration(mService.getRoute().duration));
        }
    }

    private void updateSpeed() {
        if (mService.getRoute() != null) {
            float speed;
            if (mService.getRoute().duration != 0) {
                speed = mService.getRoute().distance / (mService.getRoute().duration / 1000f);
            } else {
                speed = 0f;
            }

            mTxtSpeed.setText(Formatter.formatSpeed(speed));
        }
    }


    @Override
    public void onLocationReached(int locationId) {
        if (mMapManager != null) {
            for (models.Location loc : mService.getRoute().locations) {
                if (loc.id == locationId) {
                    mMapManager.setLocationMarkerExplored(loc);
                    break;
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (mService != null) {
            if (mService.trackingActive()) {
                showCancelRouteDialog();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }

    }

    private void showCancelRouteDialog() {
        android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(this);
        builder1.setMessage("Unsaved route will be lost. Do you want to continue?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopTracking();
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

        android.support.v7.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    /***************** Pager ********************/

    class NewRoutePagerAdapter extends FragmentPagerAdapter {
        private ArrayList<String> mFragmentsNames;
        private Context mContext;

        public NewRoutePagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mFragmentsNames = new ArrayList();
            mContext = context;
        }

        public void addFragment(String fragmentClass) {
            mFragmentsNames.add(fragmentClass);
        }

        @Override
        public Fragment getItem(int position) {
            //return MyFragment.newInstance();
            return Fragment.instantiate(mContext, mFragmentsNames.get(position));
        }

        @Override
        public int getCount() {
            // return CONTENT.length;
            return mFragmentsNames.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }


    public void fillLocationsList(ArrayList<models.Location> locations) {
        mLocationAdapter = new RouteLocationsListAdapter(this, locations, this);
        ListView listView = (ListView)findViewById(R.id.listLocations);
        listView.setAdapter(mLocationAdapter);

        // Update locations info on bottom sheet expanded
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_panel));
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mLocationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });
    }


    @Override
    public void moveCameraPositionBelowLocation(GeoPoint position) {
        mMapManager.animateTrackingCamera(new GeoPoint(position.getLatitude() -0.001, position.getLongitude()));
        //mMapManager.animateTrackingCamera(position);
    }




    /**********   Menu  **************/

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (mRouteReady) {
            menu.findItem(R.id.miShareRoute).setVisible(true);
            menu.findItem(R.id.miGetSharedRoute).setVisible(false);

            menu.findItem(R.id.miShareRoute).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    FragmentManager fm = getSupportFragmentManager();
                    RouteShareDialog fragment =  RouteShareDialog.newInstance(mService.getRoute());
                    fragment.show(fm, "shareRouteFragment");

                    return false;
                }
            });
        }
        else
        {
            menu.findItem(R.id.miGetSharedRoute).setVisible(true);
        }
        return true;
    }


}
