package adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xplocity.xplocity.R;

import java.util.ArrayList;

import models.Location;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;


/**
 * Created by dmitry on 02.08.17.
 */

public class RouteSaveLocationsListAdapter extends RecyclerView.Adapter<RouteSaveLocationsListAdapter.ViewHolder> {

    private Logger mLogger;
    private Context mContext;


    private ArrayList<Location> mDataset;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtLocationName;
        public TextView txtLocationDescription;
        public TextView txtLocationTime;


        public ViewHolder(View v) {
            super(v);

            txtLocationName = (TextView) v.findViewById(R.id.txt_location_name);
            txtLocationDescription = (TextView) v.findViewById(R.id.txt_location_description);
            txtLocationTime = (TextView) v.findViewById(R.id.txt_location_name);
        }
    }

    public RouteSaveLocationsListAdapter(Context context, ArrayList<Location> locations) {
        mContext = context;
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        mDataset = locations;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_save_location_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Location location = mDataset.get(position);

        holder.txtLocationName.setText(location.name);
        holder.txtLocationDescription.setText(location.description);

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
