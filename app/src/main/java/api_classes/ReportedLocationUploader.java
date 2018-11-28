package api_classes;

import api_classes.interfaces.ReportedLocationUploaderInterface;
import models.Location;
import xml_builders.XMLReportedLocationBuilder;

/**
 * Created by dmitry on 05.09.17.
 */

public class ReportedLocationUploader extends Loader {
    private ReportedLocationUploaderInterface mCallback;

    private String API_method = "/reported_locations";

    public ReportedLocationUploader(ReportedLocationUploaderInterface callback) {
        mCallback = callback;
    }

    public void uploadReportedLocation(Location location) {
        String url = getEndpoint() + API_method;

        XMLReportedLocationBuilder xmlBuilder = new XMLReportedLocationBuilder();
        String body = xmlBuilder.toXml(location);

        sendPostRequest(url, body, true);
    }

    @Override
    protected void onResponse(String response, int http_code) {
        if (http_code == HTTP_CREATED) {
            mCallback.onSuccessUploadReportedLocation();
        }
    }

    @Override
    protected void onError(String errorText) {
        mCallback.onErrorUploadReportedLocation(errorText);
    }



}
