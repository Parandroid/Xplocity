package managers;

import android.view.View;
import android.widget.ImageButton;

import com.xplocity.xplocity.BuildConfig;
import com.xplocity.xplocity.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Map;

import models.Location;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;

/**
 * Created by dmitry on 20.08.17.
 */

public class MapManager {
    protected MapView mMap;
    protected MyLocationNewOverlay mLocationOverlay;

    protected Logger mLogger;
    protected View mContext;

    public static final int DEFAULT_TRACKING_ZOOM = 16;
    public static final int DEFAULT_OVERVIEW_ZOOM = 11;

    private Map<Location, Marker> mLocationMarkers;

    public MapManager(MapView p_map, View context) {
        mMap = p_map;
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

        /*ImageButton btn;
        if (mContext instanceof Activity) {
            btn = ((Activity) mContext).findViewById(R.id.btn_goto_current_location);
        }
        else if (mContext instanceof ContextThemeWrapper) {
            btn = ((ContextThemeWrapper) mContext).get (R.id.btn_goto_current_location);
        }*/

        //ImageButton btn = ((Activity) mContext).findViewById(R.id.btn_goto_current_location);
        ImageButton btn = mContext.findViewById(R.id.btn_goto_current_location);
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
    }



    public void initMyLocation() {
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext.getContext()), mMap);
        mLocationOverlay.enableMyLocation();
        mMap.getOverlays().add(mLocationOverlay);

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

    public void setCamera(GeoPoint position) {
        mMap.getController().setCenter(position);
    }

    private void animateCamera(GeoPoint position, int zoom) {
        mMap.getController().setZoom(zoom);
        mMap.getController().animateTo(position);
    }

    public void animateCamera(GeoPoint position) {
        mMap.getController().animateTo(position);
    }

}
