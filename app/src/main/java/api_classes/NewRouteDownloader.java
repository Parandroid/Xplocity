package api_classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.NewRouteDownloaderInterface;
import models.Route;
import xml_parsers.XMLLocationsParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class NewRouteDownloader extends Loader {
    private NewRouteDownloaderInterface mCallback;

    public NewRouteDownloader(NewRouteDownloaderInterface callback) {
        mCallback = callback;
    }

    public void downloadNewRoute(Double lat, Double lon, int locCount, double optimalDistance, ArrayList<Integer> locationCategories) {
        String url = generateLocationsUrl(lat, lon, locCount, optimalDistance, locationCategories);
        sendDownloadRequest(url);
    }

    private String generateLocationsUrl(double lat, double lon, int locCount, double optimalDistance, ArrayList<Integer> locationCategories) {

        String url = "http://br-on.ru:3003/api/v1/locations/get_location_list?loc_count=" + Integer.toString(locCount)
                + "&optimal_distance=" + Double.toString(optimalDistance) + "&latitude="
                + Double.toString(lat) + "&longitude=" + Double.toString(lon);

        for (int cat : locationCategories) {
            url = url + "&category[]=" + Integer.toString(cat);
        }

        return url;
    }

    @Override
    protected void onDownloadResponse(String xml) {
        mLogger.logError("RESPONSE");
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLLocationsParser newRouteParser = new XMLLocationsParser();

        Route route = new Route();

        try {
            route.locations = newRouteParser.parse(stream);
            mCallback.onNewRouteDownloaded(route);
        }
        catch (Throwable e) {
            mLogger.logError("Error parsing new mRoute: ", e);
        }
    }
}
