package com.xplocity.xplocity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import utils.Formatter;


public class RouteStatResultNumbersFragment extends Fragment {

    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";


    private int mDistance;
    private int mDuration;

    public RouteStatResultNumbersFragment() {
        // Required empty public constructor
    }

    public static RouteStatResultNumbersFragment newInstance(int distance, int duration) {
        RouteStatResultNumbersFragment fragment = new RouteStatResultNumbersFragment();
        Bundle args = new Bundle();
        args.putInt(DISTANCE, distance);
        args.putInt(DURATION, duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDistance = getArguments().getInt(DISTANCE);
            mDuration = getArguments().getInt(DURATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_route_stat_result_numbers, container, false);


        Formatter formatter = new Formatter();

        ((TextView) v.findViewById(R.id.distance)).setText(formatter.formatDistance(mDistance) + " km");
        ((TextView) v.findViewById(R.id.duration)).setText(formatter.formatHours(mDuration / 60000) + " h");
        ((TextView) v.findViewById(R.id.speed)).setText(Formatter.formatSpeed(calculateSpeed(mDistance, mDuration)) + " km/h");

        return v;
    }

    private float calculateSpeed(int distance, int duration) {
        float speed;
        if (duration != 0) {
            speed = distance / (duration / 1000f);
        } else {
            speed = 0f;
        }

        return speed;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
