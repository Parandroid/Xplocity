package com.xplocity.xplocity;


import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import adapters.LocationCommentTypesSpinnerAdapter;
import api_classes.LocationCommentUploader;
import api_classes.interfaces.LocationCommentUploaderInterface;
import app.XplocityApplication;
import models.Location;
import models.LocationComment;
import models.LocationCommentType;

/**
 * A simple {@link Fragment} subclass.
 */

public class BottomsheetLocationCommentFragment extends BottomSheetDialogFragment
        implements LocationCommentUploaderInterface {

    private View mView;
    private Spinner mSpinner;

    private Location mLocation;
    private LocationCommentTypesSpinnerAdapter mAdapter;

    private static final String LOCATION_KEY = "locationKey";


    public BottomsheetLocationCommentFragment() {
        // Required empty public constructor
    }

    public static BottomsheetLocationCommentFragment newInstance(Location location) {
        BottomsheetLocationCommentFragment fragment = new BottomsheetLocationCommentFragment();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mLocation = (Location) getArguments().getSerializable(LOCATION_KEY);

        mView = inflater.inflate(R.layout.fragment_bottomsheet_modal_location_comment, container, false);
        initMessageTypes();

        ((Button) mView.findViewById(R.id.btn_send_location_comment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendBtnClicked(v);
            }
        });


        return mView;
    }

    private void initMessageTypes() {
        LocationCommentType commentTypes[] = new LocationCommentType[] {
                new LocationCommentType(1, "Не удалось найти локацию"),
                new LocationCommentType(2, "Местоположение локации указано не верно"),
                new LocationCommentType(3, "Описание локации содержит ошибки"),
                new LocationCommentType(4, "Уточнение описания локации")
        };

        mAdapter = new LocationCommentTypesSpinnerAdapter(mView.getContext(), android.R.layout.simple_spinner_item, commentTypes);
        mSpinner = (Spinner) mView.findViewById(R.id.messageType);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setSelection(commentTypes.length);
    }




    public void onSendBtnClicked(View view) {
        LocationComment comment = new LocationComment();
        comment.location = mLocation;
        comment.message = ((EditText) mView.findViewById(R.id.message)).getText().toString();
        comment.messageType = mAdapter.getItem(mSpinner.getSelectedItemPosition()).id;

        LocationCommentUploader uploader = new LocationCommentUploader(this);
        uploader.uploadLocationComment(comment);

    }

    @Override
    public void onSuccessUploadLocationComment() {
        Toast toast = Toast.makeText(XplocityApplication.getAppContext(), "Comment sent", Toast.LENGTH_LONG);
        toast.show();
        this.dismiss();
    }

    @Override
    public void onErrorUploadLocationComment(String errorText) {
        Toast toast = Toast.makeText(XplocityApplication.getAppContext(), errorText, Toast.LENGTH_LONG);
        toast.show();
    }

}
