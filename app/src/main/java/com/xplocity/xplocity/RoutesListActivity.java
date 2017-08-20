package com.xplocity.xplocity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;

import adapters.RouteDescriptionsListAdapter;
import api_classes.RouteDescriptionsDownloader;
import api_classes.interfaces.RouteDescriptionsDownloaderInterface;
import models.RouteDescription;

public class RoutesListActivity extends AppCompatActivity implements RouteDescriptionsDownloaderInterface {


    private RouteDescriptionsListAdapter adapter;


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

        routes_list_init();

    }


    // Routes list
    private void routes_list_init() {

        RouteDescriptionsDownloader route_descr_downolader = new RouteDescriptionsDownloader(this);
        route_descr_downolader.download_route_descriptions();

    }

    @Override
    public void onRouteDescriptionsDownload(ArrayList<RouteDescription> p_route_descriptions) {
        adapter = new RouteDescriptionsListAdapter(this, p_route_descriptions);

        ListView listView = (ListView)findViewById(R.id.chain_list);
        listView.setAdapter(adapter);
    }




    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), RouteViewActivity.class);
            RouteDescription route = adapter.getItem(position);
            intent.putExtra("route_id", route.id);
            startActivity(intent);
        }
    };


}
