package api_classes.interfaces;

import java.util.ArrayList;

import models.RouteDescription;

/**
 * Created by dmitry on 20.08.17.
 */

public interface RouteDescriptionsDownloaderInterface {
    void onRouteDescriptionsDownload(ArrayList<RouteDescription> p_route_descriptions);
}
