package api_classes;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.RoutesDescriptionsDeleterInterface;
import utils.Log.Logger;
import xml_parsers.XMLDeleteRouteResultParser;

/**
 * Created by dmitry on 20.08.17.
 */


public class RoutesDescriptionsDeleter extends Loader {

    private RoutesDescriptionsDeleterInterface mCallback;
    private Logger mLogger;

    private static final String API_method = "/chains";

    public RoutesDescriptionsDeleter(RoutesDescriptionsDeleterInterface callback) {
        super((Context) callback);
        mCallback = callback;
    }

    public void deleteRoute(int routeID) {
        String url = getEndpoint() + API_method + "?id=" + Integer.toString(routeID);
        sendDeleteRequest(url, true);
    }


    @Override
    protected void onResponse(String xml, int httpCode) {
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
    protected void onError(String errorText, int httpCode) {
        mCallback.onRouteDescriptionsDeleteError(errorText);
    }
}
