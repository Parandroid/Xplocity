package adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xplocity.xplocity.R;

import java.util.ArrayList;
import java.util.Calendar;

import models.RouteDescription;
import utils.Formatter;

import static utils.ResourceGetter.getResources;


/**
 * Created by dmitry on 02.08.17.
 */

public class RouteDescriptionsListAdapter extends ArrayAdapter<RouteDescription> {

    private Context mContext;

    public RouteDescriptionsListAdapter(Context context, ArrayList<RouteDescription> chains) {
        super(context, 0, chains);
        mContext = context;
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

        TextView durationHoursTxtView = (TextView) convertView.findViewById(R.id.duration_hours);
        TextView durationMinutesTxtView = (TextView) convertView.findViewById(R.id.duration_minutes);
        TextView durationHourSymbolTxtView = (TextView) convertView.findViewById(R.id.duration_hour_symbol);
        TextView durationMinuteSymbolTxtView = (TextView) convertView.findViewById(R.id.duration_minute_symbol);

        TextView dateTxtView = (TextView) convertView.findViewById(R.id.date);
        //TextView yearTxtView = (TextView) convertView.findViewById(R.id.year);
        //TextView nameTxtView = (TextView) convertView.findViewById(R.id.name);
        TextView locationsExploredTxtView = (TextView) convertView.findViewById(R.id.locations_explored);
        TextView percentExploredTxtView = (TextView) convertView.findViewById(R.id.percent_explored);

        ImageView travelTypeImg = convertView.findViewById(R.id.img_travel_type);
        ImageView progressBadgeImg = convertView.findViewById(R.id.img_progress_badge);
        ImageView progress100Img = convertView.findViewById(R.id.img_progress_100);


        Formatter formatter = new Formatter();

        String dateStr = formatter.formatDateToString(routeDescription.date);
        dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);
        int routeYear = routeDescription.date.getYear();
        if (routeYear != Calendar.getInstance().get(Calendar.YEAR)) {
            dateStr = dateStr + " '" + Integer.toString(routeYear).substring(2);
        }

        dateTxtView.setText(dateStr);

        int totalMinutes = routeDescription.duration / 60000;
        int minutes = totalMinutes % 60;
        int hours = totalMinutes / 60;

        if (hours == 0) {
            durationMinutesTxtView.setText(Integer.toString(minutes));
            durationHourSymbolTxtView.setVisibility(View.GONE);
            durationHoursTxtView.setVisibility(View.GONE);
            durationMinuteSymbolTxtView.setVisibility(View.VISIBLE);
            durationMinutesTxtView.setVisibility(View.VISIBLE);
        } else {
            durationHoursTxtView.setText(formatter.formatHours(totalMinutes));
            durationMinuteSymbolTxtView.setVisibility(View.GONE);
            durationMinutesTxtView.setVisibility(View.GONE);
            durationHourSymbolTxtView.setVisibility(View.VISIBLE);
            durationHoursTxtView.setVisibility(View.VISIBLE);
        }


        distanceTxtView.setText(Formatter.formatDistance(routeDescription.distance));

        locationsExploredTxtView.setText(Integer.toString(routeDescription.locCntExplored));

        int percentExplored = Math.round(routeDescription.locCntExplored * 100f / routeDescription.locCntTotal);
        percentExploredTxtView.setText(Integer.toString(percentExplored) + "%");

        //int color = (int) new ArgbEvaluator().evaluate(percentExplored/100f, getResources().getColor(R.color.locationUnexploredFill), getResources().getColor(R.color.locationExploredFill));
        //GradientDrawable drawable = (GradientDrawable)  ((LayerDrawable) progressBadgeImg.getDrawable()).getDrawable(0);
        //drawable.setStroke(Formatter.dpToPx(5), color);
        //progressBadgeImg.setColorFilter(color);
        //progressBadgeImg.setAlpha(0xdd/256f);

        percentExploredTxtView.setTextColor(getResources().getColor(R.color.gray900));
        progress100Img.setVisibility(View.GONE);
        if (percentExplored < 50) {
            progressBadgeImg.setColorFilter(getResources().getColor(R.color.locationUnexploredFill));
            if (percentExplored < 25)
                progressBadgeImg.setAlpha(0.2f);
            else
                progressBadgeImg.setAlpha(0.5f);
        } else {
            progressBadgeImg.setColorFilter(getResources().getColor(R.color.locationExploredFill));
            if (percentExplored < 75) {
                progressBadgeImg.setAlpha(0.4f);
            } else if (percentExplored < 90) {
                progressBadgeImg.setAlpha(0.7f);
            } else if (percentExplored < 100) {
                progressBadgeImg.setAlpha(1f);
            } else if (percentExplored == 100) {
                progress100Img.setVisibility(View.VISIBLE);
                progressBadgeImg.setAlpha(1f);
                percentExploredTxtView.setTextColor(getResources().getColor(R.color.white));
                progressBadgeImg.setColorFilter(getResources().getColor(R.color.routeDescriptionLocProgress100Fill));
            }
        }

        switch (routeDescription.travelType) {
            case "walking":
                travelTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_walking));
                break;
            case "cycling":
                travelTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_cycling));
                break;
            default:
                travelTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_walking));
        }


        ImageView routeImageView = convertView.findViewById(R.id.route_image);
        Bitmap originalBitmap;
        if (routeDescription.image == null)
            originalBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.map_foreground), 0, 0, 130, 200);
        else
            originalBitmap = routeDescription.image;
        routeImageView.setImageBitmap(originalBitmap);


        return convertView;
    }


}
