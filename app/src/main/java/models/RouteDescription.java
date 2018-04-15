package models;

import java.util.Date;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDescription implements Comparable<RouteDescription> {
    public int distance; //distance in meters
    public String distanceMeasure;
    public Date date;
    public int duration; //duration in seconds
    public String name;
    public int locCntExplored;
    public int locCntTotal;
    public int id;

    public RouteDescription() {}

    @Override
    public int compareTo(RouteDescription routeDescription) {

        if (this.date.before(routeDescription.date)) {
            return 1;
        }
        else if (this.date.after(routeDescription.date)) {
            return -1;
        }
        else {
            return 0;
        }

    }


}
