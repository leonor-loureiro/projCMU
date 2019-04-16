package pt.ulisboa.tecnico.cmov.p2photo.serverapi;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;

public class ServerAPI {

    // Emulator accessing pc's localhost
    private String url = "http://10.0.2.2:8080/server/";

    private String port = "8080";

    private String username;

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


    public void register(Context applicationContext, String username, String password) throws IOException, JSONException {

        HashMap<String,String> params = new HashMap<>();

        params.put("username",username);
        params.put("password",password);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"register", new StringEntity(json), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }

        });

    }

    public void login(Context applicationContext, String username, String password) throws IOException, JSONException {


        HashMap<String,String> params = new HashMap<>();

        params.put("username",username);
        params.put("password",password);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"login", new StringEntity(json), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }

        });

    }


    public Map<String, String> getGroupMembership() {



        return null;
    }

    /**
     * @return list of all members of the P2Pservice
     */
    public List<Member> getUsers() {

        //TODO: parse usernames into Members
        return null;
    }

    /**
     * Lists all the albums belonging to the user
     * @return list of all user's album
     * @param applicationContext
     * @param token
     * @param requestHandler
     */
    public void getUserAlbums(Context applicationContext, String username, String token, JsonHttpResponseHandler requestHandler) throws IOException, JSONException {

        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);

        String json = generateJson(params);

        HttpUtils.get(applicationContext, "getUserAlbums",  new StringEntity(json),requestHandler);

    }


    public void shareAlbum(Context applicationContext,String token, String username1, String username2, String albumName,JsonHttpResponseHandler requestHandler) throws IOException, JSONException {


        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username1", username1);
        params.put("username2", username2);
        params.put("albumName",albumName);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"shareAlbum",new StringEntity(json),requestHandler);


    }

    public void createAlbum(Context applicationContext, String token, String username, String name, String url, String fileID) throws IOException, JSONException {

        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);
        params.put("albumName",name);
        params.put("url",url);
        params.put("fileID",fileID);
        String json = generateJson(params);

        HttpUtils.post(applicationContext, "createAlbum",  new StringEntity(json), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }


        });

    }

    private String generateJson(Map<String,String> params) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        for(Map.Entry<String,String> entry : params.entrySet())
            jsonObject.put(entry.getKey(),entry.getValue());

        return jsonObject.toString();

    }

}
