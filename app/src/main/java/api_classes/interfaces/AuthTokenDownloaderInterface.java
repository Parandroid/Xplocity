package api_classes.interfaces;


import models.AuthToken;

/**
 * Created by dmitry on 20.08.17.
 */

public interface AuthTokenDownloaderInterface {
    void onAuthTokenDownloaded(AuthToken auth_token, String error);
}
