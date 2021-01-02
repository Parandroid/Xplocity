package com.xplocity.xplocity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.List;

import api_classes.LocationCategoriesDownloader;
import api_classes.NewRouteDownloader;
import api_classes.interfaces.LocationCategoriesDownloaderInterface;
import api_classes.interfaces.NewRouteDownloaderInterface;
import app.XplocityApplication;
import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;
import managers.PermissionManager;
import managers.RouteMapManager;
import managers.interfaces.MapManagerInterface;
import models.LocationCategory;
import models.Route;
import models.enums.TravelTypes;
import services.ServiceStateReceiver;
import services.interfaces.ServiceStateReceiverInterface;
import utils.Factory.LogFactory;
import utils.Formatter;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.UI.WaitWheel;


public class RouteNewActivity
        extends ServiceBindingActivity
        implements LocationCategoriesDownloaderInterface,
        NewRouteDownloaderInterface,
        ServiceStateReceiverInterface,
        RouteLocationList.OnFragmentInteractionListener,
        MapManagerInterface,
        RouteCompleteDialogFragment.RouteCompleteDialogListener,
        RouteStatLocationsFragment.FragmentListener {

    //Fragments
    private android.support.v4.app.FragmentManager mFragmentManager;
    private RouteStatLocationsFragment mLocationsFragment;

    //Constants
    private static int TIME_SLIDER_MIN = 30; //Time slider min value(30 min)
    private static int TIME_SLIDER_MAX = 1440; //Time slider max value(24 hours)
    private static int TIME_SLIDER_DEFAULT_VALUE = 240; //Time slider default value(3.5 hours)


    //Logger
    Logger mLogger;

    //Managers
    RouteMapManager mMapManager;

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

    SeekBar mSeekbar;
    ProgressBar mProgressbar;

    TextView mTxtLocExploredCount;
    TextView mTxtLocExploredPercent;
    TextView mTxtLocLeftCount;
    ImageButton btnShowClosestLocationSecondary;

    NewRoutePagerAdapter mPagerAdapter;
    ViewPager mPager;
    ViewPagerBottomSheetBehavior mBottomSheet;

    private GeoPoint mInitialPosition;
    private boolean mRouteReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_route_new);

        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        mFragmentManager = this.getSupportFragmentManager();

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


        mSeekbar = findViewById(R.id.location_progress_seekbar);
        mSeekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    onShowClosestLocationBtnClicked();
                }

                return true;
            }
        });

        mSeekbar.setVisibility(View.GONE);
        mBottomSheet = ViewPagerBottomSheetBehavior.from(findViewById(R.id.bottom_sheet_panel));

        mPagerAdapter = new NewRoutePagerAdapter(getSupportFragmentManager(), this);
        mPagerAdapter.addFragment(RouteLocationList.class.getName());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(mPagerAdapter);
        BottomSheetUtils.setupViewPager(mPager);

        receiver = new ServiceStateReceiver(this);

        super.onCreate(savedInstanceState);

        requestPermissions();

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
        mService.runServiceForeground();

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
        mTravelType = mService.getRoute().travelType;

        ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
        Animation inAnimation = animator.getInAnimation();
        Animation outAnimation = animator.getOutAnimation();
        animator.setInAnimation(null);
        animator.setOutAnimation(null);
        animator.setDisplayedChild(1);
        animator.setInAnimation(inAnimation);
        animator.setOutAnimation(outAnimation);

        mSeekbar.setVisibility(View.VISIBLE);

        onRouteReady();
    }


    // Executed when route downloaded initially or when it was initialized after restoring the app
    private void onRouteReady() {
        updateLocationsList();
        updateLocationProgress();
        initMapManager();
        if (mService.getUnexploredLocationsCount() == 0) {
            showRouteCompleteDialog();
        }

        mRouteReady = true;
        invalidateOptionsMenu(); // to show options that need a route
    }


    // Permissions
    boolean mLocationPermissionsGranted = false;
    boolean mWriteExternalStoragePermissionsGranted = false;


    private void requestPermissions() {
        if (!PermissionManager.requestPermissions(this)) {
            // if permissions have been already given
            mWriteExternalStoragePermissionsGranted = true;
            mLocationPermissionsGranted = true;
            initLocationClient();
            getCurrentPosition();
        }
        List<String> permissionsNeeded = new ArrayList<String>();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionManager.permissionsGranted(requestCode, permissions, grantResults)) {
            mWriteExternalStoragePermissionsGranted = true;
            mLocationPermissionsGranted = true;
            initLocationClient();
            getCurrentPosition();
        };
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
                            mInitialPosition = new GeoPoint(location);
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
        requestNewRoute(mInitialPosition.getLatitude(), mInitialPosition.getLongitude());
    }


    private TravelTypes mTravelType;

    private void requestNewRoute(final Double lat, final Double lon) {
        SeekBar sb = (SeekBar) findViewById(R.id.SelectTimeSlider);

        int locCount = (sb.getProgress() + TIME_SLIDER_MIN) / 15;

        RadioGroup travelType = (RadioGroup) findViewById(R.id.travel_type);
        int selectedId = travelType.getCheckedRadioButtonId();

        double optimalDistance = 2d;
        if (selectedId == R.id.radio_cycling) {
            mTravelType = TravelTypes.CYCLING;
            optimalDistance = 2d;
        } else if (selectedId == R.id.radio_walking) {
            mTravelType = TravelTypes.WALKING;
            optimalDistance = 0.5d;
        } else if (selectedId == R.id.radio_test) {
            mTravelType = TravelTypes.WALKING;
            optimalDistance = 0.01d;
            locCount = 200;
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
        route.travelType = mTravelType;
        mService.setRoute(route);

        onRouteReady();

        ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
        animator.showNext();
    }


    public void initMapManager() {
        MapView map = (MapView) findViewById(R.id.map);
        mMapManager = new RouteMapManager(map, findViewById(android.R.id.content), this, true);

        if (mLocationPermissionsGranted) {
            try {
                mMapManager.initMyLocation(mTravelType);
                //mMapManager.setMyLocationIconWalking();

                mMapManager.setRoute(mService.getRoute());
                updateRouteUI();

                if (mService.trackingActive()) {
                    mMapManager.setTrackingCamera(mService.getLastposition());
                } else {
                    mMapManager.setOverviewCamera(mInitialPosition);
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

    @Override
    public void onMarkerClicked(models.Location location) {
        View bottomSheetView = findViewById(R.id.bottom_sheet_panel);
        ViewPagerBottomSheetBehavior.from(bottomSheetView).setState(ViewPagerBottomSheetBehavior.STATE_EXPANDED);
        mPagerAdapter.getLocationsPage().showLocationInfo(location);

        mLocationsFragment.scrollToLocation(location);
    }



    @Override
    public void onFocusDropped() {
        onLocationInfoClosed();
        mPagerAdapter.getLocationsPage().showLocationList();
    }

    @Override
    public int getHiddenMapHeight() {
        if (mBottomSheet.getState() == ViewPagerBottomSheetBehavior.STATE_COLLAPSED) {
            return 0;
        } else {
            View bottomSheetView = findViewById(R.id.bottom_sheet_panel);
            int bottomSheetHeight = bottomSheetView.getHeight();
            return bottomSheetHeight;
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

            mSeekbar.setVisibility(View.VISIBLE);

            mStartTrackingButton.setEnabled(false);
            mStopTrackingButton.setEnabled(true);

            mMapManager.animateTrackingCamera(mInitialPosition);
        }
    }

    public void stopTrackingBtnPressed(View view) {
        try {
            if (mIsBound && mService.trackingActive() && mService.getRoute() != null) {
                //get last path update from service then stop the service
                updateRouteUI();
                Intent intent = new Intent(getApplicationContext(), RouteSaveActivity.class);
                stopTracking();
                startActivity(intent);
            } else if (mService.getRoute() == null) {
                stopTracking();
                Intent intent = new Intent(getApplicationContext(), RoutesListActivity.class);
                startActivity(intent);

            }
        } catch (Throwable e) {
            mLogger.logError("Redirection to route save screen failed", e);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                    .setContentTitle("Xplocity. Redirection to route save screen failed.")
                    .setContentText(e.getMessage());

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


    @Override
    public void onLocationReached(int locationId) {
        if (mMapManager != null) {
            for (models.Location loc : mService.getRoute().locations) {
                if (loc.id == locationId) {
                    mMapManager.updateLocationOnMap(loc);
                    break;
                }
            }
        }

        updateLocationProgress();
        updateLocationsList();

        if (mService.getUnexploredLocationsCount() == 0) {
            showRouteCompleteDialog();
        }
    }


    @Override
    public void onLocationCircleReached(int locationId) {
        if (mMapManager != null) {
            for (models.Location loc : mService.getRoute().locations) {
                if (loc.id == locationId) {
                    mMapManager.updateLocationOnMap(loc);
                    break;
                }
            }
        }

        updateLocationsList();
    }


    private void updateRouteUI() {
        if (mMapManager != null && mService != null) {
            if (mService.trackingActive()) {
                mMapManager.drawPath(mService.getRoute().getPathGeopoints());
                updateDistance();
                updateDuration();
                updateSpeed();
            }
        }
    }

    private void updateDistance() {
        if (mService.getRoute() != null) {
            mTxtDistance.setText(getString(R.string.n_km, Formatter.formatDistance((int) mService.getRoute().distance)));
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

            mTxtSpeed.setText(getString(R.string.n_km_h, Formatter.formatSpeed(speed)));
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
        builder1.setMessage(R.string.confirm_cancel_route);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopTracking();
                        mService.destroyService();
                        Intent intent = new Intent(getApplicationContext(), RoutesListActivity.class);
                        startActivity(intent);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.support.v7.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    /***************** Pager ********************/

    public class NewRoutePagerAdapter extends FragmentPagerAdapter {
        private ArrayList<String> mFragmentsNames;
        private SparseArray<Fragment> registeredFragments = new SparseArray<>();
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
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
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

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public RouteLocationList getLocationsPage() {
            return (RouteLocationList) getRegisteredFragment(0);
        }
    }


    private void updateLocationsList() {
        if (mLocationsFragment == null) {
            android.support.v4.app.FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            mLocationsFragment = RouteStatLocationsFragment.newInstance(mService.getRoute().locations, false);
            fragmentTransaction.replace(R.id.fragment_locations_list, mLocationsFragment);
            fragmentTransaction.commit();
        } else {
            mLocationsFragment.setLocations(mService.getRoute().locations);
        }
    }

    @Override
    public void onLocationInfoClosed() {
        mBottomSheet.setState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED);
        mLocationsFragment.dropFocus();
    }

    @Override
    public void onLocationSelected(models.Location location) {
        mMapManager.animateTrackingCamera(new GeoPoint(location.position.getLatitude(), location.position.getLongitude()));
        mPagerAdapter.getLocationsPage().showLocationInfo(location);
        mMapManager.focusOnLocation(location);
    }

    @Override
    public void onLocationUnselected() {
        mMapManager.dropFocus();
    }

    private void onShowClosestLocationBtnClicked() {
        mMapManager.animateToClosestUnexploredLocation(RouteNewActivity.this.mService.getLastposition());
    }


    /********** Progressbar  *********/

    private void updateLocationProgress() {

        if (mTxtLocExploredCount == null) {
            mTxtLocExploredCount = findViewById(R.id.locations_explored_count);
        }
        if (mTxtLocLeftCount == null) {
            mTxtLocLeftCount = findViewById(R.id.locations_left_count_number);
        }
        if (mTxtLocExploredPercent == null) {
            mTxtLocExploredPercent = findViewById(R.id.locations_explored_percent_number);
        }
        if (mProgressbar == null) {
            mProgressbar = findViewById(R.id.location_progressbar);
        }
        if (btnShowClosestLocationSecondary == null) {
            btnShowClosestLocationSecondary = findViewById(R.id.btn_show_closest_location_secondary);
            btnShowClosestLocationSecondary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShowClosestLocationBtnClicked();
                }
            });
        }


        int progress = mService.getRoute().loc_cnt_explored * 100 /  mService.getRoute().loc_cnt_total;
        int locLeftCount = mService.getRoute().loc_cnt_total - mService.getRoute().loc_cnt_explored;

        mTxtLocExploredCount.setText(String.valueOf(mService.getRoute().loc_cnt_explored));
        mTxtLocLeftCount.setText(String.valueOf(locLeftCount));
        mTxtLocExploredPercent.setText(String.valueOf(progress));

        mSeekbar.setMax(mService.getRoute().loc_cnt_total);
        mSeekbar.setProgress(mService.getRoute().loc_cnt_explored);

        mProgressbar.setMax(mService.getRoute().loc_cnt_total);
        mProgressbar.setProgress(mService.getRoute().loc_cnt_explored);

    }


    /********** Complete route dialog *********/

    private void showRouteCompleteDialog() {
        FragmentManager fm = getSupportFragmentManager();
        RouteCompleteDialogFragment dialog = RouteCompleteDialogFragment.newInstance();
        dialog.show(fm, "fragment_complete_route");
    }

    @Override
    public void onFinishRoute() {
        stopTrackingBtnPressed(null);
    }


    /**********   Menu  **************/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mRouteReady) {
            menu.findItem(R.id.miShareRoute).setVisible(true);
            menu.findItem(R.id.miGetSharedRoute).setVisible(false);

            menu.findItem(R.id.miShareRoute).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    FragmentManager fm = getSupportFragmentManager();
                    RouteShareDialog fragment = RouteShareDialog.newInstance(mService.getRoute());
                    fragment.show(fm, "shareRouteFragment");

                    return false;
                }
            });
        } else {
            menu.findItem(R.id.miGetSharedRoute).setVisible(true);
        }
        return true;
    }


}
