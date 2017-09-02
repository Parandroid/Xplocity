package models;

import java.util.Date;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDescription {
    public int distance; //distance in meters
    public Date date;
    public int duration; //duration in seconds
    public String name;
    public int loc_cnt_explored;
    public int loc_cnt_total;
    public int id;

    public RouteDescription() {}


}
