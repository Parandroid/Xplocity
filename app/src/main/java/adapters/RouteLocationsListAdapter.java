package adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xplocity.xplocity.R;
import com.xplocity.xplocity.RouteLocationList;

import java.util.ArrayList;

import adapters.interfaces.RouteLocationsListAdapterInterface;
import models.Location;
import utils.Factory.LogFactory;
import utils.Formatter;
import utils.Log.Logger;
import utils.LogLevelGetter;


/**
 * Created by dmitry on 02.08.17.
 */

public class RouteLocationsListAdapter extends RecyclerView.Adapter<RouteLocationsListAdapter.ViewHolder> {

    private Logger mLogger;
    private Context mContext;
    private RouteLocationsListAdapterInterface mCallback;
    private RouteLocationList mLocationInfoFragment;

    private ArrayList<Location> mDataset;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtLocationAddress;
        public TextView txtLocationName;
        public TextView txtLocationDescription;
        public Button btnLocationTrack;
        public LinearLayout layoutLocationInfo;

        public ViewHolder(View v) {
            super(v);

            txtLocationAddress = (TextView) v.findViewById(R.id.txtLocationAddress);
            txtLocationName = (TextView) v.findViewById(R.id.txtLocationName);
            txtLocationDescription = (TextView) v.findViewById(R.id.txtLocationDescription);
            btnLocationTrack = (Button) v.findViewById(R.id.btnLocationTrack);
            layoutLocationInfo = (LinearLayout) v.findViewById(R.id.layoutLocationInfo);
        }
    }

    public RouteLocationsListAdapter(Context context, ArrayList<Location> locations, RouteLocationsListAdapterInterface callback, RouteLocationList locationInfoFragment) {
        mContext = context;
        mCallback = callback;
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        mLocationInfoFragment = locationInfoFragment;
        mDataset = locations;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_locations_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Location location = mDataset.get(position);

        holder.txtLocationAddress.setText(location.address);
        holder.txtLocationName.setText(location.name);
        holder.txtLocationDescription.setText(location.description);
        holder.btnLocationTrack.setText(Formatter.formatDistance((int) location.distance));

        if (location.explored()) {
            holder.txtLocationName.setTextColor(ContextCompat.getColor(mContext, R.color.colorSuccess));
            holder.txtLocationAddress.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
            holder.txtLocationDescription.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
            holder.btnLocationTrack.setTextColor(ContextCompat.getColor(mContext, R.color.darkerGray));
        }
        else {
            holder.txtLocationName.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            holder.txtLocationAddress.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            holder.txtLocationDescription.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
            holder.btnLocationTrack.setTextColor(ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light));
        }

        holder.btnLocationTrack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.moveCameraPositionBelowLocation(location.position);
            }
        });


        holder.layoutLocationInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mLocationInfoFragment.showLocationInfo(location);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }



    /*private void cycleTextViewExpansion(TextView tv){
        int collapsedMaxLines = 1;
        ObjectAnimator animation = ObjectAnimator.ofInt(tv, "maxLines",
                tv.getMaxLines() == collapsedMaxLines? tv.getLineCount() : collapsedMaxLines);
        animation.setDuration(tv.getLineCount()*100).start();
    }*/



}
