package api_classes;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.RouteDescriptionsDownloaderInterface;
import models.RouteDescription;
import utils.VolleySingleton;
import xml_parsers.XMLRouteDescriptionsParser;

/**
 * Created by dmitry on 20.08.17.
 */


public class RouteDescriptionsDownloader {

    private RouteDescriptionsDownloaderInterface mainClass;

    public RouteDescriptionsDownloader(RouteDescriptionsDownloaderInterface mClass) {
        mainClass = mClass;
    }


    public void download_route_descriptions() {
        request_route_descriptions("http://br-on.ru:3003/api/v1/chains?user_id=1");
    }


    private void request_route_descriptions(String urlString) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parse_route_descriptions(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                });
        VolleySingleton.getInstance().addToRequestQueue(stringRequest);

    }

    private void parse_route_descriptions(String xml) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLRouteDescriptionsParser route_descriptions_parser = new XMLRouteDescriptionsParser();

        ArrayList<RouteDescription> route_descriptions;

        try {
            route_descriptions = route_descriptions_parser.parse(stream);
            mainClass.onRouteDescriptionsDownload(route_descriptions);

        }
        catch (Throwable e) {
        }

    }


}
