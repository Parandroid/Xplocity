package managers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Modifier;
import java.util.Collections;

import managers.interfaces.PositionManagerInterface;
import models.Location;
import models.Route;
import utils.DateTimeConverter;

/**
 * Created by dmitry on 20.08.17.
 */

public class PositionManager implements Parcelable {

    public Route route;
    public GeoPoint lastPosition;

    private DateTime mLastTime;

    private transient PositionManagerInterface mCallback;

    private static final int LOCATION_REACHED_DISTANCE = 100;

    public PositionManager(PositionManagerInterface callback) {
        route = new Route();
        mCallback = callback;
    }

    public void setCallback(PositionManagerInterface callback) {
        mCallback = callback;
    }


    public void addPosToPath(GeoPoint pos) {
        if (pos != null) {
            if (lastPosition != null) {
                route.distance = route.distance + calculateDistance(pos, lastPosition);
                updateDuration();

                check_location_reached(pos);
                sortLocationsByDistance();
            }
            route.path.add(pos);
            lastPosition = pos;
        }
    }


    private void check_location_reached(GeoPoint position) {
        for (Location loc : route.locations) {
            loc.distance = calculateDistanceToLocation(position, loc);

            if (!loc.explored()) {

                if (loc.distance <= LOCATION_REACHED_DISTANCE){
                    loc.setStateExplored();
                    loc.dateReached = DateTime.now();
                    route.loc_cnt_explored = route.loc_cnt_explored + 1;
                    mCallback.onLocationReached(loc);
                }
                else if (loc.circle.isPointInside(position)) {
                    loc.setStateUnexplored();
                    mCallback.onLocationCircleReached(loc);
                }
            }
        }
    }

    private float calculateDistanceToLocation(GeoPoint position, Location location) {
        return calculateDistance(position, location.position);
    }

    private void sortLocationsByDistance() {
        Collections.sort(route.locations);
    }


    public void startTracking() {
        route.distance = 0;
        route.duration = 0;
        mLastTime = DateTime.now();
        lastPosition = null;
    }


    public void updateDuration() {
        if (mLastTime != null) {
            DateTime curTime = DateTime.now();;

            long diffInMs = curTime.getMillis() - mLastTime.getMillis();

            route.duration = route.duration + (int) diffInMs;
            mLastTime = curTime;
        }
    }

    //Return distance in meters between two points
    public static float calculateDistance(GeoPoint pos1, GeoPoint pos2) {
        float[] results = new float[2];
        android.location.Location.distanceBetween(pos1.getLatitude(), pos1.getLongitude(), pos2.getLatitude(), pos2.getLongitude(), results);

        return results[0];

    }


    // Parcelable
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(/*Modifier.FINAL, */Modifier.TRANSIENT, Modifier.STATIC);
        builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
        Gson gson = builder.create();

        String json = gson.toJson(this);
        out.writeString(json);
    }

    public static final Parcelable.Creator<PositionManager> CREATOR
            = new Parcelable.Creator<PositionManager>() {
        public PositionManager createFromParcel(Parcel in) {
            return new PositionManager(in);
        }

        public PositionManager[] newArray(int size) {
            return new PositionManager[size];
        }
    };

    /**
     * recreate object from parcel
     */
    private PositionManager(Parcel in) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
        Gson gson = builder.create();

        PositionManager c = gson.fromJson(in.readString(), PositionManager.class);

        this.route = c.route;
        this.lastPosition = c.lastPosition;
        this.mLastTime = c.mLastTime;
    }
}
