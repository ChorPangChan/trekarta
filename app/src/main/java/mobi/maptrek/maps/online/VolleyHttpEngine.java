package mobi.maptrek.maps.online;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.oscim.core.Tile;
import org.oscim.tiling.source.HttpEngine;
import org.oscim.tiling.source.UrlTileSource;
import org.oscim.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import mobi.maptrek.util.ByteArrayInOutStream;

class VolleyHttpEngine implements HttpEngine, Response.Listener<byte[]>, Response.ErrorListener {
    private static final Logger logger = LoggerFactory.getLogger(VolleyHttpEngine.class);

    public static class VolleyHttpFactory implements HttpEngine.Factory {
        private final RequestQueue mRequestQueue;

        VolleyHttpFactory(RequestQueue requestQueue) {
            mRequestQueue = requestQueue;
        }

        @Override
        public HttpEngine create(UrlTileSource tileSource) {
            return new VolleyHttpEngine(mRequestQueue, tileSource);
        }
    }

    private final RequestQueue mRequestQueue;
    private final UrlTileSource mTileSource;
    private ByteArrayInOutStream mInOutStream;
    private byte[] mCachedData;
    private Object mTag;

    VolleyHttpEngine(RequestQueue requestQueue, UrlTileSource tileSource) {
        mRequestQueue = requestQueue;
        mTileSource = tileSource;
    }

    @Override
    public InputStream read() throws IOException {
        return mInOutStream.getInputStream();
    }

    @Override
    public void sendRequest(Tile tile) throws IOException {
        if (tile == null) {
            throw new IllegalArgumentException("Tile cannot be null");
        }
        try {
            ByteRequest request = new ByteRequest(mTileSource.getTileUrl(tile), mTileSource.getRequestHeader(), this, this);
            mTag = tile;
            request.setTag(tile);
            mRequestQueue.add(request);
            mInOutStream = new ByteArrayInOutStream();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (mTag != null)
            mRequestQueue.cancelAll(mTag);

        if (mInOutStream == null)
            return;

        IOUtils.closeQuietly(mInOutStream);
        mInOutStream = null;
        mTag = null;
    }

    @Override
    public void setCache(OutputStream outputStream) {
        if (mTileSource.tileCache != null) {
            try {
                outputStream.write(mCachedData);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean requestCompleted(boolean success) {
        IOUtils.closeQuietly(mInOutStream);
        mInOutStream = null;
        mTag = null;

        return success;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        IOUtils.closeQuietly(mInOutStream);
        mInOutStream = null;
        mTag = null;
    }

    @Override
    public void onResponse(byte[] response) {
        if (mInOutStream == null)
            return;
        try {
            if (mTileSource.tileCache != null) {
                mCachedData = response;
            }
            if (response != null)
                mInOutStream.write(response);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public class ByteRequest extends Request<byte[]> {
        private final Map<String, String> mHeaders;
        private final Response.Listener<byte[]> mListener;

        /**
         * Creates a new GET request.
         *
         * @param url      URL to fetch the string at
         * @param headers  additional request headers
         * @param listener Listener to receive the byte array response
         */
        ByteRequest(String url, Map<String, String> headers, Response.Listener<byte[]> listener,
                    Response.ErrorListener errorListener) {
            this(Method.GET, url, headers, listener, errorListener);
        }

        /**
         * Creates a new request with the given method.
         *
         * @param method   the request {@link Method} to use
         * @param url      URL to fetch the byte array at
         * @param headers  additional request headers
         * @param listener Listener to receive the byte array response or error message
         */
        ByteRequest(int method, String url, Map<String, String> headers, Response.Listener<byte[]> listener,
                    Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            mHeaders = headers;
            mListener = listener;
        }

        @Override
        protected void deliverResponse(byte[] response) {
            if (mListener != null) {
                mListener.onResponse(response);
            }
        }

        @Override
        protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
            return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        public String getBodyContentType() {
            return "application/octet-stream";
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return mHeaders;
        }
    }
}
