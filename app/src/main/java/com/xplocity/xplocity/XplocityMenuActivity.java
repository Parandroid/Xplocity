package com.xplocity.xplocity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import services.XplocityPositionService;

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

    public void onSignOut(MenuItem mi) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.are_you_sure_sign_out);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                getString(R.string.sign_out),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService();
                        dialog.cancel();
                        clearStorage();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });

        builder1.setNegativeButton(
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    private void stopService() {
        XplocityPositionService service;
        service = XplocityPositionService.getInstance();

        if (service!=null) {
            service.stopTracking();
            service.destroyService();
        }
    }

    private void clearStorage() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();
        editor.commit();

    }



}
