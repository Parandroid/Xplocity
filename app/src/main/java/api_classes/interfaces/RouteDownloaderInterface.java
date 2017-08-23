package api_classes.interfaces;

import java.util.ArrayList;

import models.Route;
import models.RouteDescription;

/**
 * Created by dmitry on 20.08.17.
 */

public interface RouteDownloaderInterface {
    void onRouteDownloaded(Route route);
}
