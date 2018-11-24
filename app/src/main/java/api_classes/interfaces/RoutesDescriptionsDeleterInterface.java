package api_classes.interfaces;

/**
 * Created by dmitry on 20.08.17.
 */

public interface RoutesDescriptionsDeleterInterface {
    void onRouteDescriptionsDeleteSuccess(int deletedRouteId);
    void onRouteDescriptionsDeleteError(String error);
}
