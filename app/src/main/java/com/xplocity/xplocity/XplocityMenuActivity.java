package com.xplocity.xplocity;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class XplocityMenuActivity extends AppCompatActivity {

    protected Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onReportNewLocation(MenuItem mi) {
        FragmentManager fm = getSupportFragmentManager();
        ReportNewLocation fragment=  ReportNewLocation.newInstance();
        fragment.show(fm, "reportNewLocationFragment");
    }

    public void onGetSharedRoute(MenuItem mi) {
        FragmentManager fm = getSupportFragmentManager();
        GetSharedRouteDialog fragment=  GetSharedRouteDialog.newInstance();
        fragment.show(fm, "getSharedRouteFragment");
    }

}
