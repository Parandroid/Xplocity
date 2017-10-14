package com.xplocity.xplocity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.Collections;

import adapters.RouteDescriptionsListAdapter;
import api_classes.RoutesDescriptionsDownloader;
import api_classes.interfaces.RoutesDescriptionsDownloaderInterface;
import models.RouteDescription;

public class RoutesListActivity extends AppCompatActivity implements RoutesDescriptionsDownloaderInterface {
    private RouteDescriptionsListAdapter mAdapter;

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

        ListView newsListView = (ListView) findViewById(R.id.chain_list);
        newsListView.setOnItemClickListener(listListener);

        routesListInit();
    }

    private void routesListInit() {
        RoutesDescriptionsDownloader loader = new RoutesDescriptionsDownloader(this);
        loader.downloadRoutesDescriptions();
    }

    @Override
    public void onRouteDescriptionsDownloaded(ArrayList<RouteDescription> routeDescriptions) {
        Collections.sort(routeDescriptions);
        mAdapter = new RouteDescriptionsListAdapter(this, routeDescriptions);
        ListView listView = (ListView)findViewById(R.id.chain_list);
        listView.setAdapter(mAdapter);
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
}
