package api_classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
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
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_NOT_ACCEPTABLE = 406;


    public Loader() {
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
    }

    protected void sendGetRequest(String urlString, boolean useAuth) {
        if (useAuth) {
            urlString = addAuthToUrl(urlString);
        }

        // Formulate the request and handle the response.
        stringRequest = new UTF8StringRequest(Request.Method.GET, urlString, null,
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
                        if (error instanceof ServerError && response != null) {
                            String res = new String(response.data);
                            Loader.this.onResponse(res, stringRequest.getHttpCode());
                        }
                        else {
                            mLogger.logError("Network error: " + error.getMessage(), error.getCause());
                            onResponse(error.getMessage(), stringRequest.getHttpCode());

                            //TODO если получили ответ, что токен неверный, просим пользователя ввести логин/пароль заново
                        }
                    }
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance().addToRequestQueue(stringRequest);
    }


    protected void sendPostRequest(String urlString, String body, boolean useAuth) {
        if (useAuth) {
            urlString = addAuthToUrl(urlString);
        }

        // Formulate the request and handle the response.
        stringRequest = new UTF8StringRequest(Request.Method.POST, urlString, body,
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
                        if (error instanceof ServerError && response != null) {
                            String res = new String(response.data);
                            Loader.this.onResponse(res, response.statusCode);
                        }
                        else {
                            mLogger.logError("Network error: " + error.getMessage(), error.getCause());
                            onResponse(error.getMessage(), stringRequest.getHttpCode());

                            //TODO если получили ответ, что токен неверный, просим пользователя ввести логин/пароль заново
                        }
                    }
                });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance().addToRequestQueue(stringRequest);
    }


    protected void sendDeleteRequest(String urlString, boolean useAuth) {
        if (useAuth) {
            urlString = addAuthToUrl(urlString);
        }

        // Formulate the request and handle the response.
        stringRequest = new UTF8StringRequest(Request.Method.DELETE, urlString, null,
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
                        if (error instanceof ServerError && response != null) {
                            String res = new String(response.data);
                            Loader.this.onResponse(res, response.statusCode);
                        }
                        else {
                            mLogger.logError("Network error: " + error.getMessage(), error.getCause());
                            onResponse(error.getMessage(), stringRequest.getHttpCode());

                            //TODO если получили ответ, что токен неверный, просим пользователя ввести логин/пароль заново
                        }
                    }
                });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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
        } else {
            // TODO передалать на Exception?
            return null;
        }

    }

    // Implement in descendants
    protected abstract void onResponse(String response, int http_code);

    protected abstract void onError(String error);
}
