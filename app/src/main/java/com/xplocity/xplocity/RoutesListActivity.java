package com.xplocity.xplocity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.Collections;

import adapters.RouteDescriptionsListAdapter;
import api_classes.RouteDescriptionImageDownloader;
import api_classes.RoutesDescriptionsDownloader;
import api_classes.interfaces.RouteDescriptionImageDownloaderInterface;
import api_classes.interfaces.RoutesDescriptionsDownloaderInterface;
import models.RouteDescription;

public class RoutesListActivity extends XplocityMenuActivity
        implements
            RoutesDescriptionsDownloaderInterface,
            RouteDescriptionImageDownloaderInterface {


    private RouteDescriptionsListAdapter mAdapter;
    private ArrayList<RouteDescription> mRouteDescriptions;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_list);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RouteNewActivity.class);
                startActivity(intent);
            }
        });

        mListView = (ListView) findViewById(R.id.chain_list);
        mListView.setOnItemClickListener(listListener);

        routesListInit();
    }

    private void routesListInit() {
        RoutesDescriptionsDownloader loader = new RoutesDescriptionsDownloader(this);
        loader.downloadRoutesDescriptions();
    }

    @Override
    public void onRouteDescriptionsDownloaded(ArrayList<RouteDescription> routeDescriptions) {
        mRouteDescriptions = routeDescriptions;
        Collections.sort(mRouteDescriptions);
        mAdapter = new RouteDescriptionsListAdapter(this, mRouteDescriptions);

        mListView.setAdapter(mAdapter);


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


}
