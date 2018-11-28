package api_classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.NewRouteDownloaderInterface;
import models.Route;
import xml_parsers.XMLLocationsParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class SharedRouteDownloader extends Loader {
    private NewRouteDownloaderInterface mCallback;
    private static final String API_method = "/shared_routes";

    public SharedRouteDownloader(NewRouteDownloaderInterface callback) {
        mCallback = callback;
    }

    public void downloadNewRoute(String id) {
        String url = generateUrl(id);
        sendGetRequest(url, true);
    }

    private String generateUrl(String id) {
        String url = getEndpoint() + API_method + "?id=" + id;

        return url;
    }

    @Override
    protected void onResponse(String xml, int http_code) {
        //mLogger.logError("RESPONSE");
        //TODO: handle invalid response
        if (http_code == HTTP_OK) {
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            XMLLocationsParser newRouteParser = new XMLLocationsParser();

            Route route = new Route();

            try {
                route.locations = newRouteParser.parse(stream);
                mCallback.onNewRouteDownloaded(route);
            } catch (Throwable e) {
                mLogger.logError("Error parsing new mRoute: ", e);
            }
        }
    }

    @Override
    protected void onError(String errorText) {

    }
}
