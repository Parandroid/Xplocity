package api_classes;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.NewRouteDownloaderInterface;
import models.Route;
import utils.VolleySingleton;
import xml_parsers.XMLLocationsParser;


/**
 * Created by dmitry on 20.08.17.
 */

public class NewRouteDownloader {
    private NewRouteDownloaderInterface mainClass;

    public NewRouteDownloader(NewRouteDownloaderInterface mClass) {
        mainClass = mClass;
    }


    public void download_new_route(Double lat, Double lon, int loc_count, double optimal_distance, ArrayList<Integer> location_categories) {
        String url = generate_locations_url(lat, lon, loc_count, optimal_distance, location_categories);
        request_new_route(url);
    }


    private String generate_locations_url(double lat, double lon, int loc_count, double optimal_distance, ArrayList<Integer> location_categories) {

        String url = "http://br-on.ru:3003/api/v1/locations/get_location_list?loc_count=" + Integer.toString(loc_count)
                + "&optimal_distance=" + Double.toString(optimal_distance) + "&latitude="
                + Double.toString(lat) + "&longitude=" + Double.toString(lon);

        for (int cat : location_categories) {
            url = url + "&category[]=" + Integer.toString(cat);
        }

        return url;
    }


    private void request_new_route(String urlString) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parse_new_route(response);
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


    private void parse_new_route(String xml) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLLocationsParser new_route_parser = new XMLLocationsParser();

        Route route = new Route();

        try {
            route.locations = new_route_parser.parse(stream);
            mainClass.onNewRouteDownload(route);

        }
        catch (Throwable e) {
        }

    }


}
