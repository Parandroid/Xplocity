package com.xplocity.xplocity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import models.Location;
import models.enums.LocationExploreState;


public class RouteStatLocationsFragment extends Fragment {

    private static final String LOCATIONS = "locations";


    private ArrayList<Location> mLocations;
    private View mView;

    public RouteStatLocationsFragment() {
        // Required empty public constructor
    }


    public static RouteStatLocationsFragment newInstance(ArrayList<Location> locations) {
        RouteStatLocationsFragment fragment = new RouteStatLocationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(LOCATIONS, locations);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocations = (ArrayList<Location>) getArguments().getSerializable(LOCATIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_route_stat_locations, container, false);
        fillLocationsList();

        return mView;
    }

    private void fillLocationsList() {
        // fill location list
        LinearLayout exploredLocationListLayout = mView.findViewById(R.id.explored_locations_list);
        LinearLayout unexploredLocationListLayout = mView.findViewById(R.id.unexplored_locations_list);

        ArrayList<Location> locationsExplored = new ArrayList<Location>();
        ArrayList<Location> locationsUnexplored = new ArrayList<Location>();
        int unseenLocationsCount = 0;

        for (Location loc: mLocations) {
            if (loc.explored()) {
                locationsExplored.add(loc);
            } else if (loc.exploreState == LocationExploreState.POINT_NOT_EXPLORED)
            {
                locationsUnexplored.add(loc);
            } else if (loc.exploreState == LocationExploreState.CIRCLE) {
                unseenLocationsCount++;
            }
        }


        Collections.sort(locationsExplored, new Comparator<Location>() {
            public int compare(Location o1, Location o2) {
                if (o1.dateReached != null && o2.dateReached != null) {
                    return o1.dateReached.compareTo(o2.dateReached);
                }
                else {
                    return 0;
                }
            }
        });


        for (int i = 0; i < locationsExplored.size(); i++) {
            Location location = locationsExplored.get(i);
            View v = LayoutInflater.from(this.getActivity()).inflate(R.layout.route_save_location_list_item, null);

            TextView txtLocationName = (TextView) v.findViewById(R.id.txt_location_name);
            TextView txtLocationDescription = (TextView) v.findViewById(R.id.txt_location_description);
            TextView txtLocationTime = (TextView) v.findViewById(R.id.txt_location_time);

            txtLocationName.setText(location.name);
            txtLocationDescription.setText(location.description);
            if (location.dateReached != null) {
                txtLocationTime.setText(String.format("%02d:%02d", location.dateReached.withZone(DateTimeZone.getDefault()).getHourOfDay(), location.dateReached.withZone(DateTimeZone.getDefault()).getMinuteOfHour()));
            } else
            {
                txtLocationTime.setText("");
            }

            View rect = v.findViewById(R.id.icon_rect_up);
            if (i == 0) {
                int paddingDp = 8;
                float density = getResources().getDisplayMetrics().density;
                int paddingPixel = (int)(paddingDp * density);
                rect.setPadding(0, paddingPixel,0,0);
            }
            else if (i == locationsExplored.size() - 1) {
                int paddingDp = 40;
                float density = getResources().getDisplayMetrics().density;
                int paddingPixel = (int)(paddingDp * density);
                rect.setPadding(0, 0,0,paddingPixel);
            }

            exploredLocationListLayout.addView(v);

        }



        for (int i = 0; i < locationsUnexplored.size(); i++) {
            Location location = locationsUnexplored.get(i);
            View v = LayoutInflater.from(this.getActivity()).inflate(R.layout.route_save_location_list_item_unexplored, null);

            TextView txtLocationName = (TextView) v.findViewById(R.id.txt_location_name);
            txtLocationName.setText(location.name);

            unexploredLocationListLayout.addView(v);
        }


        View unseenLocationsLayout = mView.findViewById(R.id.unseen_locations_info);
        if (unseenLocationsCount > 0) {
            TextView txtUnseenLocationsCount = mView.findViewById(R.id.txt_unseen_locations);
            String unseenLocationsText = getString(R.string.unseen_locations_text_1)
                    + "<b> " + Integer.toString(unseenLocationsCount) + " </b>"
                    + getString(R.string.unseen_locations_text_2)
                    + "<br/>" + getString(R.string.unseen_locations_text_3);
            txtUnseenLocationsCount.setText(Html.fromHtml(unseenLocationsText));
        }
        else {
            unseenLocationsLayout.setVisibility(View.GONE);
        }



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
