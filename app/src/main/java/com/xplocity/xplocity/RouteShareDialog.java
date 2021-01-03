package com.xplocity.xplocity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import api_classes.SharedRouteUploader;
import api_classes.interfaces.SharedRouteUploaderInterface;
import app.XplocityApplication;
import models.Route;


public class RouteShareDialog
        extends DialogFragment
        implements SharedRouteUploaderInterface {

    private static final String ROUTE_KEY = "routeKey";

    private Route mRoute;
    private String mRouteId;

    private TextView mTxtRouteID;

    public RouteShareDialog() {
        // Required empty public constructor
    }

    public static RouteShareDialog newInstance(Route route) {
        RouteShareDialog fragment = new RouteShareDialog();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ROUTE_KEY, route);
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

        mRoute = (Route) getArguments().getParcelable(ROUTE_KEY);

        View v = inflater.inflate(R.layout.fragment_share_route, container, false);


        mTxtRouteID = (TextView) v.findViewById(R.id.txtShareRouteID);
        ((Button) v.findViewById(R.id.btnShareRouteID)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShareBtnPressed(view);
            }
        });
        ((Button) v.findViewById(R.id.btnUploadSharedRoute)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUploadRouteBtnPressed(view);
            }
        });

        return v;
    }

    public void onShareBtnPressed(View view) {

        ShareCompat.IntentBuilder
                .from(getActivity())
                .setText(mRouteId)
                .setType("text/plain")
                .setChooserTitle("Share route")
                .startChooser();

    }

    public void onUploadRouteBtnPressed(View view) {
        SharedRouteUploader routeUploader = new SharedRouteUploader(this, getContext());
        routeUploader.uploadSharedRoute(mRoute);

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



    @Override
    public void onSuccessUploadRoute(String sharedRouteID) {
        mRouteId = sharedRouteID;
        mTxtRouteID.setText(mRouteId);
    }

    @Override
    public void onErrorUploadRoute(String errorText) {
        Toast toast = Toast.makeText(XplocityApplication.getAppContext(), errorText, Toast.LENGTH_LONG);
        toast.show();
    }

}
