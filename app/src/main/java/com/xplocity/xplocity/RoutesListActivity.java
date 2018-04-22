package com.xplocity.xplocity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;

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
    private View mFooterLoading;

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


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RouteNewActivity.class);
                startActivity(intent);
            }
        });


        mRouteDescriptions = new ArrayList<RouteDescription>();
        mAdapter = new RouteDescriptionsListAdapter(this, mRouteDescriptions);
        mListView.setAdapter(mAdapter);

        downloadRoutesDescription();

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


}
