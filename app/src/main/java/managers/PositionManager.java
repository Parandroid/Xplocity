package managers;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.TtsSpan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import managers.interfaces.PositionManagerInterface;
import models.Location;
import models.Route;

/**
 * Created by dmitry on 20.08.17.
 */

public class PositionManager implements Parcelable {

    public Route route;
    public GeoPoint lastPosition;
    public boolean trackingActive;

    private Date mLastTime;

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
            if (lastPosition != null)
            {
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

            if (!loc.explored && loc.distance <= LOCATION_REACHED_DISTANCE) {
                loc.explored = true;
                mCallback.onLocationReached(loc);
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
        trackingActive = true;
        mLastTime = Calendar.getInstance().getTime();
        lastPosition = null;
    }

    public void stopTracking() {
        trackingActive = false;
    }


    public void updateDuration() {
        if (mLastTime != null) {
            Date curTime = Calendar.getInstance().getTime();

            long diffInMs = curTime.getTime() - mLastTime.getTime();

            route.duration = route.duration + (int) diffInMs;
            mLastTime = curTime;
        }
    }

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
        //builder.excludeFieldsWithoutExposeAnnotation();
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
        Gson gson = new Gson();
        PositionManager c = gson.fromJson(in.readString(), PositionManager.class);

        this.route = c.route;
        this.lastPosition = c.lastPosition;
        this.trackingActive = c.trackingActive;
    }
}
