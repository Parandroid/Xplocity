package managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xplocity.xplocity.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapAdapter;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
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
import models.enums.TravelTypes;
import utils.ImageManager;
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
    private Marker mFocusedMarker;

    private Map<Location, LocationOnMap> mLocationsOnMap;

    public RouteMapManager(MapView p_map, View context, MapManagerInterface callback, boolean showLocationArrow) {
        super(p_map, context, callback);

        mLocationsOnMap = new HashMap<>();

        mArrow = mContext.findViewById(R.id.location_arrow);

        MapEventsReceiver mReceive = new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                mCallback.onFocusDropped();
                dropFocus();
                InfoWindow.closeAllInfoWindowsOn(mMap);
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

        if (showLocationArrow) {
            calculateNormScreenSize();
            mMap.setMapListener(new MapAdapter() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    updateLocationArrowPosition();

                    return super.onScroll(event);
                }
            });
        }

        hideArrowToLocation();
    }


    public void initMyLocation(TravelTypes travelType) {
        super.initMyLocation();
        setPersonIcon(travelType);
    }

    private void setPersonIcon(TravelTypes travelType) {
        Bitmap bmpIcon;
        switch (travelType) {
            case WALKING:
                bmpIcon = ImageManager.getBitmapFromVectorDrawable(R.drawable.ic_walking);
                break;
            case CYCLING:
                bmpIcon = ImageManager.getBitmapFromVectorDrawable(R.drawable.ic_cycling);
                break;
            default:
                bmpIcon = ImageManager.getBitmapFromVectorDrawable(R.drawable.ic_walking);
        }

        mLocationOverlay.setPersonIcon(bmpIcon);
        mLocationOverlay.setDirectionArrow(bmpIcon, bmpIcon);
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
                if (circle != null) {
                    circle.setVisible(true);
                }
                hideMarker(marker);
                break;
            case POINT_NOT_EXPLORED:
                if (circle != null) {
                    hideCircle(circle);
                }


                if (marker == mFocusedMarker) {
                    setMarkerIconUnexploredFocused(marker);
                } else {
                    setMarkerIconUnexplored(marker);
                }

                showMarker(marker);
                break;
            case POINT_EXPLORED:
                if (circle != null) {
                    hideCircle(circle);
                }

                if (marker == mFocusedMarker) {
                    setMarkerIconExploredFocused(marker);
                } else {
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

    private void hideCircle(Polygon circle) {
        mMap.getOverlays().remove(circle);
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

                mCallback.onMarkerClicked(loc);
                focusOnMarker(marker);

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


    public void dropFocus() {
        if (mFocusedMarker != null) {
            Marker lastFocusedMarker = mFocusedMarker;
            mFocusedMarker = null;

            Location loc = (Location) lastFocusedMarker.getRelatedObject();
            refreshMarkerAndCircle(lastFocusedMarker, mLocationsOnMap.get(loc).circle, loc.exploreState);
            mMap.invalidate();
        }
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
        List<GeoPoint> circle = Polygon.pointsAsCircle(locCircle.center, locCircle.radius);
        final GridPolygon p = new GridPolygon();
        p.setPoints(circle);
        p.setStrokeColor(ResourceGetter.getResources().getColor(R.color.transparent));
        p.setStrokeWidth(0);
        p.setFillColor(ResourceGetter.getResources().getColor(R.color.black));
        p.setPatternBitmap(BitmapFactory.decodeResource(ResourceGetter.getResources(), R.drawable.questions));

        p.setTitle(ResourceGetter.getResources().getString(R.string.location_circle_title));
        p.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mMap));

        mMap.getOverlays().add(1, p); //1, т.к. 0 зарезервирован за Overlay для обработки кликов
        return p;
        //mMap.invalidate();
    }



    public void animateToClosestUnexploredLocation(GeoPoint position) {
        GeoPoint closestLocPosition = findClosestUnexploredLocation(position, false);
        if (closestLocPosition != null) {
            animateCamera(closestLocPosition);
        }
    }


    public void zoomToRouteBoundingBox() {
        BoundingBox boundingBox = BoundingBox.fromGeoPoints(mPolyline.getPoints());
        mMap.zoomToBoundingBox(boundingBox, false);

        IMapController mapController = mMap.getController();
        mapController.zoomToSpan(boundingBox.getLatitudeSpan(), boundingBox.getLongitudeSpan());
        mapController.setCenter(boundingBox.getCenter());
    }

    public Bitmap exportBitmap() {
        mMap.buildDrawingCache();
        Bitmap bitmap=mMap.getDrawingCache().copy(Bitmap.Config.RGB_565, true);
        return bitmap;
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
        GeoPoint closestLoc = findClosestUnexploredLocation(mapCenter, true);

        if (closestLoc != null) {
            mArrow.setVisibility(View.VISIBLE);

            Point locationPosition = new Point();
            mMap.getProjection().toPixels(closestLoc, locationPosition);

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
    private GeoPoint findClosestUnexploredLocation(GeoPoint position, boolean hideVisible) {

        GeoPoint closestLocation = null;
        float smallestDistance = -1;

        for (Location location : mLocationsOnMap.keySet()) {
            if (!location.explored()) {
                GeoPoint targetPos;


                if (location.exploreState == LocationExploreState.POINT_NOT_EXPLORED) {
                    if (!hideVisible || (hideVisible && !isPointVisible(location.position))) {
                        targetPos = location.position;
                    } else {
                        return null;
                    }
                } else {
                    //Circle
                    if (!hideVisible || (hideVisible && !isCircleVisible(location.circle))) {
                        targetPos = location.circle.center;

                    } else {
                        return null;
                    }
                }

                float distance = PositionManager.calculateDistance(position, targetPos);
                if (smallestDistance == -1 || distance < smallestDistance) {
                    closestLocation = targetPos;
                    smallestDistance = distance;

                }
            }
        }

        return closestLocation;
    }


    private boolean isPointVisible(GeoPoint point) {
        Rect currentMapBoundsRect = new Rect();
        Point locationPosition = new Point();

        mMap.getProjection().toPixels(point, locationPosition);
        mMap.getScreenRect(currentMapBoundsRect);


        return currentMapBoundsRect.contains(locationPosition.x, locationPosition.y);
    }


    private boolean isCircleVisible(LocationCircle circle) {
        Rect currentMapBoundsRect = new Rect();
        Point locationPosition = new Point();

        if (isPointVisible(circle.center)) {
            return true;
        } else {
            Rect rect = new Rect();
            mMap.getScreenRect(rect);

            Projection projection = mMap.getProjection();
            Point circleCenter = new Point();
            mMap.getProjection().toPixels(circle.center, circleCenter);
            int circleRadius = (int) projection.metersToPixels(circle.radius);

            return checkRectCircleCollision(circleCenter.x, circleCenter.y, circleRadius, rect);

            /*GeoPoint ne, se, sw, nw;
            BoundingBox visibleRegion = mMap.getBoundingBox();

            ne = new GeoPoint(visibleRegion.getLatNorth(), visibleRegion.getLonEast());
            se = new GeoPoint(visibleRegion.getLatSouth(), visibleRegion.getLonEast());
            sw = new GeoPoint(visibleRegion.getLatSouth(), visibleRegion.getLonWest());
            nw = new GeoPoint(visibleRegion.getLatNorth(), visibleRegion.getLonWest());

            if ((circle.isPointInside(ne)) || (circle.isPointInside(se)) || (circle.isPointInside(sw)) || (circle.isPointInside(nw))) {
                return true;
            }*/
        }
    }


    private boolean checkRectCircleCollision(int circle_x, int circle_y, int circle_r, Rect rect) {
        {
            int distX = Math.abs(circle_x - rect.left-rect.width()/2);
            int distY = Math.abs(circle_y - rect.top-rect.height()/2);

            if (distX > (rect.width()/2 + circle_r)) { return false; }
            if (distY > (rect.height()/2 + circle_r)) { return false; }

            if (distX <= (rect.width()/2)) { return true; }
            if (distY <= (rect.height()/2)) { return true; }

            int dx=distX-rect.width()/2;
            int dy=distY-rect.height()/2;
            return (dx*dx+dy*dy<=(circle_r*circle_r));
        }
    }




    public void rotateArrowToLocation(double x, double y) {
        double angle = -Math.toDegrees(Math.atan2(y, x)) + 90;
        mArrow.setRotation((float) angle);
    }

    private void hideArrowToLocation() {
        mArrow.setVisibility(View.INVISIBLE);
    }

}
