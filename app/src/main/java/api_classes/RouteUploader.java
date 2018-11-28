package api_classes;

import api_classes.interfaces.RouteUploaderInterface;
import models.Route;
import xml_builders.XMLRouteBuilder;

/**
 * Created by dmitry on 05.09.17.
 */

public class RouteUploader extends Loader {
    private RouteUploaderInterface mCallback;

    private String API_method = "/chains";

    public RouteUploader(RouteUploaderInterface callback) {
        mCallback = callback;
    }

    public void uploadRoute(Route route) {
        String url = getEndpoint() + API_method;

        XMLRouteBuilder xmlBuilder = new XMLRouteBuilder();
        String body = xmlBuilder.toXml(route);

        sendPostRequest(url, body, true);
    }

    @Override
    protected void onResponse(String response, int http_code) {
        if (http_code == HTTP_CREATED) {
            mCallback.onSuccessUploadRoute();
        }
    }

    @Override
    protected void onError(String errorText) {
        mCallback.onErrorUploadRoute(errorText);
    }



}
