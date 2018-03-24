package api_classes.interfaces;

/**
 * Created by dmitry on 06.09.17.
 */

public interface SharedRouteUploaderInterface {
    void onSuccessUploadRoute(String sharedRouteID);
    void onErrorUploadRoute(String errorText);
}
