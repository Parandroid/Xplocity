package api_classes.interfaces;

import java.util.ArrayList;

/**
 * Created by dmitry on 06.09.17.
 */

public interface ResetPasswordUploaderInterface {
    void onSuccessResetPassword();
    void onErrorResetPassword(ArrayList<String> errors);
}
