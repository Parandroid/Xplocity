package com.xplocity.xplocity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

import api_classes.RouteDownloader;
import api_classes.interfaces.RouteDownloaderInterface;
import managers.RouteMapManager;
import managers.interfaces.MapManagerInterface;
import models.Location;
import models.Route;
import utils.Formatter;
import utils.StorageHelper;
import utils.UI.WaitWheel;

public class RouteViewActivity
        extends XplocityMenuActivity
        implements RouteDownloaderInterface,
        MapManagerInterface,
        RouteStatLocationsFragment.FragmentListener {

    private WaitWheel mWaitWheel;

    private RouteMapManager mMapManager = null;
    private int mRouteId;
    private android.support.v4.app.FragmentManager mFragmentManager;

    private RouteStatLocationsFragment mLocationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mWaitWheel = new WaitWheel((FrameLayout) findViewById(R.id.waitWheel), this);
        mFragmentManager = getSupportFragmentManager();

        initMapManager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWaitWheel.showWaitAnimation();
        Bundle recdData = getIntent().getExtras();
        mRouteId = recdData.getInt(getString(R.string.route_id_key));
        if (recdData.get(getString(R.string.route_key)) != null) {
            Route route = recdData.getParcelable(getString(R.string.route_key));

            //Ugly fix osmdroid zoom bug https://stackoverflow.com/questions/10509130/why-osmdroid-loaded-a-lot-of-maps-zoom-to-a-wrong-place
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onRouteDownloaded(route);
                }
            }, 200);

        }
        else {
            downloadRoute();
        }
    }

    @Override
    public void onBackPressed() {
        redirectToRouteList();
    }

    private void redirectToRouteList() {
        Intent intent = new Intent(getApplicationContext(), RoutesListActivity.class);
        startActivity(intent);
    }


    public void initMapManager() {
        MapView map = (MapView) findViewById(R.id.map);
        mMapManager = new RouteMapManager(map, findViewById(android.R.id.content), this, false);
    }

    @Override
    public void onMarkerClicked(models.Location location) {
        ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(false);
        mLocationsFragment.scrollToLocation(location);
    }

    @Override
    public void onFocusDropped() {
        mLocationsFragment.dropFocus();
    }

    @Override
    public void onLocationSelected(Location location) {
        mMapManager.focusOnLocation(location);
    }

    @Override
    public void onLocationUnselected() {
        mMapManager.dropFocus();
    }

    @Override
    public int getHiddenMapHeight() {
        return 0;
    }


    public void downloadRoute() {
        RouteDownloader loader = new RouteDownloader(this);
        loader.downloadRoute(mRouteId);
    }

    @Override
    public void onRouteDownloaded(Route route) {
        if (mMapManager != null) {
            mMapManager.setRoute(route);

            showResultNumbers((int) route.distance, route.duration);
            showProgress(route.loc_cnt_total, route.loc_cnt_explored);
            showLocations(route.locations);

            mMapManager.zoomToRouteBoundingBox();
            mWaitWheel.hideWaitAnimation();

            Formatter formatter = new Formatter();
            setTitle(getString(R.string.title_activity_route_view) + " " + formatter.formatDate(route.date));
        }
    }

    private void showResultNumbers(int distance, int duration) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        RouteStatResultNumbersFragment resultFragment = RouteStatResultNumbersFragment.newInstance(distance, duration);
        fragmentTransaction.replace(R.id.fragment_result_numbers, resultFragment);
        fragmentTransaction.commit();
    }

    private void showProgress(int allLocCnt, int exploredLocCnt) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        RouteStatProgressCircleFragment progressFragment = RouteStatProgressCircleFragment.newInstance(allLocCnt, exploredLocCnt);
        fragmentTransaction.replace(R.id.fragment_progress_circle_container, progressFragment);
        fragmentTransaction.commit();
    }

    private void showLocations(ArrayList<Location> locations) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        mLocationsFragment = RouteStatLocationsFragment.newInstance(locations, true);
        fragmentTransaction.replace(R.id.fragment_locations_list, mLocationsFragment);
        fragmentTransaction.commit();
    }


    /**********   Menu  **************/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.miShareRoute).setVisible(true);
        menu.findItem(R.id.miShareRoute).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mWaitWheel.showWaitAnimation();
                shareRouteImage();
                mWaitWheel.hideWaitAnimation();
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                redirectToRouteList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    /**********   Route sharing  **************/

    private void shareRouteImage() {
        Bitmap bmp = generateRouteImage();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        Uri uri = StorageHelper.saveImage(bmp);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share to"));
    }

    private Bitmap generateRouteImage() {
        Bitmap mapBmp = bitmapFromView(R.id.map_layout);
        Bitmap statsBmp = bitmapFromView(R.id.fragment_result_numbers);

        return combineMapAndStatsBitmaps(mapBmp, statsBmp);
    }

    private Bitmap bitmapFromView(int viewId) {
        View v = findViewById(viewId);
        v.destroyDrawingCache();
        v.buildDrawingCache();
        return v.getDrawingCache();
    }

    private Bitmap combineMapAndStatsBitmaps(Bitmap mapBmp, Bitmap statsBmp) {
        Bitmap resultBmp = Bitmap.createBitmap(mapBmp.getWidth(), mapBmp.getHeight() + statsBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBmp);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        canvas.drawBitmap(mapBmp, 0, 0, null);
        canvas.drawBitmap(statsBmp, 0, mapBmp.getHeight(), null);
        return resultBmp;
    }



}
