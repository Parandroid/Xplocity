package adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import utils.UI.BottomSheetListView;


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
    public View getView(int position, View convertView, final ViewGroup parent) {
        final Location location = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.route_locations_list_item, parent, false);
        }

        final TextView txtLocationAddress = (TextView) convertView.findViewById(R.id.txtLocationAddress);
        final TextView txtLocationName = (TextView) convertView.findViewById(R.id.txtLocationName);
        final TextView txtLocationDescription = (TextView) convertView.findViewById(R.id.txtLocationDescription);

        Button btnLocationTrack = (Button) convertView.findViewById(R.id.btnLocationTrack);

        txtLocationAddress.setText(location.address);
        txtLocationName.setText(location.name);
        txtLocationDescription.setText(location.description);
        btnLocationTrack.setText(Formatter.formatDistance((int) location.distance));

        if (location.explored) {
            txtLocationName.setTextColor(ContextCompat.getColor(mContext, R.color.colorSuccess));
            txtLocationAddress.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
            txtLocationDescription.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
            btnLocationTrack.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
        }
        else {
            txtLocationName.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            txtLocationAddress.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            txtLocationDescription.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            btnLocationTrack.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
        }

        btnLocationTrack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.moveCameraPositionBelowLocation(location.position);
            }
        });


        LinearLayout layoutLocationInfo = (LinearLayout) convertView.findViewById(R.id.layoutLocationInfo);
        layoutLocationInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cycleTextViewExpansion(txtLocationName);
                cycleTextViewExpansion(txtLocationAddress);
                cycleTextViewExpansion(txtLocationDescription);
            }
        });


        return convertView;
    }


    private void cycleTextViewExpansion(TextView tv){
        int collapsedMaxLines = 1;
        ObjectAnimator animation = ObjectAnimator.ofInt(tv, "maxLines",
                tv.getMaxLines() == collapsedMaxLines? tv.getLineCount() : collapsedMaxLines);
        animation.setDuration(tv.getLineCount()*100).start();
    }




}
