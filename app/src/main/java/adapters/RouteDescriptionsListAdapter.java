package adapters;

import android.content.Context;
import android.content.Intent;
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

        TextView distance = (TextView) convertView.findViewById(R.id.distance);
        TextView duration = (TextView) convertView.findViewById(R.id.duration);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView locations_explored = (TextView) convertView.findViewById(R.id.locations_explored);
        TextView locations_total = (TextView) convertView.findViewById(R.id.locations_total);


        distance.setText(format_distance(routeDescription.distance));
        date.setText(format_date(routeDescription.date));
        duration.setText(format_duration(routeDescription.duration));
        name.setText(routeDescription.name);
        locations_explored.setText(Integer.toString(routeDescription.loc_cnt_explored));
        locations_total.setText(Integer.toString(routeDescription.loc_cnt_total));

        return convertView;
    }

    private String format_distance(int distance) {
        String result = String.format("%.2f", (double)distance/1000) + " km";
        return result;
    }

    private String format_date(Date date) {
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return simpleDate.format(date);
    }

    private String format_duration(int duration) {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


}
