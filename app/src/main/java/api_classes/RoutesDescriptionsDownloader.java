package api_classes;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.RoutesDescriptionsDownloaderInterface;
import models.RouteDescription;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.VolleySingleton;
import xml_parsers.XMLRouteDescriptionsParser;

/**
 * Created by dmitry on 20.08.17.
 */


public class RoutesDescriptionsDownloader {

    private RoutesDescriptionsDownloaderInterface mCallback;
    private Logger mLogger;

    public RoutesDescriptionsDownloader(RoutesDescriptionsDownloaderInterface callback) {
        mCallback = callback;
        mLogger = LogFactory.createLogger(this.getClass(), LogLevelGetter.get());
    }

    public void downloadRoutesDescriptions() {
        requestRoutesDescriptions("http://br-on.ru:3003/api/v1/chains?user_id=1");
    }

    private void requestRoutesDescriptions(String urlString) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseRoutesDescriptions(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        mLogger.logError("Error Loading routes' descriptions: " + error.getMessage(), error.getCause());
                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(stringRequest);
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
