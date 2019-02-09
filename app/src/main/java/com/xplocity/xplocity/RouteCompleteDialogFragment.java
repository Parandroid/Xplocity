package com.xplocity.xplocity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by dmitry on 09.02.19.
 */

public class RouteCompleteDialogFragment extends DialogFragment {

    public interface RouteCompleteDialogListener {
        void onFinishRoute();
    }



    public RouteCompleteDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static RouteCompleteDialogFragment newInstance() {
        RouteCompleteDialogFragment frag = new RouteCompleteDialogFragment();
            /*Bundle args = new Bundle();
            args.putString("title", title);
            frag.setArguments(args);*/
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_route_complete, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnFinish = (Button) view.findViewById(R.id.btn_finish);
        Button btnContinue = (Button) view.findViewById(R.id.btn_continue);


        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteCompleteDialogListener listener = (RouteCompleteDialogListener) getActivity();
                listener.onFinishRoute();
                dismiss();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }
}
