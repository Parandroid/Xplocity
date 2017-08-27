package api_classes;

import android.content.res.Resources;

import com.xplocity.xplocity.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import api_classes.interfaces.AuthTokenDownloaderInterface;
import api_classes.interfaces.NewRouteDownloaderInterface;
import app.XplocityApplication;
import models.AuthToken;
import models.Route;
import xml_parsers.XMLAuthTokenParser;
import xml_parsers.XMLLocationsParser;

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
        sendDownloadRequest(url, false);
    }

    private String generateAuthUrl(String email, String password) {
        String url = XplocityApplication.getAppContext().getString(R.string.API_endpoint) + API_method + "?email=" + email + "&password=" + password;
        return url;
    }

    @Override
    protected void onDownloadResponse(String xml, int http_code) {

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


}
