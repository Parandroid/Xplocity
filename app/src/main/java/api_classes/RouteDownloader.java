package api_classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.RouteDownloaderInterface;
import models.Route;

import xml_parsers.XMLRouteParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDownloader extends Loader{
    private RouteDownloaderInterface mCallback;

    public RouteDownloader(RouteDownloaderInterface callback) {
        mCallback = callback;
    }

    public void downloadRoute(int routeId) {
        sendDownloadRequest("http://br-on.ru:3003/api/v1/chains/" + Integer.toString(routeId));
    }

    @Override
    protected void onDownloadResponse(String xml) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLRouteParser parser = new XMLRouteParser();
        Route route;

        try {
            route = parser.parse(stream);
            mCallback.onRouteDownloaded(route);
        }
        catch (Throwable e) {
            mLogger.logError("Error parsing new mRoute: ", e);
        }
    }
}
