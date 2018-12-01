package managers;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import app.XplocityApplication;

/**
 * Created by dmitry on 17.03.18.
 */

public class reportLocationMapManager extends MapManager {
    private Marker mMarker;

    public reportLocationMapManager(MapView p_map, View context) {
        super(p_map, context);

        createMarker();
        MapEventsReceiver mReceive = new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                mMarker.setPosition(p);
                mMap.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                //DO NOTHING FOR NOW:
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mReceive);
        mMap.getOverlays().add(0, mapEventsOverlay);

    }

    public GeoPoint getMarkerPosition() {
        return mMarker.getPosition();
    }


    private void createMarker() {
        mMarker = new Marker(mMap);
        mMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMap.getOverlays().add(mMarker);
    }


    @Override
    public void initMyLocation() {
        super.initMyLocation();

        LocationManager locationManager = (LocationManager) XplocityApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (loc != null) {
                GeoPoint lastPos = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                setTrackingCamera(lastPos);
                mMarker.setPosition(lastPos);
            }
        }
        catch (SecurityException e) {

        }
    }

}
