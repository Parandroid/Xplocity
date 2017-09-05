package api_classes;

import com.xplocity.xplocity.R;

import api_classes.interfaces.RouteUploaderInterface;
import app.XplocityApplication;
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
        String url = XplocityApplication.getAppContext().getString(R.string.API_endpoint) + API_method;

        XMLRouteBuilder xmlBuilder = new XMLRouteBuilder();
        String body = xmlBuilder.toXml(route);

        sendPostRequest(url, body, true);
    }


    protected void onResponse(String response, int http_code) {
        mCallback.onSuccessUploadRoute();
    }


}
