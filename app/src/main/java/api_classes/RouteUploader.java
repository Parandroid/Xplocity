package api_classes;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.RouteUploaderInterface;
import models.Route;
import xml_builders.XMLRouteBuilder;
import xml_parsers.XMLRouteParser;

/**
 * Created by dmitry on 05.09.17.
 */

public class RouteUploader extends Loader {
    private RouteUploaderInterface mCallback;

    private String API_method = "/chains";

    public RouteUploader(RouteUploaderInterface callback) {
        super((Context) callback);
        mCallback = callback;
    }

    public void uploadRoute(Route route) {
        String url = getEndpoint() + API_method;

        XMLRouteBuilder xmlBuilder = new XMLRouteBuilder();
        String body = xmlBuilder.toXml(route);

        sendPostRequest(url, body, true);
    }

    @Override
    protected void onResponse(String xml, int httpCode) {
        if (httpCode == HTTP_CREATED) {

            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            XMLRouteParser parser = new XMLRouteParser();
            Route route;

            try {
                route = parser.parse(stream);
                mCallback.onSuccessUploadRoute(route);
            }
            catch (Throwable e) {
                mLogger.logError("Error parsing new mRoute: ", e);
            }

        }
    }

    @Override
    protected void onError(String errorText, int httpCode) {
        mCallback.onErrorUploadRoute(errorText);
    }



}
