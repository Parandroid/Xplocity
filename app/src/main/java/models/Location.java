package models;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;

/**
 * Created by dmitry on 20.08.17.
 */

public class Location implements Comparable<Location> {
    public int id;
    public boolean explored;

    public String name;
    public String description;
    public String address;

    public GeoPoint position;
    public float distance; //distance to location in meters

    public Location() {

    }

    @Override
    public int compareTo(Location loc) {

        if (this.explored && !loc.explored) {
            return 1;
        }
        else if (!this.explored && loc.explored) {
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
