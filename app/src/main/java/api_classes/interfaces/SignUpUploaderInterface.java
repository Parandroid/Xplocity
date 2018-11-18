package api_classes.interfaces;

import java.util.ArrayList;

import models.AuthToken;

/**
 * Created by dmitry on 06.09.17.
 */

public interface SignUpUploaderInterface {
    void onSuccessUploadSignUpInfo(AuthToken authToken);
    void onErrorUploadSignUpInfo(ArrayList<String> errorText);
}
