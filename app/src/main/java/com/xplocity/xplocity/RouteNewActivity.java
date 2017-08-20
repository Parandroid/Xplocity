package com.xplocity.xplocity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import api_classes.LocationCategoriesDownloader;
import api_classes.interfaces.LocationCategoriesDownloaderInterface;
import models.LocationCategory;

public class RouteNewActivity extends AppCompatActivity implements LocationCategoriesDownloaderInterface {

    private static int TIME_SLIDER_MIN = 30; //Time slider minimum value


    private ArrayList<CheckBox> category_checkboxes; //Array of checkboxes (location categories)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_new);

        init_route_settings();

    }


    // Вызывается при нажатии на кнопку "Get locations"
    public void create_route(View view) {
        //TODO
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
