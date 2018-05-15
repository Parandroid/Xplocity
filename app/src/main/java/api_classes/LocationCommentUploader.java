package api_classes;

import com.xplocity.xplocity.R;

import api_classes.interfaces.LocationCommentUploaderInterface;
import api_classes.interfaces.ReportedLocationUploaderInterface;
import app.XplocityApplication;
import models.Location;
import models.LocationComment;
import xml_builders.XMLLocationCommentBuilder;
import xml_builders.XMLReportedLocationBuilder;

/**
 * Created by dmitry on 05.09.17.
 */

public class LocationCommentUploader extends Loader {
    private LocationCommentUploaderInterface mCallback;

    private String API_method = "/locations/";

    public LocationCommentUploader(LocationCommentUploaderInterface callback) {
        mCallback = callback;
    }

    public void uploadLocationComment(LocationComment comment) {
        String url = XplocityApplication.getAppContext().getString(R.string.API_endpoint) + API_method + Integer.toString(comment.location.id) + "/comments";

        XMLLocationCommentBuilder xmlBuilder = new XMLLocationCommentBuilder();
        String body = xmlBuilder.toXml(comment);

        sendPostRequest(url, body, true);
    }

    @Override
    protected void onResponse(String response, int http_code) {
        if (http_code == HTTP_CREATED) {
            mCallback.onSuccessUploadLocationComment();
        }
    }

    @Override
    protected void onError(String errorText) {
        mCallback.onErrorUploadLocationComment(errorText);
    }



}
