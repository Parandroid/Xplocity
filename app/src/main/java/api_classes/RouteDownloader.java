package api_classes;

import com.xplocity.xplocity.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.RouteDownloaderInterface;
import app.XplocityApplication;
import models.Route;

import xml_parsers.XMLRouteParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDownloader extends Loader{
    private RouteDownloaderInterface mCallback;

    private static final String API_method = "/chains/";

    public RouteDownloader(RouteDownloaderInterface callback) {
        mCallback = callback;
    }

    public void downloadRoute(int routeId) {
        String url = XplocityApplication.getAppContext().getString(R.string.API_endpoint) + API_method + Integer.toString(routeId);
        sendGetRequest(url, true);
    }

    @Override
    protected void onResponse(String xml, int http_code) {
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

    @Override
    protected void onError(String errorText) {

    }
}
