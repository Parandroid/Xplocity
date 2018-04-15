package api_classes;

import android.graphics.Bitmap;

import com.xplocity.xplocity.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.RouteDescriptionImageDownloaderInterface;
import api_classes.interfaces.RouteDownloaderInterface;
import app.XplocityApplication;
import models.Route;
import xml_parsers.XMLRouteDescriptionImageParser;
import xml_parsers.XMLRouteParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDescriptionImageDownloader extends Loader{
    private RouteDescriptionImageDownloaderInterface mCallback;
    private int mRouteId;

    private static final String API_method = "/chains/image/";

    public RouteDescriptionImageDownloader(RouteDescriptionImageDownloaderInterface callback) {
        mCallback = callback;
    }

    public void downloadRoute(int routeId) {
        mRouteId = routeId;
        String url = XplocityApplication.getAppContext().getString(R.string.API_endpoint) + API_method + Integer.toString(routeId);
        sendGetRequest(url, true);
    }

    @Override
    protected void onResponse(String xml, int http_code) {
        if (http_code == HTTP_OK || http_code == HTTP_NOT_MODIFIED) {
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
    protected void onError(String errorText) {

    }
}
