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

import api_classes.SharedRouteDownloader;
import api_classes.SharedRouteUploader;
import api_classes.interfaces.NewRouteDownloaderInterface;
import api_classes.interfaces.SharedRouteUploaderInterface;
import app.XplocityApplication;
import models.Route;


public class GetSharedRouteDialog
        extends DialogFragment
        implements NewRouteDownloaderInterface {


    private TextView mTxtRouteID;

    public GetSharedRouteDialog() {
        // Required empty public constructor
    }

    public static GetSharedRouteDialog newInstance() {
        GetSharedRouteDialog fragment = new GetSharedRouteDialog();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_get_shared_route, container, false);


        mTxtRouteID = (TextView) v.findViewById(R.id.txtShareRouteID);
        ((Button) v.findViewById(R.id.btnGetSharedRoute)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGetSharedRouteBtnPressed(view);
            }
        });

        return v;
    }


    public void onGetSharedRouteBtnPressed(View view) {
        SharedRouteDownloader routeDownloader = new SharedRouteDownloader(this);
        routeDownloader.downloadNewRoute(mTxtRouteID.getText().toString());

    }

    @Override
    public void onNewRouteDownloaded(Route route) {
        ((NewRouteDownloaderInterface) this.getDialog().getOwnerActivity()).onNewRouteDownloaded(route);
        this.dismiss();
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
