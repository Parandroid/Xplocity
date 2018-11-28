package api_classes;

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
    protected void onResponse(String xml, int http_code) {

        if (http_code == HTTP_OK || http_code == HTTP_NOT_MODIFIED) {
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            XMLAuthTokenParser authTokenParser = new XMLAuthTokenParser();


            try {
                AuthToken auth_token = authTokenParser.parse(stream);
                mCallback.onAuthTokenDownloaded(auth_token, "OK");
            } catch (Throwable e) {
                mLogger.logError("Error parsing auth token: ", e);
            }
        }
        else {
            mCallback.onAuthTokenDownloaded(null, xml);
        }
    }

    @Override
    protected void onError(String errorText) {

    }


}
