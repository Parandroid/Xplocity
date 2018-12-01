package managers;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xplocity.xplocity.R;

import org.osmdroid.events.MapAdapter;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import managers.interfaces.MapManagerInterface;
import models.Location;
import models.LocationCircle;
import models.Route;
import models.enums.LocationExploreState;
import utils.ResourceGetter;
import utils.UI.GridPolygon;

/**
 * Created by dmitry on 20.08.17.
 */


class LocationOnMap {
    Marker marker;
    Polygon circle;

    public LocationOnMap(Marker pMarker, Polygon pCircle) {
        marker = pMarker;
        circle = pCircle;
    }
}

public class RouteMapManager extends MapManager {
    private Polyline mPolyline;
    private ImageView mArrow;
    Marker mFocusedMarker;


    private MapManagerInterface mCallback;


    /*private Map<Location, Marker> mLocationMarkers;
    private Map<Location, Polygon> mLocationCircles;*/
    private Map<Location, LocationOnMap> mLocationsOnMap;

    public RouteMapManager(MapView p_map, View context, MapManagerInterface callback) {
        super(p_map, context);

        mCallback = callback;

        /*mLocationMarkers = new HashMap<>();
        mLocationCircles = new HashMap<>();*/
        mLocationsOnMap = new HashMap<>();

        mArrow = mContext.findViewById(R.id.location_arrow);

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



    public void updateLocationOnMap(Location loc) {
        LocationOnMap locOnMap = mLocationsOnMap.get(loc);

        Marker marker = locOnMap.marker;

        Polygon circle = null;
        if (loc.hasCircle)
            circle = locOnMap.circle;

        refreshMarkerAndCircle(marker, circle, loc.exploreState);
    }

    private void refreshMarkerAndCircle(Marker marker, Polygon circle, LocationExploreState locState) {
        switch (locState) {
            case CIRCLE:
                if (circle != null)
                    circle.setVisible(true);
                hideMarker(marker);
                break;
            case POINT_NOT_EXPLORED:
                if (circle != null)
                    circle.setVisible(false);

                if (marker == mFocusedMarker) {
                    setMarkerIconUnexploredFocused(marker);
                }
                else {
                    setMarkerIconUnexplored(marker);
                }

                showMarker(marker);
                break;
            case POINT_EXPLORED:
                if (circle != null)
                    circle.setVisible(false);

                if (marker == mFocusedMarker) {
                    setMarkerIconExploredFocused(marker);
                }
                else {
                    setMarkerIconExplored(marker);
                }
                showMarker(marker);
        }
    }


    private void showMarker(Marker marker) {
        if (!mMap.getOverlays().contains(marker))
            mMap.getOverlays().add(marker);
    }

    private void hideMarker(Marker marker) {
        if (mMap.getOverlays().contains(marker))
            mMap.getOverlays().remove(marker);
    }


    private void setMarkerIconUnexplored(Marker marker) {
        marker.setIcon(ContextCompat.getDrawable(mContext.getContext(), R.drawable.location_unexplored_marker));
    }

    private void setMarkerIconExplored(Marker marker) {
        marker.setIcon(ContextCompat.getDrawable(mContext.getContext(), R.drawable.location_explored_marker));
    }

    private void setMarkerIconExploredFocused(Marker marker) {
        marker.setIcon(ContextCompat.getDrawable(mContext.getContext(), R.drawable.location_explored_focused_marker));
    }

    private void setMarkerIconUnexploredFocused(Marker marker) {
        marker.setIcon(ContextCompat.getDrawable(mContext.getContext(), R.drawable.location_unexplored_focused_marker));
    }


    public void setRoute(Route route) {
        drawPath(route.path);

        for (Location loc : route.locations) {
            addLocationToMap(loc);
        }

    }


    public void updateLocationMarkers() {
        for (Map.Entry<Location, LocationOnMap> entry : mLocationsOnMap.entrySet()) {
            LocationOnMap locOnMap = entry.getValue();
            Location loc = entry.getKey();

            Polygon circle = null;
            if (loc.hasCircle)
                circle = locOnMap.circle;

            refreshMarkerAndCircle(locOnMap.marker, circle, loc.exploreState);
        }
    }


    private void addLocationToMap(Location loc) {
        Marker marker = new Marker(mMap);
        marker.setPosition(loc.position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(loc.name);
        marker.setSubDescription(loc.address);
        marker.setSnippet(loc.description);
        marker.setRelatedObject(loc);

        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {

                focusOnMarker(marker);
                mCallback.onMarkerClicked(loc);

                return true;
            }
        });

        Polygon circle = null;
        if (loc.hasCircle)
            circle = drawCircle(loc.circle);

        refreshMarkerAndCircle(marker, circle, loc.exploreState);

        mLocationsOnMap.put(loc, new LocationOnMap(marker, circle));
        //mMap.getOverlays().add(marker);
    }

    public void focusOnLocation(Location loc) {
        LocationOnMap locOnMap = mLocationsOnMap.get(loc);
        Marker marker = locOnMap.marker;
        focusOnMarker(marker);
    }

    private void focusOnMarker(Marker marker) {
        Marker lastFocusedMarker = mFocusedMarker;
        mFocusedMarker = marker;

        if (lastFocusedMarker != null) {
            Location loc = (Location) lastFocusedMarker.getRelatedObject();
            refreshMarkerAndCircle(lastFocusedMarker, mLocationsOnMap.get(loc).circle, loc.exploreState);
        }

        Location loc = (Location) mFocusedMarker.getRelatedObject();
        refreshMarkerAndCircle(mFocusedMarker, mLocationsOnMap.get(loc).circle, loc.exploreState);

        animateCamera(loc.position);
        mMap.invalidate();

        mFocusedMarker = marker;
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


    private Polygon drawCircle(LocationCircle locCircle) {
        List<GeoPoint> circle = Polygon.pointsAsCircle(locCircle.center, locCircle.raduis);
        final GridPolygon p = new GridPolygon();
        p.setPoints(circle);
        p.setStrokeColor(ResourceGetter.getResources().getColor(R.color.transparent));
        p.setStrokeWidth(0);
        p.setFillColor(ResourceGetter.getResources().getColor(R.color.black));
        p.setPatternBitmap(BitmapFactory.decodeResource(ResourceGetter.getResources(), R.drawable.questions));

        mMap.getOverlays().add(p);
        return p;
        //mMap.invalidate();
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

        for (Location location : mLocationsOnMap.keySet()) {
            if (!location.explored()) {
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
