package api_classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.SignUpUploaderInterface;
import models.AuthToken;
import xml_parsers.XMLAuthTokenParser;
import xml_parsers.XMLSignUpErrorsParser;

/**
 * Created by dmitry on 05.09.17.
 */

public class SignUpUploader extends Loader {
    private SignUpUploaderInterface mCallback;

    private String API_method = "/users";

    public SignUpUploader(SignUpUploaderInterface callback) {
        mCallback = callback;
    }

    public void uploadSignUpInfo(String email, String password) {
        String url = getEndpoint() + API_method + "?email=" + email + "&password=" + password;

        sendPostRequest(url, null, false);
    }

    @Override
    protected void onResponse(String xml, int http_code) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        if (http_code == HTTP_CREATED) {

            XMLAuthTokenParser authTokenParser = new XMLAuthTokenParser();
            try {
                AuthToken auth_token = authTokenParser.parse(stream);
                mCallback.onSuccessUploadSignUpInfo(auth_token);
            } catch (Throwable e) {
                mLogger.logError("Error parsing auth token: ", e);
            }
        }
        else {
            if (http_code == HTTP_NOT_ACCEPTABLE) {
                XMLSignUpErrorsParser parser = new XMLSignUpErrorsParser();
                try {
                    ArrayList<String> errors = parser.parse(stream);
                    mCallback.onErrorUploadSignUpInfo(errors);
                } catch (Throwable e) {
                    mLogger.logError("Error parsing sign up errors: ", e);
                }
            }
            else {
                onError(xml);
            }
        }
    }

    @Override
    protected void onError(String errorText) {
        ArrayList<String> errors = new ArrayList<String>();
        errors.add(errorText);
        mCallback.onErrorUploadSignUpInfo(errors);
    }


}
