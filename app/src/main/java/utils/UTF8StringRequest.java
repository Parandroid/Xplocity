package utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;

/**
 * Created by dmitry on 06.08.17.
 */

public class UTF8StringRequest extends StringRequest {

    private int http_code;
    private String mBody;

    public UTF8StringRequest(int method, String url, String body, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mBody = body;

    }

    public UTF8StringRequest(String url, String body, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        mBody = body;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {

        String utf8String = null;
        try {
            utf8String = new String(response.data, "UTF-8");
            http_code = response.statusCode;
            return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public String getBodyContentType() {
        return "application/xml; charset=" +
                getParamsEncoding();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        try {
            return mBody == null ? null :
                    mBody.getBytes(getParamsEncoding());
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }


    public int getHttpCode() {
        return http_code;
    }
}
