package models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dmitry on 20.08.17.
 */

public class Location {
    public int id;
    public boolean explored;

    public String name;
    public String description;
    public String address;

    public LatLng position;


    public Location() {

    }
}
