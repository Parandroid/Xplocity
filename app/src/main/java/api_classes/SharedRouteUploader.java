package api_classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.SharedRouteUploaderInterface;
import models.Route;
import xml_builders.XMLSharedRouteBuilder;
import xml_parsers.XMLSharedRouteIDParser;

/**
 * Created by dmitry on 05.09.17.
 */

public class SharedRouteUploader extends Loader {
    private SharedRouteUploaderInterface mCallback;

    private String API_method = "/shared_routes";

    public SharedRouteUploader(SharedRouteUploaderInterface callback) {
        mCallback = callback;
    }

    public void uploadSharedRoute(Route route) {
        String url = getEndpoint() + API_method;

        XMLSharedRouteBuilder xmlBuilder = new XMLSharedRouteBuilder();
        String body = xmlBuilder.toXml(route);

        sendPostRequest(url, body, true);
    }

    @Override
    protected void onResponse(String xml, int http_code) {
        if (http_code == HTTP_CREATED) {
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            XMLSharedRouteIDParser parser = new XMLSharedRouteIDParser();


            try {
                String sharedRouteID = parser.parse(stream);
                mCallback.onSuccessUploadRoute(sharedRouteID);
            } catch (Throwable e) {
                mLogger.logError("Error parsing auth token: ", e);
            }

        }
    }

    @Override
    protected void onError(String errorText) {
        mCallback.onErrorUploadRoute(errorText);
    }



}
