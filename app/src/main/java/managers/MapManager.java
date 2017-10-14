package managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.xplocity.xplocity.BuildConfig;
import com.xplocity.xplocity.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

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
    private MapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private Polyline mPolyline;
    private Logger mLogger;
    private Context mContext;

    public static final int DEFAULT_TRACKING_ZOOM = 16;
    public static final int DEFAULT_OVERVIEW_ZOOM = 11;

    private Map<Location, Marker> mLocationMarkers;

    public MapManager(MapView p_map, Context context) {
        mMap = p_map;
        mLocationMarkers = new HashMap<>();
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        mContext = context;

        initMap();

        //mMap.setInfoWindowAdapter(new LocationToMapAdapter(mContext));
    }


    private void initMap() {
        //Create themap
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mMap.setMultiTouchControls(true);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setTilesScaledToDpi(true);

        ImageButton btn = ((Activity) mContext).findViewById(R.id.btn_goto_current_location);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationOverlay != null) {
                    GeoPoint currentPos = mLocationOverlay.getMyLocation();
                    if (currentPos != null)
                        animateTrackingCamera(mLocationOverlay.getMyLocation());
                }
            }

        });

        MapEventsReceiver mReceive = new MapEventsReceiver() {

            @Override public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(mMap);
                return true;
            }

            @Override public boolean longPressHelper(GeoPoint p) {
                //DO NOTHING FOR NOW:
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mReceive);
        mMap.getOverlays().add(0, mapEventsOverlay);
    }


    public void initMyLocation() {
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), mMap);
        mLocationOverlay.enableMyLocation();
        mMap.getOverlays().add(mLocationOverlay);
    }


    public void setLocationMarkerExplored(Location loc) {
        try {
            setMarkerIconExplored(mLocationMarkers.get(loc));
        } catch (Exception e) {
            mLogger.logError("Failed to set location marker as explored", e);
        }
    }

    private void setMarkerIconExplored(Marker marker) {
        marker.setIcon(ContextCompat.getDrawable(mContext, R.drawable.location_explored_marker));
    }

    public void setRoute(Route route) {
        for (Location loc : route.locations) {
            addLocationMarker(loc);
        }

        drawPath(route.path);
    }


    public void updateLocationMarkers() {
        for (Map.Entry<Location, Marker> entry : mLocationMarkers.entrySet()) {
            if (entry.getKey().explored) {
                setMarkerIconExplored(entry.getValue());
            }
        }
    }


    private void addLocationMarker(Location loc) {

        Marker marker = new Marker(mMap);
        marker.setPosition(loc.position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(loc.name);
        marker.setSubDescription(loc.address);
        marker.setSnippet(loc.description);


        if (loc.explored) {
            setMarkerIconExplored(marker);
        } else {
            marker.setIcon(ContextCompat.getDrawable(mContext, R.drawable.location_unexplored_marker));
        }

        marker.setRelatedObject(loc);
        mLocationMarkers.put(loc, marker);
        mMap.getOverlays().add(marker);
    }


    public void drawPath(ArrayList<GeoPoint> path) {
        if (!path.isEmpty()) {
            if (mPolyline != null) {
                mPolyline.setPoints(path);
            }
            else {
                mPolyline = new Polyline();
                mPolyline.setWidth(ResourceGetter.getInteger("map_polyline_width"));
                mPolyline.setColor(Color.RED);
                mPolyline.setPoints(path);
                mMap.getOverlayManager().add(mPolyline);
            }
        }
    }


    public void animateTrackingCamera(GeoPoint position) {
        animateCamera(position, DEFAULT_TRACKING_ZOOM);
    }

    public void animateOverviewCamera(GeoPoint position) {
        animateCamera(position, DEFAULT_OVERVIEW_ZOOM);
    }

    public void setTrackingCamera(GeoPoint position) {
        setCamera(position, DEFAULT_TRACKING_ZOOM);
    }

    public void setOverviewCamera(GeoPoint position) {
        setCamera(position, DEFAULT_OVERVIEW_ZOOM);
    }


    private void setCamera(GeoPoint position, int zoom) {
        mMap.getController().setZoom(zoom);
        mMap.getController().setCenter(position);
    }

    private void animateCamera(GeoPoint position, int zoom) {
        mMap.getController().setZoom(zoom);
        mMap.getController().animateTo(position);
    }

}
