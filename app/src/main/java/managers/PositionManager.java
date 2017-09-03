package managers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import models.Route;

/**
 * Created by dmitry on 20.08.17.
 */

public class PositionManager implements Parcelable {

    public Route route;
    public LatLng lastPosition;
    public boolean trackingActive;

    public PositionManager() {
        route = new Route();
    }


    public void setPath(ArrayList<LatLng> path) {
        route.path = path;
        lastPosition = path.get(path.size()-1);
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
            return new PositionManager (in);
        }

        public PositionManager [] newArray(int size) {
            return new PositionManager[size];
        }
    };

    /** recreate object from parcel */
    private PositionManager(Parcel in) {
        Gson gson = new Gson();
        PositionManager  c = gson.fromJson(in.readString(), PositionManager.class);

        this.route = c.route;
        this.lastPosition = c.lastPosition;
        this.trackingActive = c.trackingActive;
    }
}
