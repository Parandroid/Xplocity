package managers;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import models.Location;

/**
 * Created by dmitry on 20.08.17.
 */

public class MapManager {
    private GoogleMap map;
    private Polyline polyline;


    public static final float DEFAULT_TRACKING_ZOOM = 15f;
    public static final float DEFAULT_OVERVIEW_ZOOM = 10f;



    private Map<Location, Marker> location_markers;

    public MapManager(GoogleMap p_map) {
        map = p_map;
        location_markers = new HashMap<Location, Marker>();
    }


    public void add_location_marker(Location loc) {
        Marker m = map.addMarker(new MarkerOptions()
                .position(loc.position)
                .title(loc.name)
                .snippet("Address: " + loc.address + System.getProperty("line.separator") + "Description: " + loc.description));

        if (loc.explored)
            set_marker_icon_explored(m);

        m.setTag(loc);

        location_markers.put(loc, m);
    }


    public void set_location_marker_explored(Location loc) {
        try {
            set_marker_icon_explored(location_markers.get(loc));
        }
        catch (Exception e)
        {
            Log.e("Xplocity,MapManager", e.getMessage());
        }
    }


    public void draw_route(ArrayList<LatLng> path) {
        if (polyline == null) {
            polyline = map.addPolyline(new PolylineOptions()
                    .addAll(path)
                    .width(5)
                    .color(Color.RED));
        }
        else {
            polyline.setPoints(path);
        }
    }



    private void set_marker_icon_explored(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
    }


}
