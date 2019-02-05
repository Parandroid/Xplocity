package models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Random;

import models.enums.LocationExploreState;
import utils.DateTimeConverter;

/**
 * Created by dmitry on 20.08.17.
 */



public class Location
        implements Comparable<Location>,
        Serializable,
        Parcelable {
    public int id;
    public LocationExploreState exploreState;

    public String name;
    public String description;
    public String address;

    public DateTime dateReached;

    public GeoPoint position;
    public float distance; //distance to location in meters

    public LocationCircle circle;
    public boolean hasCircle; //true - for active routes, false - when viewing old routes

    private final int LOCATION_CIRCLE_RADIUS = 300;

    public Location() {
        //generateLocationCircle(300, 150);
    }


    public boolean explored() {
        return exploreState == LocationExploreState.POINT_EXPLORED ? true : false;
    }


    public void setStateCircle() {
        exploreState = LocationExploreState.CIRCLE;
        generateLocationCircle(LOCATION_CIRCLE_RADIUS, 150);
    }


    public void setStateUnexplored() {
        exploreState = LocationExploreState.POINT_NOT_EXPLORED;
    }

    public void setStateExplored() {
            exploreState = LocationExploreState.POINT_EXPLORED;
    }


    private void generateLocationCircle(float pRadius, float pMaxOffset) {
        if (circle == null) {
            final float r_earth = 6371000.0f;

            Random r = new Random();
            double dx = pMaxOffset * (-1 + 2 * r.nextDouble());
            double dy = pMaxOffset * (-1 + 2 * r.nextDouble());


            double new_latitude = position.getLatitude() + (dy / r_earth) * (180 / Math.PI);
            double new_longitude = position.getLongitude() + (dx / r_earth) * (180 / Math.PI) / Math.cos(position.getLatitude() * Math.PI / 180);

            circle = new LocationCircle(new GeoPoint(new_latitude, new_longitude), pRadius);
        }
    }



    @Override
    public int compareTo(Location loc) {

        if (this.explored() && !loc.explored()) {
            return 1;
        }
        else if (!this.explored() && loc.explored()) {
            return -1;
        }
        else if (this.distance > loc.distance) {
            return 1;
        }
        else if (this.distance < loc.distance) {
            return -1;
        }
        else {
            return 0;
        }

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

    public static final Parcelable.Creator<Location> CREATOR
            = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    /** recreate object from parcel */
    private Location(Parcel in) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());

        Gson gson = builder.create();
        Location loc = gson.fromJson(in.readString(), Location.class);
        this.id = loc.id;
        this.exploreState = loc.exploreState;
        this.name = loc.name;
        this.description = loc.description;
        this.address = loc.address;
        this.dateReached = loc.dateReached;
        this.position = loc.position;
        this.distance = loc.distance;
        this.circle = loc.circle;
        this.hasCircle = loc.hasCircle;
    }

}

