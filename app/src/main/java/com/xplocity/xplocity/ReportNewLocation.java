package com.xplocity.xplocity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.views.MapView;

import api_classes.ReportedLocationUploader;
import api_classes.interfaces.ReportedLocationUploaderInterface;
import app.XplocityApplication;
import managers.interfaces.MapManagerInterface;
import managers.reportLocationMapManager;
import models.Location;


public class ReportNewLocation
        extends DialogFragment
        implements ReportedLocationUploaderInterface,
        MapManagerInterface {

    reportLocationMapManager mMapManager;

    EditText mEditName;
    EditText mEditDesc;


    public ReportNewLocation() {
        // Required empty public constructor
    }


    public static ReportNewLocation newInstance() {
        ReportNewLocation fragment = new ReportNewLocation();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        MapView map = getDialog().findViewById(R.id.map);
        mMapManager = new reportLocationMapManager(map, getView(), this);
        mMapManager.initMyLocation();
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_report_new_location, container, false);

        mEditName = (EditText) v.findViewById(R.id.locationName);
        mEditDesc = (EditText) v.findViewById(R.id.locationDescription);

        ((Button) v.findViewById(R.id.btnReportLocation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReportBtnPressed(view);
            }
        });
        ((Button) v.findViewById(R.id.btnCancelReportLocation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelBtnPressed(view);
            }
        });

        return v;
    }


    @Override
    public void onMarkerClicked(models.Location location) {};

    @Override
    public void onFocusDropped() {}

    @Override
    public int getHiddenMapHeight() {
        return 0;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onCancelBtnPressed(View view) {
        this.dismiss();
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }

    public void onReportBtnPressed(View view) {
        if (checkFields()) {
            Location location = new Location();
            location.name = mEditName.getText().toString();
            location.description = mEditDesc.getText().toString();
            location.position = mMapManager.getMarkerPosition();

            ReportedLocationUploader reportedLocationUploader = new ReportedLocationUploader(this, getContext());
            reportedLocationUploader.uploadReportedLocation(location);

        }
    }

    private boolean checkFields() {
        boolean ok = true;
        if (mEditName.getText().length() == 0) {
            ok = false;
            mEditName.setError("Enter location name");
        }
        if (mEditDesc.getText().length() == 0) {
            ok = false;
            mEditDesc.setError("Enter location description");
        }
        return ok;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onSuccessUploadReportedLocation() {
        Toast toast = Toast.makeText(XplocityApplication.getAppContext(), "Location successfully reported", Toast.LENGTH_LONG);
        toast.show();
        this.dismiss();
    }

    @Override
    public void onErrorUploadReportedLocation(String errorText) {
        Toast toast = Toast.makeText(XplocityApplication.getAppContext(), errorText, Toast.LENGTH_LONG);
        toast.show();
    }

}
