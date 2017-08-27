package api_classes;

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

    public LocationCategoriesDownloader(LocationCategoriesDownloaderInterface callback) {
        mCallback = callback;
    }

    public void downloadLocationCategories() {
        sendDownloadRequest("http://br-on.ru:3003/api/v1/location_categories", false);
    }

    @Override
    protected void onDownloadResponse(String xml, int http_code) {
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
}
