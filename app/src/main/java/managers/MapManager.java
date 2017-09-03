package managers;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
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
import utils.Factory.LogFactory;
import utils.LogLevelGetter;
import utils.Log.Logger;
import utils.ResourceGetter;
import models.Route;

/**
 * Created by dmitry on 20.08.17.
 */

public class MapManager {
    private GoogleMap mMap;
    private Polyline mPolyline;
    private Logger mLogger;

    public static final float DEFAULT_TRACKING_ZOOM = 15f;
    public static final float DEFAULT_OVERVIEW_ZOOM = 10f;

    private Map<Location, Marker> mLocationMarkers;

    public MapManager(GoogleMap p_map) {
        mMap = p_map;
        mLocationMarkers = new HashMap<>();
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
    }

    public void addLocationMarker(Location loc) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(loc.position)
                .title(loc.name)
                .snippet("Address: " + loc.address + System.getProperty("line.separator") + "Description: " + loc.description));

        if (loc.explored) {
            setMarkerIconExplored(marker);
        }

        marker.setTag(loc);
        mLocationMarkers.put(loc, marker);
    }

    public void setLocationMarkerExplored(Location loc) {
        try {
            setMarkerIconExplored(mLocationMarkers.get(loc));
        }
        catch (Exception e) {
            mLogger.logError("Failed to set location marker as explored", e);
        }
    }

    private void setMarkerIconExplored(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
    }

    public void setRoute(Route route) {
        for (Location loc : route.locations) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(loc.position)
                    .title(loc.name)
                    .snippet("Address: " + loc.address + System.getProperty("line.separator") + "Description: " + loc.description));

            if (loc.explored) {
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            m.setTag(loc);
        }

        drawPath(route.path);

        if (!route.locations.isEmpty()) {
            Location loc = route.locations.get(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc.position, 10f));
        }


    }


    public void drawPath(ArrayList<LatLng> path) {
        if (!path.isEmpty()) {
            if (mPolyline != null) {
                mPolyline.setPoints(path);
            }
            else {
                mPolyline = mMap.addPolyline(new PolylineOptions()
                        .addAll(path)
                        .width(ResourceGetter.getInteger("map_polyline_width"))
                        .color(Color.RED));
            }
        }
    }


    public void setTrackingCamera(LatLng position) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,
                DEFAULT_TRACKING_ZOOM), 1000, null);
    }
}
