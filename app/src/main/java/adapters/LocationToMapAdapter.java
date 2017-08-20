package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.xplocity.xplocity.R;

import models.Location;


/**
 * Created by dmitry on 03.08.17.
 */


public class LocationToMapAdapter implements GoogleMap.InfoWindowAdapter {

    // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
    // "title" and "snippet".
    private final View mWindow;

    public LocationToMapAdapter(Context context) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.location_map_marker_info, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }


    private void render(Marker marker, View view) {
        TextView txt_name = ((TextView) view.findViewById(R.id.name));
        TextView txt_address = ((TextView) view.findViewById(R.id.address));
        TextView txt_description = ((TextView) view.findViewById(R.id.description));

        Location loc = ((Location) marker.getTag());

        txt_name.setText(loc.name);
        txt_address.setText(loc.address);
        txt_description.setText(loc.description);

    }
}