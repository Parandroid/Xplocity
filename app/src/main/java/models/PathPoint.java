package models;

import org.joda.time.DateTime;
import org.osmdroid.util.GeoPoint;

public class PathPoint extends GeoPoint {
    private DateTime time;

    public PathPoint(GeoPoint aGeopoint, DateTime aTime) {
        super(aGeopoint);
        time = aTime;
    }

    public PathPoint(final double aLatitude, final double aLongitude) {
        super(aLatitude, aLongitude);
    }

    public PathPoint(final double aLatitude, final double aLongitude, DateTime aTime) {
        super(aLatitude, aLongitude);
        time = aTime;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }
}
