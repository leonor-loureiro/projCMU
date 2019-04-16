package pt.ulisboa.tecnico.cmov.p2photo.serverapi;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.entity.StringEntity;

public class HttpUtils {
    private static final String BASE_URL = "http://10.0.2.2:8080/server/";

    private static AsyncHttpClient client = new AsyncHttpClient();


    public static RequestHandle get(Context applicationContext, String url, StringEntity stringEntity, JsonHttpResponseHandler responseHandler) {
        return client.get(applicationContext,getAbsoluteUrl(url),stringEntity,"application/json",responseHandler);

    }

    public static void post(Context applicationContext, String url, StringEntity stringEntity, AsyncHttpResponseHandler responseHandler) {
        client.post(applicationContext,getAbsoluteUrl(url),stringEntity,"application/json",responseHandler);
    }


    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
