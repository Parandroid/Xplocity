package com.xplocity.xplocity;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import api_classes.LocationCategoriesDownloader;
import api_classes.NewRouteDownloader;
import api_classes.interfaces.LocationCategoriesDownloaderInterface;
import api_classes.interfaces.NewRouteDownloaderInterface;
import models.LocationCategory;
import models.Route;

public class RouteNewActivity extends FragmentActivity implements LocationCategoriesDownloaderInterface, NewRouteDownloaderInterface, OnMapReadyCallback {

    private static int TIME_SLIDER_MIN = 30; //Time slider minimum value

    // Permissions
    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;

    //UI Objects
    private ArrayList<CheckBox> category_checkboxes; //Array of checkboxes (location categories)
    private FusedLocationProviderClient mFusedLocationClient; //Location client used to get current position
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_new);

        init_route_settings();

        fn_permission(); //TODO мб вынести работу с разрешениями в отдельный класс?
    }

    // Permissions
    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
            init_location_client();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;
                    init_location_client();

                } else {
                    Toast.makeText(getApplicationContext(), "Xplocity needs the permission for access your position to track your routes.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void init_location_client() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }



    // Called when "Get locations" button is pressed
    public void create_route(View view) {

        try {
            //TODO добавить проверку permissions
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            request_new_route(location.getLatitude(), location.getLongitude());
                        }
                    });
        } catch (SecurityException e) {
        }
    }

    private void request_new_route(Double lat, Double lon) {
        SeekBar sb = (SeekBar) findViewById(R.id.SelectTimeSlider);

        int loc_count = (sb.getProgress() + TIME_SLIDER_MIN) / 15;

        RadioGroup travel_type = (RadioGroup) findViewById(R.id.travel_type);
        double optimal_distance;
        int selectedId = travel_type.getCheckedRadioButtonId();

        if (selectedId == R.id.radio_cycling) {
            optimal_distance = 3d;
        } else if (selectedId == R.id.radio_walking) {
            optimal_distance = 1d;
        } else if (selectedId == R.id.radio_running) {
            optimal_distance = 2d;
        } else {
            optimal_distance = 3d;
        }

        // Populate array with IDs of checked categories
        ArrayList<Integer> checked_location_categories = new ArrayList<>();
        for (CheckBox checkBox : category_checkboxes) {
            if (checkBox.isChecked()) {
                checked_location_categories.add(((LocationCategory) checkBox.getTag()).id);
            }
        }

        NewRouteDownloader downloader = new NewRouteDownloader(this);
        downloader.download_new_route(lat, lon, loc_count, optimal_distance, checked_location_categories);
    }

    @Override
    public void onNewRouteDownload(Route p_route) {
        //Create the google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
        animator.showNext();

        //TODO сохранить полученный маршрут в Position Manager


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (boolean_permission) {
            try {
                mMap.setMyLocationEnabled(true);

                //TODO нарисовать на карте полученный массив локаций, переместить камеру к текущей позиции
            }
            catch (SecurityException e) {
                //TODO обрабатывать отсутствие прав
            }
        }
    }



    ////////// Route settings windows

    public void init_route_settings() {
        time_slider_init();

        LocationCategoriesDownloader downloader = new LocationCategoriesDownloader(this);
        downloader.download_location_categories();
    }

    @Override
    public void onLocationCategoriesDownload(ArrayList<LocationCategory> p_loc_categories) {
        location_categories_init(p_loc_categories);
    }


    private void location_categories_init(ArrayList<LocationCategory> location_categories) {
        category_checkboxes = new ArrayList<>();

        try {
            LinearLayout location_categories_list = (LinearLayout) findViewById(R.id.location_categories_list);
            for (LocationCategory cat : location_categories) {
                CheckBox checkbox = new CheckBox(this);
                checkbox.setTag(cat);
                checkbox.setText(cat.name);


                location_categories_list.addView(checkbox);
                category_checkboxes.add(checkbox);

            }

        } catch (Throwable e) {
            Log.e("chain", e.getMessage());
        }
    }



    public static String formatHoursAndMinutes(int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + " h " + minutes + " m";
    }

    private void time_slider_init() {
        SeekBar time_slider = (SeekBar) findViewById(R.id.SelectTimeSlider);
        time_slider.setMax(1440 - TIME_SLIDER_MIN);

        final TextView text_time = (TextView) findViewById(R.id.text_time);

        time_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_time.setText(formatHoursAndMinutes(progress + TIME_SLIDER_MIN));
            }
        });

        time_slider.setProgress(240-TIME_SLIDER_MIN);
    }

}
