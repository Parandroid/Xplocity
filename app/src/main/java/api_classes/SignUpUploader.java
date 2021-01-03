package api_classes;

import android.content.Context;
import android.widget.Toast;

import com.xplocity.xplocity.R;

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
        super((Context) callback);
        mCallback = callback;
    }

    public void uploadSignUpInfo(String email, String password) {
        String url = getEndpoint() + API_method + "?email=" + email + "&password=" + password;

        sendPostRequest(url, null, false);
    }

    @Override
    protected void onResponse(String xml, int httpCode) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        XMLAuthTokenParser authTokenParser = new XMLAuthTokenParser();
        try {
            AuthToken auth_token = authTokenParser.parse(stream);
            mCallback.onSuccessUploadSignUpInfo(auth_token);
        } catch (Throwable e) {
            mLogger.logError("Error parsing auth token: ", e);
            Toast.makeText(mContext, R.string.error_network_error, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onError(String errorText, int httpCode) {
        InputStream stream = new ByteArrayInputStream(errorText.getBytes(StandardCharsets.UTF_8));
        XMLSignUpErrorsParser parser = new XMLSignUpErrorsParser();
        try {
            ArrayList<String> errors = parser.parse(stream);
            mCallback.onErrorUploadSignUpInfo(errors);
        } catch (Throwable e) {
            mLogger.logError("Error parsing sign up errors: ", e);
            ArrayList<String> errors = new ArrayList<String>();
            errors.add(errorText);
            mCallback.onErrorUploadSignUpInfo(errors);
        }
    }

}
