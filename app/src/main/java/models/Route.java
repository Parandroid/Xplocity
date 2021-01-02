package models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import models.enums.TravelTypes;
import utils.DateTimeConverter;
import utils.Formatter;

/**
 * Created by dmitry on 20.08.17.
 */


public class Route implements Parcelable {
    public DateTime date;
    public TravelTypes travelType;

    public int loc_cnt_explored;
    public int loc_cnt_total;
    public int id;
    public float distance = 0f; //distance in meter
    public int duration = 0; //duration in milliseconds

    public ArrayList<PathPoint> path;
    public ArrayList<Location> locations;


    public Route() {
        locations = new ArrayList<Location>();
        path = new ArrayList<PathPoint>();
    }

    // deep copy path from source ArrayList
    public void setPath(ArrayList<PathPoint> src) {
        path.clear();
        for (PathPoint pos : src) {
            path.add(pos);
        }
    }


    public ArrayList<GeoPoint> getPathGeopoints() {
        return (ArrayList<GeoPoint>) (ArrayList<?>) path;
    }



    public void pathFromString (String str_route) {
        ArrayList<PathPoint> points = new ArrayList<PathPoint>();
        Formatter formatter = new Formatter();

        for (String str_pos : str_route.split(";")) {
            //Achtung! API возвращает координаты в инвертированном порядке. (Долгота-ширина)
            int firstDelimiterIndex = str_pos.indexOf(" ");
            int secondDelimiterIndex = str_pos.indexOf(" ", firstDelimiterIndex + 1);
            Double latitude = Double.parseDouble(str_pos.substring(0, firstDelimiterIndex));
            Double longitude;
            if (secondDelimiterIndex > 0) {
                longitude = Double.parseDouble(str_pos.substring(firstDelimiterIndex + 1, secondDelimiterIndex));
                DateTime time = formatter.formatStringToUTCDatetime(str_pos.substring(secondDelimiterIndex + 1));
                points.add(new PathPoint(latitude, longitude, time));

            }
            else {
                longitude = Double.parseDouble(str_pos.substring(firstDelimiterIndex + 1));
                points.add(new PathPoint(latitude, longitude));
            }
        }

        this.path = points;
    }



    /// Parcelable
    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(/*Modifier.FINAL, */Modifier.TRANSIENT, Modifier.STATIC);
        builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
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
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());

        Gson gson = builder.create();
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
