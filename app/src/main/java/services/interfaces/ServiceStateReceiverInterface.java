package services.interfaces;

import models.Location;

/**
 * Created by dmitry on 02.09.17.
 */

public interface ServiceStateReceiverInterface {
    void onPositionChanged();
    void onLocationReached(int locationId);
    void onLocationCircleReached(int locationId);
}
