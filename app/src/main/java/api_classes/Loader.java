package api_classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xplocity.xplocity.R;

import app.XplocityApplication;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.UTF8StringRequest;
import utils.VolleySingleton;

public abstract class Loader {
    protected Logger mLogger;
    private UTF8StringRequest stringRequest;

    //http codes
    public static final int HTTP_OK = 200;
    public static final int HTTP_NOT_MODIFIED = 304;


    public Loader() {
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
    }

    protected void sendDownloadRequest(String urlString, boolean useAuth) {
        if (useAuth) {
            urlString = addAuthToUrl(urlString);
        }

        // Formulate the request and handle the response.
        stringRequest = new UTF8StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onDownloadResponse(response, stringRequest.getHttpCode());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLogger.logError("Network error: " + error.getMessage(), error.getCause());
                        onDownloadResponse(error.getMessage(), stringRequest.getHttpCode());

                        //TODO если получили ответ, что токен неверный, просим пользователя ввести логин/пароль заново

                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(stringRequest);
    }

    private String addAuthToUrl(String url) {
        Context app_context = XplocityApplication.getAppContext();
        SharedPreferences prefs = app_context.getSharedPreferences(app_context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (prefs.getString("auth_token", null) != null && prefs.getInt("user_id", 0) != 0) {
            String auth_token = prefs.getString("auth_token", null);
            int user_id = prefs.getInt("user_id", 0);


            if (url.contains("?")) {
                url = url + "&";
            } else {
                url = url + "?";
            }

            url = url + "user_id=" + Integer.toString(user_id) + "&authentication_token=" + auth_token;

            return url;
        }
        else
        {
            // TODO передалать на Exception?
            return null;
        }

    }

    // Implement in descendants
    protected abstract void onDownloadResponse(String response, int http_code);
}
