package com.xplocity.xplocity;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class XplocityMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onReportNewLocation(MenuItem mi) {
        showReportNewLocationDialog();
    }

    private void showReportNewLocationDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ReportNewLocation reportNewLocationFragment=  ReportNewLocation.newInstance();
        reportNewLocationFragment.show(fm, "reportNewLocationFragment");
    }
}
