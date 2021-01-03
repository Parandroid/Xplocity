package api_classes;

import android.content.Context;

import api_classes.interfaces.LocationCommentUploaderInterface;
import models.LocationComment;
import xml_builders.XMLLocationCommentBuilder;

/**
 * Created by dmitry on 05.09.17.
 */

public class LocationCommentUploader extends Loader {
    private LocationCommentUploaderInterface mCallback;

    private String API_method = "/locations/";

    public LocationCommentUploader(LocationCommentUploaderInterface callback, Context context) {
        super(context);
        mCallback = callback;
    }

    public void uploadLocationComment(LocationComment comment) {
        String url = getEndpoint() + API_method + Integer.toString(comment.location.id) + "/comments";

        XMLLocationCommentBuilder xmlBuilder = new XMLLocationCommentBuilder();
        String body = xmlBuilder.toXml(comment);

        sendPostRequest(url, body, true);
    }

    @Override
    protected void onResponse(String response, int httpCode) {
        if (httpCode == HTTP_CREATED) {
            mCallback.onSuccessUploadLocationComment();
        }
    }

    @Override
    protected void onError(String errorText, int httpCode) {
        mCallback.onErrorUploadLocationComment(errorText);
    }



}
