package api_classes;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


import api_classes.interfaces.LocationCategoriesDownloaderInterface;
import models.LocationCategory;
import utils.UTF8StringRequest;
import utils.VolleySingleton;
import xml_parsers.XMLLocationCategoryParser;


/**
 * Created by dmitry on 20.08.17.
 */

public class LocationCategoriesDownloader {
    private LocationCategoriesDownloaderInterface mainClass;

    public LocationCategoriesDownloader(LocationCategoriesDownloaderInterface mClass) {
        mainClass = mClass;
    }


    public void download_location_categories() {
        request_location_categories("http://br-on.ru:3003/api/v1/location_categories");
    }


    private void request_location_categories(String urlString) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new UTF8StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parse_location_categories(response);
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

    private void parse_location_categories(String xml) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLLocationCategoryParser location_categories_parser = new XMLLocationCategoryParser();

        ArrayList<LocationCategory> location_categories;

        try {
            location_categories = location_categories_parser.parse(stream);
            mainClass.onLocationCategoriesDownload(location_categories);

        }
        catch (Throwable e) {
        }

    }
}
