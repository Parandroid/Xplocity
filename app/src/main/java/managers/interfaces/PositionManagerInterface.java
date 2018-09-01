package managers.interfaces;

import models.Location;

/**
 * Created by dmitry on 09.09.17.
 */

public interface PositionManagerInterface {
    void onLocationReached(Location location);
    void onLocationCircleReached(Location location);
}
