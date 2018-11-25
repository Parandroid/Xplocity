package models;

import android.graphics.Bitmap;

import org.joda.time.DateTime;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDescription implements Comparable<RouteDescription> {
    public int distance; //distance in meters
    public String distanceMeasure;
    public DateTime date;
    public int duration; //duration in seconds
    public String name;
    public String travelType;

    public int locCntExplored;
    public int locCntTotal;
    public int id;
    public Bitmap image;

    public RouteDescription() {}

    @Override
    public int compareTo(RouteDescription routeDescription) {

        if (this.date.getMillis() < routeDescription.date.getMillis()) {
            return 1;
        }
        else if (this.date.getMillis() > routeDescription.date.getMillis()) {
            return -1;
        }
        else {
            return 0;
        }

    }


}
