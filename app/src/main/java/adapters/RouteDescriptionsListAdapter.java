package adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.xplocity.xplocity.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import models.RouteDescription;
import utils.Formatter;
import utils.UI.BlurBuilder;

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
        TextView durationTxtView = (TextView) convertView.findViewById(R.id.duration);
        TextView dateTxtView = (TextView) convertView.findViewById(R.id.date);
        TextView yearTxtView = (TextView) convertView.findViewById(R.id.year);
        //TextView nameTxtView = (TextView) convertView.findViewById(R.id.name);
        TextView locationsUnExploredTxtView = (TextView) convertView.findViewById(R.id.locations_unexplored);
        TextView locationsExploredTxtView = (TextView) convertView.findViewById(R.id.locations_explored);



        Formatter formatter = new Formatter();

        distanceTxtView.setText(formatter.formatDistance(routeDescription.distance));
        dateTxtView.setText(formatter.formatDateToString(routeDescription.date));
        //dateTxtView.setText(formatter.formatDate(routeDescription.date));

        int routeYear = routeDescription.date.getYear();
        if (routeYear != Calendar.getInstance().get(Calendar.YEAR)) {
            yearTxtView.setText(Integer.toString(routeYear));
            yearTxtView.setVisibility(View.VISIBLE);
        }
        else
        {
            yearTxtView.setVisibility(View.GONE);
        }


        durationTxtView.setText(formatter.formatHours(routeDescription.duration/60000));
        //nameTxtView.setText(routeDescription.name);
        locationsExploredTxtView.setText(Integer.toString(routeDescription.locCntExplored));
        locationsUnExploredTxtView.setText(Integer.toString(routeDescription.locCntTotal-routeDescription.locCntExplored));


        View blurredLayout = convertView.findViewById(R.id.routeInfoBlurredLayout);
        ImageView routeImageView = convertView.findViewById(R.id.route_image);

        Bitmap originalBitmap;
        if(routeDescription.image == null)
            originalBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.map_foreground), 0,0, 130, 200);
        else
            originalBitmap = routeDescription.image;

        routeImageView.setImageBitmap(originalBitmap);


        // Flip bitmap horizontally
       /* Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

        Bitmap blurredBitmap = BlurBuilder.blur( mContext, originalBitmap );


        blurredLayout.setBackground(new BitmapDrawable(getResources(), blurredBitmap));*/

        return convertView;
    }




}
