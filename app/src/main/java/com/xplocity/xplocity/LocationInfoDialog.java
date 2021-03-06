package com.xplocity.xplocity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import models.Location;


public class LocationInfoDialog
        extends DialogFragment {

    private static final String LOCATION_KEY = "locationKey";

    private Location mLocation;


    public LocationInfoDialog() {
        // Required empty public constructor
    }

    public static LocationInfoDialog newInstance(Location location) {
        LocationInfoDialog fragment = new LocationInfoDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(LOCATION_KEY, location);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mLocation = (Location) getArguments().getParcelable(LOCATION_KEY);

        View v = inflater.inflate(R.layout.fragment_route_new_location_info, container, false);
        return v;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        /*mListener = null;*/
    }



}
