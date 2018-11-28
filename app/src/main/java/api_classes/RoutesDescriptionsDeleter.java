package api_classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.RoutesDescriptionsDeleterInterface;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import xml_parsers.XMLDeleteRouteResultParser;

/**
 * Created by dmitry on 20.08.17.
 */


public class RoutesDescriptionsDeleter extends Loader {

    private RoutesDescriptionsDeleterInterface mCallback;
    private Logger mLogger;

    private static final String API_method = "/chains";

    public RoutesDescriptionsDeleter(RoutesDescriptionsDeleterInterface callback) {
        mCallback = callback;
        mLogger = LogFactory.createLogger(this.getClass(), LogLevelGetter.get());
    }

    public void deleteRoute(int routeID) {
        String url = getEndpoint() + API_method + "?id=" + Integer.toString(routeID);
        sendDeleteRequest(url, true);
    }


    @Override
    protected void onResponse(String xml, int http_code) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLDeleteRouteResultParser parser = new XMLDeleteRouteResultParser();


        try {
            int deletedRouteId = parser.parse(stream);
            mCallback.onRouteDescriptionsDeleteSuccess(deletedRouteId);
        }
        catch (Throwable e) {
            mLogger.logError("Error parsing routes' descriptions: ", e);
        }
    }

    @Override
    protected void onError(String errorText) {
        mCallback.onRouteDescriptionsDeleteError(errorText);
    }
}
