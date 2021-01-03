package api_classes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xplocity.xplocity.BuildConfig;
import com.xplocity.xplocity.LoginActivity;
import com.xplocity.xplocity.R;

import java.nio.charset.StandardCharsets;

import app.XplocityApplication;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.UTF8StringRequest;
import utils.VolleySingleton;

public abstract class Loader {
    protected Logger mLogger;
    private UTF8StringRequest stringRequest;
    protected Context mContext;

    //http codes
    public static final int HTTP_NETWORK_ERROR = -1;

    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_NOT_ACCEPTABLE = 406;


    public Loader(Context context) {
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        mContext = context;
    }


    protected String getEndpoint() {
        return BuildConfig.API_URL;

    }


    protected void sendGetRequest(String urlString, boolean useAuth) {
        sendHttpRequest(Request.Method.GET, urlString, null, useAuth, 10000);
    }


    protected void sendPostRequest(String urlString, String body, boolean useAuth) {
        sendHttpRequest(Request.Method.POST, urlString, body, useAuth, 0);
    }


    protected void sendDeleteRequest(String urlString, boolean useAuth) {
        sendHttpRequest(Request.Method.DELETE, urlString, null, useAuth, 0);
    }


    private void sendHttpRequest(int httpMethod, String urlString, String body, boolean useAuth, int timeout) {
        if (useAuth) {
            urlString = addAuthToUrl(urlString);
        }

        // Formulate the request and handle the response.
        stringRequest = new UTF8StringRequest(httpMethod, urlString, body,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Loader.this.onResponse(response, stringRequest.getHttpCode());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        NetworkResponse response = error.networkResponse;
                        if (error instanceof NetworkError) {
                            Toast.makeText(mContext, R.string.error_network_error, Toast.LENGTH_SHORT).show();
                            onError(error.getMessage(), HTTP_NETWORK_ERROR);
                        } else if (response.statusCode == HTTP_UNAUTHORIZED) {
                            deleteTokenFromStorage();
                            redirectToLoginActivity();
                        } else {
                            mLogger.logError("Http error: " + error.getMessage(), error.getCause());
                            onError(new String(response.data, StandardCharsets.UTF_8), response.statusCode);
                        }
                    }
                });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance().addToRequestQueue(stringRequest);
    }

    private void redirectToLoginActivity() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        mContext.startActivity(intent);
        Toast.makeText(mContext, R.string.error_credentials, Toast.LENGTH_LONG).show();
    }

    private void deleteTokenFromStorage() {
        Context app_context = XplocityApplication.getAppContext();
        SharedPreferences prefs = app_context.getSharedPreferences(app_context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("user_id");
        editor.remove("auth_token");
        editor.commit();
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
        } else {
            deleteTokenFromStorage();
            redirectToLoginActivity();
            return null;
        }

    }

    // Implement in descendants
    protected abstract void onResponse(String response, int httpCode);

    protected abstract void onError(String error, int httpCode);
}
