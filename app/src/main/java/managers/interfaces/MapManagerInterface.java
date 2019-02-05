package managers.interfaces;

import models.Location;

/**
 * Created by dmitry on 09.09.17.
 */

public interface MapManagerInterface {
    void onMarkerClicked(Location location);
    void onFocusDropped();
    int getHiddenMapHeight();

}
