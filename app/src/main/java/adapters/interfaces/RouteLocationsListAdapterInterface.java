package adapters.interfaces;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dmitry on 08.10.17.
 */

public interface RouteLocationsListAdapterInterface {
    void moveCameraPositionBelowLocation(LatLng position);
}
