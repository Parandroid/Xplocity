package api_classes.interfaces;

import java.util.ArrayList;

import models.LocationCategory;
import models.Route;

/**
 * Created by dmitry on 20.08.17.
 */

public interface LocationCategoriesDownloaderInterface {
    void onLocationCategoriesDownloaded(ArrayList<LocationCategory> locationCategories);
}
