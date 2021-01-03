package api_classes;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import api_classes.interfaces.AuthTokenDownloaderInterface;
import models.AuthToken;
import xml_parsers.XMLAuthTokenParser;

/**
 * Created by dmitry on 20.08.17.
 */

public class AuthTokenDownloader extends Loader {
    private AuthTokenDownloaderInterface mCallback;
    private static final String API_method = "/auth/get_authentication_token";

    public AuthTokenDownloader(AuthTokenDownloaderInterface callback) {
        super((Context) callback);
        mCallback = callback;
    }

    public void downloadAuthToken(String email, String password) {
        String url = generateAuthUrl(email, password);
        sendGetRequest(url, false);
    }

    private String generateAuthUrl(String email, String password) {
        String url = getEndpoint() + API_method + "?email=" + email + "&password=" + password;
        return url;
    }

    @Override
    protected void onResponse(String xml, int httpCode) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLAuthTokenParser authTokenParser = new XMLAuthTokenParser();

        try {
            AuthToken auth_token = authTokenParser.parse(stream);
            mCallback.onAuthTokenDownloaded(auth_token);
        } catch (Throwable e) {
            mLogger.logError("Error parsing auth token: ", e);
        }
    }

    @Override
    protected void onError(String errorText, int httpCode) {
        mCallback.onAuthTokenDownloadError(errorText);
    }


}
