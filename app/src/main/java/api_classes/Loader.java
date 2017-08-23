package api_classes;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.UTF8StringRequest;
import utils.VolleySingleton;

public abstract class Loader {
    protected Logger mLogger;

    public Loader() {
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
    }

    protected void sendDownloadRequest(String urlString) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new UTF8StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onDownloadResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLogger.logError("Error Loading location categories: " + error.getMessage(), error.getCause());
                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(stringRequest);
    }

    // Implement in descendants
    protected abstract void onDownloadResponse(String response);
}
