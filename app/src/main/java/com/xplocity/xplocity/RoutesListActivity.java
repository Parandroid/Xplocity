package com.xplocity.xplocity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import adapters.RouteDescriptionsListAdapter;
import api_classes.RouteDescriptionImageDownloader;
import api_classes.RoutesDescriptionsDeleter;
import api_classes.RoutesDescriptionsDownloader;
import api_classes.interfaces.RouteDescriptionImageDownloaderInterface;
import api_classes.interfaces.RoutesDescriptionsDeleterInterface;
import api_classes.interfaces.RoutesDescriptionsDownloaderInterface;
import managers.PermissionManager;
import models.RouteDescription;

public class RoutesListActivity extends ServiceBindingActivity
        implements
            RoutesDescriptionsDownloaderInterface,
            RouteDescriptionImageDownloaderInterface,
            RoutesDescriptionsDeleterInterface {


    private RouteDescriptionsListAdapter mAdapter;
    private ArrayList<RouteDescription> mRouteDescriptions;
    private ListView mListView;
    private View mFooterLoading;
    private FloatingActionButton mFab;

    private static final int MAX_ROUTES_PER_REQUEST = 10; //max number of routes returned by single request
    private int mCurrentOffset = 0;

    private boolean mFlagLoading = false;
    private boolean mAllRoutesDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_list);

        mListView = (ListView) findViewById(R.id.chain_list);
        mFooterLoading = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.route_description_list_item_footer_loading, null, false);


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RouteNewActivity.class);
                startActivity(intent);
            }
        });

        mFab.setVisibility(View.GONE);


        mRouteDescriptions = new ArrayList<RouteDescription>();
        mAdapter = new RouteDescriptionsListAdapter(this, mRouteDescriptions);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(listListener);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (!mAllRoutesDownloaded) {

                    if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                        if (mFlagLoading == false) {
                            mFlagLoading = true;
                            downloadRoutesDescription();
                        }
                    }
                }
            }
        });

        registerForContextMenu(mListView);


        requestPermissions();

    }

    private void requestPermissions() {
        if (!PermissionManager.requestPermissions(this)) {
            // if permissions have been already given
            mFab.setVisibility(View.VISIBLE);
            downloadRoutesDescription();
        }
        List<String> permissionsNeeded = new ArrayList<String>();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionManager.permissionsGranted(requestCode, permissions, grantResults)) {
            mFab.setVisibility(View.VISIBLE);
            downloadRoutesDescription();
        };
    }


    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }


    @Override
    protected void onServiceBound() {
        if (mService.trackingActive()) {
            Intent intent = new Intent(getApplicationContext(), RouteNewActivity.class);
            startActivity(intent);
        }
        else if (mService.savingActive()) {
            Intent intent = new Intent(getApplicationContext(), RouteSaveActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onServiceUnbound() {

    }

    private void downloadRoutesDescription() {
        mFlagLoading = true;
        mListView.addFooterView(mFooterLoading);

        RoutesDescriptionsDownloader loader = new RoutesDescriptionsDownloader(this);
        loader.downloadRoutesDescriptions(mCurrentOffset, MAX_ROUTES_PER_REQUEST);
    }


    @Override
    public void onRouteDescriptionsDownloaded(ArrayList<RouteDescription> routeDescriptions) {
        if (routeDescriptions.size() == 0) {
            mAllRoutesDownloaded = true;
        }
        else {

            if (mRouteDescriptions == null)
                mRouteDescriptions = routeDescriptions;
            else
                mRouteDescriptions.addAll(routeDescriptions);

            mCurrentOffset = mCurrentOffset + routeDescriptions.size();

            mAdapter.notifyDataSetChanged();
        }

        mListView.removeFooterView(mFooterLoading);
        mFlagLoading = false;

        //Collections.sort(mRouteDescriptions);



        //TODO: load images only for visible routes and couple routes below
        for (RouteDescription r : routeDescriptions) {
            RouteDescriptionImageDownloader loader = new RouteDescriptionImageDownloader(this);
            loader.downloadRoute(r.id);
        }

    }


    @Override
    public void onRouteDescriptionImageDownloaded(int routeId, Bitmap image) {
        for(RouteDescription r : mRouteDescriptions){
            if(r.id == routeId) {
                r.image = image;
                //mAdapter.notifyDataSetChanged();
                updateListView(routeId);
                break;
            }
        }
    }

    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), RouteViewActivity.class);
            RouteDescription route = mAdapter.getItem(position);
            intent.putExtra(getString(R.string.route_id_key), route.id);
            startActivity(intent);
        }
    };

    private void updateListView(int routeId){
        int start = mListView.getFirstVisiblePosition();
        for(int i=start, j=mListView.getLastVisiblePosition();i<=j;i++)
            if(routeId == ((RouteDescription) mListView.getItemAtPosition(i)).id) {
                View view = mListView.getChildAt(i-start);
                mListView.getAdapter().getView(i, view, mListView);
                break;
            }
    }

     /*   MENU   */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.chain_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            //menu.setHeaderTitle(Countries[info.position]);
            menu.add("Delete");

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        RouteDescription route = mRouteDescriptions.get(info.position);
        showDeleteRouteDialog(route.id);

        return true;
    }

    private void showDeleteRouteDialog(int routeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this route?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RoutesDescriptionsDeleter deleter = new RoutesDescriptionsDeleter(RoutesListActivity.this);
                        deleter.deleteRoute(routeId);
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }


    @Override
    public void onRouteDescriptionsDeleteSuccess(int deletedRouteId) {
        for(RouteDescription r : mRouteDescriptions){
            if(r.id == deletedRouteId) {
                mRouteDescriptions.remove(r);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    };

    @Override
    public void onRouteDescriptionsDeleteError(String error) {

    };


}
