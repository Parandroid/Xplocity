package api_classes;

import android.content.Context;
import android.widget.Toast;

import com.xplocity.xplocity.R;

import api_classes.interfaces.SendResetPasswordMailUploaderInterface;

/**
 * Created by dmitry on 05.09.17.
 */

public class SendResetPasswordMailUploader extends Loader {
    private SendResetPasswordMailUploaderInterface mCallback;

    private String API_method = "/auth/send_reset_password_email";

    public SendResetPasswordMailUploader(SendResetPasswordMailUploaderInterface callback) {
        super((Context) callback);
        mCallback = callback;
    }

    public void sendResetPasswordMail(String email) {
        String url = getEndpoint() + API_method + "?email=" + email;

        sendPostRequest(url, null, false);
    }

    @Override
    protected void onResponse(String xml, int httpCode) {
        mCallback.onSuccessSentMail();
    }

    @Override
    protected void onError(String errorText, int httpCode) {
        Toast.makeText(mContext, R.string.error_network_error, Toast.LENGTH_SHORT).show();
    }

}
