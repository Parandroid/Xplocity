package api_classes;

import com.xplocity.xplocity.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.NewRouteDownloaderInterface;
import app.XplocityApplication;
import models.Route;
import xml_parsers.XMLLocationsParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class NewRouteDownloader extends Loader {
    private NewRouteDownloaderInterface mCallback;
    private static final String API_method = "/locations/get_location_list";

    public NewRouteDownloader(NewRouteDownloaderInterface callback) {
        mCallback = callback;
    }

    public void downloadNewRoute(Double lat, Double lon, int locCount, double optimalDistance, ArrayList<Integer> locationCategories) {
        String url = generateLocationsUrl(lat, lon, locCount, optimalDistance, locationCategories);
        sendGetRequest(url, true);
    }

    private String generateLocationsUrl(double lat, double lon, int locCount, double optimalDistance, ArrayList<Integer> locationCategories) {
        String url = XplocityApplication.getAppContext().getString(R.string.API_endpoint) + API_method + "?loc_count=" + Integer.toString(locCount)
                + "&optimal_distance=" + Double.toString(optimalDistance) + "&latitude="
                + Double.toString(lat) + "&longitude=" + Double.toString(lon);

        for (int cat : locationCategories) {
            url = url + "&category[]=" + Integer.toString(cat);
        }

        return url;
    }

    @Override
    protected void onResponse(String xml, int http_code) {
        mLogger.logError("RESPONSE");
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLLocationsParser newRouteParser = new XMLLocationsParser();

        Route route = new Route();

        try {
            route.locations = newRouteParser.parse(stream);
            route.loc_cnt_explored = 0;
            route.loc_cnt_total = route.locations.size();
            mCallback.onNewRouteDownloaded(route);
        }
        catch (Throwable e) {
            mLogger.logError("Error parsing new mRoute: ", e);
        }
    }

    @Override
    protected void onError(String errorText) {

    }
}
