package api_classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xplocity.xplocity.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.RoutesDescriptionsDownloaderInterface;
import app.XplocityApplication;
import models.Route;
import models.RouteDescription;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.UTF8StringRequest;
import utils.VolleySingleton;
import xml_parsers.XMLRouteDescriptionsParser;
import xml_parsers.XMLRouteParser;

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

    public void downloadRoutesDescriptions() {
        String url = XplocityApplication.getAppContext().getString(R.string.API_endpoint) + API_method;

        sendDownloadRequest(url, true);
    }


    @Override
    protected void onDownloadResponse(String xml, int http_code) {
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


    private void parseRoutesDescriptions(String xml) {
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
}
