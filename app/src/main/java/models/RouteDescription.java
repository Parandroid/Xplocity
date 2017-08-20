package models;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDescription {
    public String date;
    public int loc_cnt_explored;
    public int loc_cnt_total;
    public int id;

    public RouteDescription(int id, String date, int loc_cnt_explored, int loc_cnt_total) {
        this.id = id;
        this.date = date;
        this.loc_cnt_explored = loc_cnt_explored;
        this.loc_cnt_total = loc_cnt_total;
    }
}
