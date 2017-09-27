package api_classes.interfaces;

/**
 * Created by dmitry on 06.09.17.
 */

public interface RouteUploaderInterface {
    void onSuccessUploadRoute();
    void onErrorUploadRoute(String errorText);
}
