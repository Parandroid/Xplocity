package api_classes;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


import api_classes.interfaces.RouteDownloaderInterface;
import models.Route;
import utils.VolleySingleton;

import xml_parsers.XMLRouteParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class RouteDownloader {
    private RouteDownloaderInterface mainClass;

    public RouteDownloader(RouteDownloaderInterface mClass) {
        mainClass = mClass;
    }


    public void download_route(int route_id) {
        request_route("http://br-on.ru:3003/api/v1/chains/"+Integer.toString(route_id));
    }


    private void request_route(String urlString) {
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
        XMLRouteParser route_parser = new XMLRouteParser();

        Route route;

        try {
            route = route_parser.parse(stream);
            mainClass.onRouteDownload(route);

        }
        catch (Throwable e) {
        }

    }
}
