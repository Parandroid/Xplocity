package api_classes.interfaces;

import models.Route;

/**
 * Created by dmitry on 06.09.17.
 */

public interface RouteUploaderInterface {
    void onSuccessUploadRoute(Route route);
    void onErrorUploadRoute(String errorText);
}
