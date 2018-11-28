package api_classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.RoutesDescriptionsDownloaderInterface;
import models.RouteDescription;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import xml_parsers.XMLRouteDescriptionsParser;

/**
 * Created by dmitry on 20.08.17.
 */


public class RoutesDescriptionsDownloader extends Loader {

    private RoutesDescriptionsDownloaderInterface mCallback;
    private Logger mLogger;

    private static final String API_method = "/chains";

    public RoutesDescriptionsDownloader(RoutesDescriptionsDownloaderInterface callback) {
        mCallback = callback;
        mLogger = LogFactory.createLogger(this.getClass(), LogLevelGetter.get());
    }

    public void downloadRoutesDescriptions(int offset, int limit) {
        String url = getEndpoint() + API_method + "?offset=" + Integer.toString(offset) + "&limit=" + Integer.toString(limit);
        sendGetRequest(url, true);
    }


    @Override
    protected void onResponse(String xml, int http_code) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLRouteDescriptionsParser route_descriptions_parser = new XMLRouteDescriptionsParser();

        ArrayList<RouteDescription> route_descriptions;

        try {
            route_descriptions = route_descriptions_parser.parse(stream);
            mCallback.onRouteDescriptionsDownloaded(route_descriptions);
        }
        catch (Throwable e) {
            mLogger.logError("Error parsing routes' descriptions: ", e);
        }
    }

    @Override
    protected void onError(String errorText) {

    }
}
