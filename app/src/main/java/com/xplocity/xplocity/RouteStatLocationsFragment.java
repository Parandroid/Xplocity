package com.xplocity.xplocity;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import models.Location;
import models.enums.LocationExploreState;


public class RouteStatLocationsFragment extends Fragment {

    public interface FragmentListener {
        void onLocationSelected(Location location);
        void onLocationUnselected();
    }


    private static final String LOCATIONS = "locations";

    private FragmentListener mCallback;

    private ArrayList<Location> mLocations;

    private ArrayList<Location> mLocationsExplored;
    private ArrayList<Location> mLocationsUnexplored;

    private View mView;

    LinearLayout mExploredLocationListLayout;
    LinearLayout mUnexploredLocationListLayout;
    View mUnseenLocationsLayout;

    private Boolean mShowUnseen;

    public RouteStatLocationsFragment() {
        // Required empty public constructor
    }


    public static RouteStatLocationsFragment newInstance(Boolean showUnseen) {
        RouteStatLocationsFragment fragment = new RouteStatLocationsFragment();
        fragment.mShowUnseen = showUnseen;
        return fragment;
    }

    public static RouteStatLocationsFragment newInstance(ArrayList<Location> locations, Boolean showUnseen) {
        RouteStatLocationsFragment fragment = new RouteStatLocationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(LOCATIONS, locations);
        fragment.setArguments(args);
        fragment.mShowUnseen = showUnseen;
        return fragment;
    }


    public void scrollToLocation(Location loc) {

        View v = mView.findViewWithTag(loc);

        if (v != null) {
            scrollToView(this.getActivity().findViewById(R.id.scroll_view), v);
            //v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            v.requestFocus();
        }
    }


    public void dropFocus() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                mView.findViewById(R.id.container).requestFocus();
            }
        });


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocations = (ArrayList<Location>) getArguments().getSerializable(LOCATIONS);
        }
        else
            mLocations = new ArrayList<Location>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_route_stat_locations, container, false);

        mExploredLocationListLayout = mView.findViewById(R.id.explored_locations_list);
        mUnexploredLocationListLayout = mView.findViewById(R.id.unexplored_locations_list);
        mUnseenLocationsLayout = mView.findViewById(R.id.unseen_locations_info);

        fillLocationsList();

        return mView;
    }


    public void setLocations(ArrayList<Location> locations) {
        mLocations = locations;
        fillLocationsList();
    }

    private void fillLocationsList() {
        mLocationsExplored = new ArrayList<Location>();
        mLocationsUnexplored = new ArrayList<Location>();

        mExploredLocationListLayout.removeAllViews();
        mUnexploredLocationListLayout.removeAllViews();

        int unseenLocationsCount = 0;

        for (Location loc : mLocations) {
            if (loc.explored()) {
                mLocationsExplored.add(loc);
            } else if (loc.exploreState == LocationExploreState.POINT_NOT_EXPLORED) {
                mLocationsUnexplored.add(loc);
            } else if (loc.exploreState == LocationExploreState.CIRCLE) {
                unseenLocationsCount++;
            }
        }


        Collections.sort(mLocationsExplored, new Comparator<Location>() {
            public int compare(Location o1, Location o2) {
                if (o1.dateReached != null && o2.dateReached != null) {
                    return o1.dateReached.compareTo(o2.dateReached);
                } else {
                    return 0;
                }
            }
        });


        for (int i = 0; i < mLocationsExplored.size(); i++) {
            Location location = mLocationsExplored.get(i);
            View v = LayoutInflater.from(this.getActivity()).inflate(R.layout.route_save_location_list_item, null);
            v.setTag(location);


            TextView txtLocationName = (TextView) v.findViewById(R.id.txt_location_name);
            TextView txtLocationDescription = (TextView) v.findViewById(R.id.txt_location_description);
            TextView txtLocationTime = (TextView) v.findViewById(R.id.txt_location_time);

            txtLocationName.setText(location.name);
            txtLocationDescription.setText(location.description);
            if (location.dateReached != null) {
                txtLocationTime.setText(String.format("%02d:%02d", location.dateReached.withZone(DateTimeZone.getDefault()).getHourOfDay(), location.dateReached.withZone(DateTimeZone.getDefault()).getMinuteOfHour()));
            } else {
                txtLocationTime.setText("");
            }

            View rect = v.findViewById(R.id.icon_rect_up);
            if (i == 0) {
                int paddingDp = 8;
                float density = getResources().getDisplayMetrics().density;
                int paddingPixel = (int) (paddingDp * density);
                rect.setPadding(0, paddingPixel, 0, 0);
            } else if (i == mLocationsExplored.size() - 1) {
                int paddingDp = 40;
                float density = getResources().getDisplayMetrics().density;
                int paddingPixel = (int) (paddingDp * density);
                rect.setPadding(0, 0, 0, paddingPixel);
            }

            mExploredLocationListLayout.addView(v);

            v.findViewById(R.id.container_explored).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    onLocationItemTouched(v, event, location);
                    return false;
                }
            });

        }


        for (int i = 0; i < mLocationsUnexplored.size(); i++) {
            Location location = mLocationsUnexplored.get(i);
            View v = LayoutInflater.from(this.getActivity()).inflate(R.layout.route_save_location_list_item_unexplored, null);
            v.setTag(location);

            TextView txtLocationName = (TextView) v.findViewById(R.id.txt_location_name);
            txtLocationName.setText(location.name);

            mUnexploredLocationListLayout.addView(v);

            v.findViewById(R.id.container_unexplored).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    onLocationItemTouched(v, event, location);

                    return false;
                }
            });
        }

        if (mShowUnseen) {
            if (unseenLocationsCount > 0) {
                TextView txtUnseenLocationsCount = mView.findViewById(R.id.txt_unseen_locations);
                String unseenLocationsText = getString(R.string.unseen_locations_text_1)
                        + "<b> " + Integer.toString(unseenLocationsCount) + " </b>"
                        + getString(R.string.unseen_locations_text_2)
                        + "<br/>" + getString(R.string.unseen_locations_text_3);
                txtUnseenLocationsCount.setText(Html.fromHtml(unseenLocationsText));
            } else {
                mUnseenLocationsLayout.setVisibility(View.GONE);
            }
        }
        else {
            mUnseenLocationsLayout.setVisibility(View.GONE);
        }

    }


    private void onLocationItemTouched(View v, MotionEvent event, Location location) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!v.hasFocus()) {
                if (mCallback != null) {
                    mCallback.onLocationSelected(location);
                }
            } else {
                if (mCallback != null) {
                    dropFocus();

                    mCallback.onLocationUnselected();
                }
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (FragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MyInterface ");
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void scrollToView(final NestedScrollView scrollViewParent, final View view) {
        // Get deepChild Offset
        Point childOffset = new Point();
        getDeepChildOffset(scrollViewParent, view.getParent(), view, childOffset);
        // Scroll to child.
        int rHeight = scrollViewParent.getHeight();

        scrollViewParent.post(new Runnable() {
            public void run() {
                scrollViewParent.scrollTo(0, childOffset.y - (rHeight / 3));
            }
        });

        //scrollViewParent.smoothScrollTo(0, childOffset.y - (rHeight/3));
    }

    /**
     * Used to get deep child offset.
     * <p/>
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumulated Offset.
     */
    private void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
    }

}
