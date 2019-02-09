package com.xplocity.xplocity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;



public class RouteStatProgressCircleFragment extends Fragment {

    private static final String ALL_LOC_COUNT = "all_loc_count";
    private static final String EXPLORED_LOC_COUNT = "explored_loc_count";


    private int mAllLocCount;
    private int mExploredLocCount;

    public RouteStatProgressCircleFragment() {
        // Required empty public constructor
    }

    public static RouteStatProgressCircleFragment newInstance(int allLocCount, int exploredLocCount) {
        RouteStatProgressCircleFragment fragment = new RouteStatProgressCircleFragment();
        Bundle args = new Bundle();
        args.putInt(ALL_LOC_COUNT, allLocCount);
        args.putInt(EXPLORED_LOC_COUNT, exploredLocCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAllLocCount = getArguments().getInt(ALL_LOC_COUNT);
            mExploredLocCount = getArguments().getInt(EXPLORED_LOC_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_route_stat_progress_circle, container, false);


        int percentVisited = Math.round(mExploredLocCount * 100f / mAllLocCount);
        ((TextView) v.findViewById(R.id.locations_explored)).setText(Integer.toString(mExploredLocCount));
        ((ProgressBar) v.findViewById(R.id.progressBar)).setProgress(percentVisited);

        if (mExploredLocCount != mAllLocCount) {
            v.findViewById(R.id.badge_100).setVisibility(View.GONE);
            v.findViewById(R.id.text_100).setVisibility(View.GONE);
        }

        return v;
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
