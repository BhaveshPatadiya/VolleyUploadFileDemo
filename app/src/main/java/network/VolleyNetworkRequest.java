package network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class VolleyNetworkRequest<T> extends Request<T> {

    private static final String TAG = "--MultipartRequest--";
    public static final int DEFAULT_TIMEOUT_MS = 20000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();

    private final Response.Listener<T> mListener;
    protected Map<String, String> headers;
    private Map<String, String> stringData;
    private final Gson gson = new Gson();
    private Class<T> outPutClassObj;
    private HashMap<String, File> mFilePart;
    boolean isPojo;

    //Constructor
    public VolleyNetworkRequest(String url,
                                ErrorListener errorListener,
                                Listener<T> listener,
                                Map<String, String> headers,
                                Map<String, String> stringData,
                                HashMap<String, File> fileParam,
                                Class outPutClassObj,
                                boolean isPojo) {

        super(Method.POST, url, errorListener);

        setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS,
                DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mListener = listener;
        this.stringData = stringData;
        this.outPutClassObj = outPutClassObj;
        mFilePart = fileParam;
        this.headers = headers;
        this.isPojo = isPojo;

        buildMultipartEntity();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (headers == null) {
            return new HashMap<>();
        }
        return headers;
    }

    /**
     * mFilePart and FILE_PART_NAME size must be equal else it will throw error
     */

    private void buildMultipartEntity() {
        if (null != mFilePart) {
            for (Map.Entry<String, File> entry : mFilePart.entrySet()) {
                mBuilder.addBinaryBody(entry.getKey(), entry.getValue(), ContentType.create("image/jpeg"), entry.getValue().getName());

            }

        }

        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));

        if (null != stringData) {
            try {
                for (Map.Entry<String, String> entry : stringData.entrySet()) {
                    //entity.addPart(postEntityModel.getName(), new StringBody(postEntityModel.getValue(), ContentType.TEXT_PLAIN));
                    mBuilder.addTextBody(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                Log.e(TAG, "buildMultipartEntity: =" + e.getMessage());
            }
        }

    }

    @Override
    public String getBodyContentType() {
        String contentTypeHeader = mBuilder.build().getContentType().getValue();
        return contentTypeHeader;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mBuilder.build().writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.e(TAG, "parseNetworkResponse: =" + json);
            if (isPojo) {
                T myPojo = gson.fromJson(json, outPutClassObj);
                return Response.success(myPojo, HttpHeaderParser.parseCacheHeaders(response)); //it will return pojo class
            }
            return (Response<T>) Response.success(json, HttpHeaderParser.parseCacheHeaders(response));// it will return String

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
       /* T result = null;
        return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));*/
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }


}