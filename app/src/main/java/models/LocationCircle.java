package models;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

import managers.PositionManager;

/**
 * Created by dmitry on 31.08.18.
 */

public class LocationCircle
    implements Serializable {

    public GeoPoint center;
    public double raduis;

    public LocationCircle(GeoPoint pCenter, double pRadius) {
        center = pCenter;
        raduis = pRadius;
    }

    public boolean isPointInside(GeoPoint point) {
        double distance = PositionManager.calculateDistance(point, center);
        if (distance <= raduis)
            return true;
        else
            return false;
    }
}
