package com.xplocity.xplocity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import models.Location;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RouteLocationList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RouteLocationList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteLocationList extends Fragment {


    private View mLocationInfoPage;
    private View mLocationsListPage;


    private OnFragmentInteractionListener mListener;

    public RouteLocationList() { }


    public static RouteLocationList newInstance() {
        RouteLocationList fragment = new RouteLocationList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bottomsheet_location_page, container, false);

        mLocationInfoPage = v.findViewById(R.id.bottom_sheet_page_location_info);
        mLocationsListPage = v.findViewById(R.id.bottom_sheet_page_locations_list);

        showLocationList();

        return v;
    }


    public void showLocationInfo(final Location loc) {

        //((NestedScrollView) mLocationInfoPage).scrollTo(0, 0);;;

        mLocationInfoPage.setVisibility(View.VISIBLE);
        mLocationsListPage.setVisibility(View.GONE);
        mLocationInfoPage.bringToFront();
        ((View) mLocationsListPage.getParent()).requestLayout();
        ((View) mLocationsListPage.getParent()).invalidate();

        ((TextView) mLocationInfoPage.findViewById(R.id.name)).setText(loc.name);
        ((TextView) mLocationInfoPage.findViewById(R.id.address)).setText(loc.address);

        if (loc.explored()) {
            ((TextView) mLocationInfoPage.findViewById(R.id.description)).setText(loc.description);
        }
        else {
            ((TextView) mLocationInfoPage.findViewById(R.id.description)).setText("???");
        }

        ((ImageButton) mLocationInfoPage.findViewById(R.id.closeButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationList();
                mListener.onLocationInfoClosed();
            }
        });

        ((ImageButton) mLocationInfoPage.findViewById(R.id.btn_return_to_locations_list)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationList();
            }
        });

        ((Button) mLocationInfoPage.findViewById(R.id.btn_comment_location)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomsheetLocationCommentFragment fragment = BottomsheetLocationCommentFragment.newInstance(loc);
                fragment.show(getFragmentManager(), fragment.getTag());
            }
        });
    }

    public void showLocationList() {
        mLocationInfoPage.setVisibility(View.GONE);
        mLocationsListPage.setVisibility(View.VISIBLE);
        mLocationsListPage.bringToFront();
        ((View) mLocationsListPage.getParent()).requestLayout();
        ((View) mLocationsListPage.getParent()).invalidate();

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLocationInfoClosed();
    }
}
