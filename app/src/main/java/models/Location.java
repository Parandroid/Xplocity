package models;

import org.joda.time.DateTime;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.Random;

import models.enums.LocationExploreState;

/**
 * Created by dmitry on 20.08.17.
 */



public class Location
        implements Comparable<Location>,
        Serializable {
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

}

