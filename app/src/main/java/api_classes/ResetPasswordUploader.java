package api_classes;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.ResetPasswordUploaderInterface;
import xml_parsers.XMLSignUpErrorsParser;

/**
 * Created by dmitry on 05.09.17.
 */

public class ResetPasswordUploader extends Loader {
    private ResetPasswordUploaderInterface mCallback;

    private String API_method = "/auth/reset_password";

    public ResetPasswordUploader(ResetPasswordUploaderInterface callback) {
        super((Context) callback);
        mCallback = callback;
    }

    public void resetPassword(String password, String token) {
        String url = getEndpoint() + API_method + "?password=" + password + "&token=" + token;
        sendPostRequest(url, null, false);
    }

    @Override
    protected void onResponse(String xml, int httpCode) {
        mCallback.onSuccessResetPassword();
    }

    @Override
    protected void onError(String errorText, int httpCode) {
        InputStream stream = new ByteArrayInputStream(errorText.getBytes(StandardCharsets.UTF_8));
        XMLSignUpErrorsParser parser = new XMLSignUpErrorsParser();
        try {
            ArrayList<String> errors = parser.parse(stream);
            mCallback.onErrorResetPassword(errors);
        } catch (Throwable e) {
            mLogger.logError("Error parsing sign up errors: ", e);
            ArrayList<String> errors = new ArrayList<String>();
            errors.add(errorText);
            mCallback.onErrorResetPassword(errors);
        }

    }

}
