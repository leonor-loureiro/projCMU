package pt.ulisboa.tecnico.cmov.p2photo.serverapi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListPhotosActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoginActivity;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;

public class ServerAPI {

    // Emulator accessing pc's localhost
    private String url = "http://10.0.2.2:8080/server/";

    private String port = "8080";


    private static String loginToken;

    // singleton
    private static ServerAPI instance = null;


    /**
     * Private constructor to prevent unwanted instantiation
     */
    private ServerAPI(){

    }


    /**
     * Gets the singleton
     * @return the singleton instance
     */
    public static ServerAPI getInstance(){
        if(instance == null)
            instance = new ServerAPI();
        return instance;
    }


    /**
     * Sends a register request to the server
     * @param responseHandler Handles the response from the server
     */
    public void register(Context applicationContext, String username, String password, String publicKey, JsonHttpResponseHandler responseHandler) throws IOException, JSONException {

        HashMap<String,String> params = new HashMap<>();

        params.put("username",username);
        params.put("password",password);
        params.put("key",publicKey);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"register", new StringEntity(json), responseHandler);

    }


    /**
     * Sends a login request to the server
     * @param responseHandler Handles the response from the server
     */
    public void login(Context applicationContext, String username, String password,JsonHttpResponseHandler responseHandler) throws IOException, JSONException {


        HashMap<String,String> params = new HashMap<>();

        params.put("username",username);
        params.put("password",password);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"login", new StringEntity(json),responseHandler);

    }


    /**
     * Sends a getGroupMembership request to the server
     * @param responseHandler Handles the response from the server
     */

    public void getGroupMembership(Context applicationContext, String token, String name, String albumName,String mode, JsonHttpResponseHandler responseHandler) throws JSONException, UnsupportedEncodingException {

        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", name);
        params.put("albumName",albumName);
        params.put("mode",mode);

        String json = generateJson(params);

        HttpUtils.get(applicationContext, "getGroupMembership",  new StringEntity(json),responseHandler);

    }


    /**
     * Sends a getUsers request to the server
     * @param responseHandler Handles the response from the server
     */

    public void getUsers(Context applicationContext, String token, String username, JsonHttpResponseHandler responseHandler) throws UnsupportedEncodingException, JSONException {


        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);

        String json = generateJson(params);

        HttpUtils.get(applicationContext, "getUsers",  new StringEntity(json), responseHandler);

    }


    /**
     * Sends a register request to the server
     * @param responseHandler Handles the response from the server
     */

    public void getUserAlbums(Context applicationContext, String username, String token,String mode, JsonHttpResponseHandler responseHandler)
            throws IOException, JSONException {

        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);
        params.put("mode",mode);

        String json = generateJson(params);

        HttpUtils.get(applicationContext, "getUserAlbums",  new StringEntity(json),responseHandler);

    }



    /**
     * Sends a shareAlbum request to the server
     * @param responseHandler Handles the response from the server
     */

    public void shareAlbum(Context applicationContext,
                           String token, String username1, String username2, String albumName, String secretKey, String mode,
                           JsonHttpResponseHandler responseHandler)
            throws IOException, JSONException {


        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username1", username1);
        params.put("username2", username2);
        params.put("albumName",albumName);
        params.put("key", secretKey);
        params.put("mode",mode);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"shareAlbum",new StringEntity(json),responseHandler);
    }


    /**
     * Sends a createAlbum request to the server
     * @param responseHandler Handles the response from the server
     */

    public void createAlbum(Context applicationContext,
                            String token, String username, String alumName,
                            String url, String fileID, String secretKey, String mode,
                            JsonHttpResponseHandler responseHandler)
            throws IOException, JSONException {

        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);
        params.put("albumName",alumName);
        params.put("url",url);
        params.put("fileID",fileID);
        params.put("key", secretKey);
        params.put("mode",mode);

        String json = generateJson(params);

        HttpUtils.post(applicationContext, "createAlbum",  new StringEntity(json), responseHandler);
    }


    /**
     * Sends a updateAlbum request to the server
     */
    public void updateAlbum(final Context applicationContext, String token, String username, String name, String url, String fileID, String mode)
            throws IOException, JSONException {

        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);
        params.put("albumName",name);
        params.put("url",url);
        params.put("fileID",fileID);
        params.put("mode",mode);
        String json = generateJson(params);

        HttpUtils.post(applicationContext, "updateAlbum",  new StringEntity(json), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(statusCode == 401)
                    ServerAPI.getInstance().tokenInvalid(applicationContext);

            }
        });

    }

    /**
     * Sends a getUsers request to the server
     * @param responseHandler Handles the response from the server
     */

    public void getOperationsLog(Context applicationContext, JsonHttpResponseHandler responseHandler) throws UnsupportedEncodingException, JSONException {

        HttpUtils.get(applicationContext, "getOperationsLog",  null, responseHandler);

    }


    /**
     * Sends a get file ID request to the server
     * @param responseHandler Handles the response from the server
     */

    public void getFileID(Context applicationContext, String token, String username, String albumName,String mode, JsonHttpResponseHandler responseHandler) throws JSONException, UnsupportedEncodingException {
        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);
        params.put("albumName",albumName);
        params.put("mode",mode);
        String json = generateJson(params);

        HttpUtils.get(applicationContext, "getFileID",  new StringEntity(json),responseHandler);
    }

    /**
     * Sends the server a request for the secret key
     * @param responseHandler Handles the response from the server
     */

    public void getSecretKey(Context applicationContext, String token, String username, String albumName, JsonHttpResponseHandler responseHandler) throws JSONException, UnsupportedEncodingException {
        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);
        params.put("albumName",albumName);
        String json = generateJson(params);

        HttpUtils.get(applicationContext, "getSecretKey",  new StringEntity(json),responseHandler);
    }

    /**
     * generates a json based on a Map
     * @return the String in json
         */
    private String generateJson(Map<String,String> params) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        for(Map.Entry<String,String> entry : params.entrySet())
            jsonObject.put(entry.getKey(),entry.getValue());

        return jsonObject.toString();

    }

    /**
     * Called when the token expired, goes to the login screen
     */
    public void tokenInvalid(Context context){

        Toast.makeText(context,
                context.getString(R.string.token_expired),
                Toast.LENGTH_SHORT)
                .show();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

    }


}
