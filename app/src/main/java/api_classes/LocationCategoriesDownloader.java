package api_classes;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.LocationCategoriesDownloaderInterface;
import models.LocationCategory;
import xml_parsers.XMLLocationCategoryParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class LocationCategoriesDownloader extends Loader {
    private LocationCategoriesDownloaderInterface mCallback;

    private static final String API_method = "/location_categories";

    public LocationCategoriesDownloader(LocationCategoriesDownloaderInterface callback) {
        super((Context) callback);
        mCallback = callback;
    }

    public void downloadLocationCategories() {
        String url = getEndpoint() + API_method;
        sendGetRequest(url, false);
    }

    @Override
    protected void onResponse(String xml, int httpCode) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLLocationCategoryParser parser = new XMLLocationCategoryParser();

        ArrayList<LocationCategory> location_categories;

        try {
            location_categories = parser.parse(stream);
            mCallback.onLocationCategoriesDownloaded(location_categories);
        }
        catch (Throwable e) {
            mLogger.logError("Error parsing location categories: ", e);
        }
    }

    @Override
    protected void onError(String errorText, int httpCode) {

    }
}
