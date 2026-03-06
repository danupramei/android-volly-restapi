package com.budimanlai.restapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RestAPIBase {

    protected String TAG = getClass().getName();
    protected String mBaseUrl;
    protected boolean mDebug = false;

    protected Context mContext;
    protected RequestQueue mRequestQueue;
    protected ImageLoader mImageLoader;
    protected Map<String, String> mHeaders;
    protected RestAPIListenerInterface mListener;

    public RestAPIBase(Context context) {
        init(context, "");
    }

    public RestAPIBase(Context context, String baseUrl) {
        init(context, baseUrl);
    }

    protected void init(Context context, String baseUrl) {
        mContext = context;
        mBaseUrl = baseUrl;

        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

        mHeaders = new HashMap<>();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Remove all headers field
     */
    public void removeHeaders() {
        mHeaders.clear();
    }

    /**
     * Add single header key
     *
     * @param key   String header field name
     * @param value String header value
     */
    public void setHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    /**
     * Add multiple headers
     *
     * @param headers Map<String, String> header items
     */
    public void setHeaders(Map<String, String> headers) {
        mHeaders.putAll(headers);
    }

    /**
     * Get all headers
     *
     * @return Map<String, String>
     */
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    /**
     * Turn on/off debug message
     *
     * @param debug boolean
     */
    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    public void setListener(RestAPIListenerInterface listener) {
        mListener = listener;
    }

    public RestAPIListenerInterface getListener() {
        if (mListener == null) {
            mListener = new RestAPIJSONListener();
        }
        return mListener;
    }

    /**
     * Show the log message to logcat if mdebug is on (true)
     *
     * @param message String
     */
    protected void log(String message) {
        if (mDebug) {
            Log.d(TAG, message);
        }
    }

    /**
     * Send POST Raw JSON to endpoint
     *
     * @param url    String
     * @param params Map<String, String> params
     */
    protected void stringRequest(int method, final String url, final JSONObject params, String tag, final RestAPIListenerInterface listener, final RestAPIResponseInterface handler) {
        log("URL: " + getUrl(url));
        log("Endpoint: " + url);
        log("Headers: " + mHeaders);
        log("Body: " + params.toString());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                method,
                getUrl(url),
                params,
                response -> {
                    log("onResponse: " + response.toString());
                    listener.onSuccessHandler(response.toString(), handler);
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("VOLLEY_ERROR", "Status: " + error.networkResponse.statusCode);
                        Log.e("VOLLEY_ERROR", new String(error.networkResponse.data));
                    } else {
                        Log.e("VOLLEY_ERROR", error.toString());
                    }

                    listener.onErrorHandler(error, handler);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return RestAPIBase.this.getHeaders();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        log("Timeout set to: " + jsonRequest.getRetryPolicy().getCurrentTimeout());
        addToRequestQueue(jsonRequest, tag);
    }

    public String getUrl(String url) {
        return mBaseUrl + url;
    }

    public void postJSON(String url, JSONObject params, String tag, RestAPIListenerInterface listener, RestAPIResponseInterface handler) {
        stringRequest(Request.Method.POST, url, params, tag, listener, handler);
    }

    public void postJSON(String url, JSONObject params, RestAPIResponseInterface handler) {
        stringRequest(Request.Method.POST, url, params, "post_json_request", getListener(), handler);
    }

    public void postJSON(String url, Map<String, String> params, RestAPIResponseInterface handler) {
        stringRequest(Request.Method.POST, url, new JSONObject(params), "post_json_request", getListener(), handler);
    }

    public void get(String url, JSONObject params, String tag, RestAPIListenerInterface listener, RestAPIResponseInterface handler) {
        stringRequest(Request.Method.GET, url, params, tag, listener, handler);
    }

    public void get(String url, JSONObject params, RestAPIResponseInterface handler) {
        stringRequest(Request.Method.GET, url, params, "post_json_request", getListener(), handler);
    }

    public void get(String url, Map<String, String> params, RestAPIResponseInterface handler) {
        stringRequest(Request.Method.GET, url, new JSONObject(params), "post_json_request", getListener(), handler);
    }

    public void get(String url, RestAPIResponseInterface handler) {
        stringRequest(Request.Method.GET, url, new JSONObject(), "post_json_request", getListener(), handler);
    }
}
