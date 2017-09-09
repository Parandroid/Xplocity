package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.xplocity.xplocity.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import models.RouteDescription;
import utils.Formatter;


/**
 * Created by dmitry on 02.08.17.
 */

public class RouteDescriptionsListAdapter extends ArrayAdapter<RouteDescription> {
    public RouteDescriptionsListAdapter(Context context, ArrayList<RouteDescription> chains) {
        super(context, 0, chains);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RouteDescription routeDescription = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.route_description_list_item, parent, false);
        }

        TextView distanceTxtView = (TextView) convertView.findViewById(R.id.distance);
        TextView durationTxtView = (TextView) convertView.findViewById(R.id.duration);
        TextView dateTxtView = (TextView) convertView.findViewById(R.id.date);
        TextView nameTxtView = (TextView) convertView.findViewById(R.id.name);
        TextView locationsExploredTxtView = (TextView) convertView.findViewById(R.id.locations_explored);
        TextView locationsTotalTxtView = (TextView) convertView.findViewById(R.id.locations_total);


        distanceTxtView.setText(Formatter.formatDistance(routeDescription.distance));
        dateTxtView.setText(Formatter.formatDate(routeDescription.date));
        durationTxtView.setText(Formatter.formatDuration(routeDescription.duration));
        nameTxtView.setText(routeDescription.name);
        locationsExploredTxtView.setText(Integer.toString(routeDescription.locCntExplored));
        locationsTotalTxtView.setText(Integer.toString(routeDescription.locCntTotal));

        return convertView;
    }




}
