package api_classes;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.RouteDescriptionImageDownloaderInterface;
import xml_parsers.XMLRouteDescriptionImageParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDescriptionImageDownloader extends Loader{
    private RouteDescriptionImageDownloaderInterface mCallback;
    private int mRouteId;

    private static final String API_method = "/chains/image/";

    public RouteDescriptionImageDownloader(RouteDescriptionImageDownloaderInterface callback) {
        super((Context) callback);
        mCallback = callback;
    }

    public void downloadRoute(int routeId) {
        mRouteId = routeId;
        String url = getEndpoint() + API_method + Integer.toString(routeId);
        sendGetRequest(url, true);
    }

    @Override
    protected void onResponse(String xml, int httpCode) {
        if (httpCode == HTTP_OK || httpCode == HTTP_NOT_MODIFIED) {
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            XMLRouteDescriptionImageParser parser = new XMLRouteDescriptionImageParser();
            Bitmap image;


            try {
                image = parser.parse(stream);
                mCallback.onRouteDescriptionImageDownloaded(mRouteId, image);
            } catch (Throwable e) {
                mLogger.logError("Error parsing route image: ", e);
            }
        }
    }

    @Override
    protected void onError(String errorText, int httpCode) {

    }
}
