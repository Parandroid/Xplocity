package adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.xplocity.xplocity.R;

import java.util.ArrayList;

import adapters.interfaces.RouteLocationsListAdapterInterface;
import models.Location;
import utils.Factory.LogFactory;
import utils.Formatter;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.ResourceGetter;


/**
 * Created by dmitry on 02.08.17.
 */

public class RouteLocationsListAdapter extends ArrayAdapter<Location> {

    private Logger mLogger;
    private Context mContext;
    private RouteLocationsListAdapterInterface mCallback;

    public RouteLocationsListAdapter(Context context, ArrayList<Location> locations, RouteLocationsListAdapterInterface callback) {
        super(context, 0, locations);

        mContext = context;
        mCallback = callback;
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Location location = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.route_locations_list_item, parent, false);
        }

        TextView txtLocationAddress = (TextView) convertView.findViewById(R.id.txtLocationAddress);
        TextView txtLocationName = (TextView) convertView.findViewById(R.id.txtLocationName);
        TextView txtLocationDistance = (TextView) convertView.findViewById(R.id.txtLocationDistance);

        ImageButton btnLocationTrack = (ImageButton) convertView.findViewById(R.id.btnLocationTrack);

        txtLocationAddress.setText(location.address);
        txtLocationName.setText(location.name);
        txtLocationDistance.setText(Formatter.formatDistance((int) location.distance));

        if (location.explored) {
            txtLocationName.setTextColor(ContextCompat.getColor(mContext, R.color.colorSuccess));
            txtLocationAddress.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
            txtLocationDistance.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
        }
        else {
            txtLocationName.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            txtLocationAddress.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            txtLocationDistance.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
        }

        btnLocationTrack.setFocusable(false);
        btnLocationTrack.setFocusableInTouchMode(false);
        btnLocationTrack.setClickable(true);

        btnLocationTrack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.moveCameraPositionBelowLocation(location.position);
            }
        });


        return convertView;
    }




}
