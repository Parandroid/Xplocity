package api_classes.interfaces;

import android.graphics.Bitmap;

import models.Route;

/**
 * Created by dmitry on 20.08.17.
 */

public interface RouteDescriptionImageDownloaderInterface {
    void onRouteDescriptionImageDownloaded(int routeId, Bitmap image);
}
