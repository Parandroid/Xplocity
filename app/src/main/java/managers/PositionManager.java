package managers;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.TtsSpan;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
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
    public LatLng lastPosition;
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


    public void addPosToPath(LatLng pos) {
        if (pos != null) {
            route.distance = route.distance + calculateDistance(pos, lastPosition);
            updateDuration();

            check_location_reached(pos);
            route.path.add(pos);
            lastPosition = pos;

        }
    }


    private void check_location_reached(LatLng position) {
        for (Location loc : route.locations) {
            if (!loc.explored) {
                float[] results = new float[2];
                android.location.Location.distanceBetween(loc.position.latitude, loc.position.longitude, position.latitude, position.longitude, results);
                if (results[0] <= LOCATION_REACHED_DISTANCE) {
                    loc.explored = true;
                    mCallback.onLocationReached(loc);
                }
            }
        }
    }


    public void startTracking() {
        route.distance = 0;
        route.duration = 0;
        trackingActive = true;
        mLastTime = Calendar.getInstance().getTime();
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

    public float calculateDistance(LatLng pos1, LatLng pos2) {
        float[] results = new float[2];
        android.location.Location.distanceBetween(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, results);

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
