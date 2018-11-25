package models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by dmitry on 20.08.17.
 */

public class Route implements Parcelable {
    public String date;
    public String travelType;

    public int loc_cnt_explored;
    public int loc_cnt_total;
    public int id;
    public float distance = 0f; //distance in meter
    public int duration = 0; //duration in milliseconds

    public ArrayList<GeoPoint> path;
    public ArrayList<Location> locations;


    public Route() {

        locations = new ArrayList<Location>();
        path = new ArrayList<GeoPoint>();
    }

    // deep copy path from source ArrayList
    public void setPath(ArrayList<GeoPoint> src) {
        path.clear();
        for (GeoPoint pos : src) {
            path.add(pos);
        }

    }

    public static ArrayList<GeoPoint> string_to_route(String str_route) {
        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

        for (String str_pos : str_route.split(";")) {
            //Achtung! API возвращает координаты в инвертированном порядке. (Долгота-ширина)
            Double latitude = Double.parseDouble(str_pos.substring(0, str_pos.indexOf(" ")));
            Double longitude = Double.parseDouble(str_pos.substring(str_pos.indexOf(" ")+1, str_pos.length()));

            points.add(new GeoPoint(latitude, longitude));
        }

        return points;
    }



    /// Parcelable

    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(/*Modifier.FINAL, */Modifier.TRANSIENT, Modifier.STATIC);
        //builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.create();
        String json = gson.toJson(this);
        out.writeString(json);
    }

    public static final Parcelable.Creator<Route> CREATOR
            = new Parcelable.Creator<Route>() {
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    /** recreate object from parcel */
    private Route(Parcel in) {
        Gson gson = new Gson();
        Route c = gson.fromJson(in.readString(), Route.class);
        this.id = c.id;
        this.date = c.date;
        this.path = c.path;
        this.locations = c.locations;
        this.loc_cnt_explored = c.loc_cnt_explored;
        this.loc_cnt_total = c.loc_cnt_total;
        this.distance = c.distance;
        this.duration = c.duration;
    }
}
