package managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xplocity.xplocity.BuildConfig;
import com.xplocity.xplocity.R;
import com.xplocity.xplocity.RouteLocationList;
import com.xplocity.xplocity.RouteNewActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapAdapter;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.ScrollEvent;
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

import adapters.LocationMarkerInfoWindow;
import models.Location;
import utils.Factory.LogFactory;
import utils.LogLevelGetter;
import utils.Log.Logger;
import utils.ResourceGetter;
import models.Route;

/**
 * Created by dmitry on 20.08.17.
 */

public class routeMapManager extends mapManager {
    private Polyline mPolyline;
    private ImageView mArrow;

    private View mBottomSheet;
    private RouteLocationList mLocationInfoFragment;


    private Map<Location, Marker> mLocationMarkers;

    public routeMapManager(MapView p_map, View locationBottomSheetView, RouteLocationList locationInfoFragment, View context) {
        super(p_map, context);

        mLocationMarkers = new HashMap<>();
        mArrow = mContext.findViewById(R.id.location_arrow);
        mBottomSheet = locationBottomSheetView;
        mLocationInfoFragment = locationInfoFragment;

        MapEventsReceiver mReceive = new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(mMap);
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

        //mMap.setInfoWindowAdapter(new LocationToMapAdapter(mContext));

        calculateNormScreenSize();
        mMap.setMapListener(new MapAdapter() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                updateLocationArrowPosition();

                return super.onScroll(event);
            }
        });

        hideArrowToLocation();
    }


    public void setLocationMarkerExplored(Location loc) {
        try {
            setMarkerIconExplored(mLocationMarkers.get(loc));
        } catch (Exception e) {
            mLogger.logError("Failed to set location marker as explored", e);
        }
    }

    private void setMarkerIconExplored(Marker marker) {
        marker.setIcon(ContextCompat.getDrawable(mContext.getContext(), R.drawable.location_explored_marker));
    }

    public void setRoute(Route route) {
        drawPath(route.path);

        for (Location loc : route.locations) {
            addLocationMarker(loc);
        }

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
        marker.setRelatedObject(loc);

        if (mBottomSheet != null) {
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    Location loc = (Location) marker.getRelatedObject();

                    BottomSheetBehavior.from(mBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    mLocationInfoFragment.showLocationInfo();

                    ((TextView) mBottomSheet.findViewById(R.id.name)).setText(loc.name);

                    return false;
                }
            });
        }
        else {
            InfoWindow infoWindow = new LocationMarkerInfoWindow(R.layout.location_map_marker_info, mMap);
            marker.setInfoWindow(infoWindow);
        }


        if (loc.explored) {
            setMarkerIconExplored(marker);
        } else {
            marker.setIcon(ContextCompat.getDrawable(mContext.getContext(), R.drawable.location_unexplored_marker));
        }

        mLocationMarkers.put(loc, marker);
        mMap.getOverlays().add(marker);
    }


    public void drawPath(ArrayList<GeoPoint> path) {
        if (!path.isEmpty()) {
            if (mPolyline != null) {
                mPolyline.setPoints(path);
            } else {
                mPolyline = new Polyline();
                mPolyline.setWidth(ResourceGetter.getInteger("map_polyline_width"));
                mPolyline.setColor(Color.RED);
                mPolyline.setPoints(path);
                mMap.getOverlayManager().add(mPolyline);
            }
        }
    }


    /*******   Location arrow   **********/

    private int mScreenHeight;
    private int mScreenWidth;
    private double mNormScreenHeight;
    private double mNormScreenWidth;
    private Point mMapCenterPoint;

    private void calculateNormScreenSize() {
        mScreenHeight = mMap.getMeasuredHeight();
        mScreenWidth = mMap.getMeasuredWidth();
        double screenDiag = Math.sqrt(mScreenHeight * mScreenHeight + mScreenWidth * mScreenWidth);
        mNormScreenHeight = mScreenHeight / screenDiag;
        mNormScreenWidth = mScreenWidth / screenDiag;

        mMapCenterPoint = new Point(mScreenWidth / 2, mScreenHeight / 2);

    }


    public void updateLocationArrowPosition() {
        GeoPoint mapCenter = (GeoPoint) mMap.getMapCenter();
        Location closestLoc = findTargetLocation(mapCenter);

        if (closestLoc != null) {
            mArrow.setVisibility(View.VISIBLE);

            Point locationPosition = new Point();
            mMap.getProjection().toPixels(closestLoc.position, locationPosition);

            int distanceHorizont = locationPosition.x - mMapCenterPoint.x;
            int distanceVertical = -(locationPosition.y - mMapCenterPoint.y);

            double distance = Math.sqrt(distanceHorizont * distanceHorizont + distanceVertical * distanceVertical);
            double normDistanceHorizon = distanceHorizont / distance;
            double normDistanceVertical = distanceVertical / distance;

            RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) mArrow.getLayoutParams();

            relativeParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            relativeParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            relativeParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            relativeParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            relativeParams.setMargins(0, 0, 0, 0);


            if ((normDistanceVertical >= -1 && normDistanceVertical <= -mNormScreenHeight) && (normDistanceHorizon > -mNormScreenWidth && normDistanceHorizon < mNormScreenWidth)) {
                relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                relativeParams.leftMargin = (int) (mScreenWidth / 2 * (1 + normDistanceHorizon / mNormScreenWidth));
            } else if ((normDistanceVertical <= 1 && normDistanceVertical > mNormScreenHeight) && (normDistanceHorizon > -mNormScreenWidth && normDistanceHorizon < mNormScreenWidth)) {
                relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                relativeParams.leftMargin = (int) (mScreenWidth / 2 * (1 + normDistanceHorizon / mNormScreenWidth));

            } else if (normDistanceHorizon <= 0 && (normDistanceVertical > -mNormScreenHeight && normDistanceVertical < mNormScreenHeight)) {
                relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                relativeParams.topMargin = mScreenHeight - (int) (mScreenHeight / 2 * (1 + normDistanceVertical / mNormScreenHeight));
            } else if (normDistanceHorizon > 0 && (normDistanceVertical > -mNormScreenHeight && normDistanceVertical < mNormScreenHeight)) {
                relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                relativeParams.topMargin = mScreenHeight - (int) (mScreenHeight / 2 * (1 + normDistanceVertical / mNormScreenHeight));

            }

            rotateArrowToLocation(normDistanceHorizon, normDistanceVertical);
            mArrow.requestLayout();

        } else {
            hideArrowToLocation();
        }
    }


    // Find closest unexplored location if no such locations are shown on current map view.
    // if any location is shown, return null
    private Location findTargetLocation(GeoPoint position) {

        Location closestLocation = null;
        float smallestDistance = -1;

        for (Location location : mLocationMarkers.keySet()) {
            if (!location.explored) {
                if (!isPointVisible(location.position)) {

                    float distance = PositionManager.calculateDistance(position, location.position);
                    if (smallestDistance == -1 || distance < smallestDistance) {
                        closestLocation = location;
                        smallestDistance = distance;
                    }
                } else {
                    return null;
                }
            }
        }

        return closestLocation;
    }


    private boolean isPointVisible(GeoPoint point) {
        Rect currentMapBoundsRect = new Rect();
        Point locationPosition = new Point();
        //GeoPoint deviceLocation = new GeoPoint((int) (bestCurrentLocation.getLatitude() * 1000000.0), (int) (bestCurrentLocation.getLongitude() * 1000000.0));

        mMap.getProjection().toPixels(point, locationPosition);
        mMap.getScreenRect(currentMapBoundsRect);


        return currentMapBoundsRect.contains(locationPosition.x, locationPosition.y);

    }

    public void rotateArrowToLocation(double x, double y) {
        double angle = -Math.toDegrees(Math.atan2(y, x)) + 90;
        mArrow.setRotation((float) angle);
    }

    private void hideArrowToLocation() {
        mArrow.setVisibility(View.INVISIBLE);
    }

}
