package pt.ulisboa.tecnico.cmov.p2photo.serverapi;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;

public class ServerAPI {

    // Emulator accessing pc's localhost
    private String url = "http://10.0.2.2:8080/server/";

    private String port = "8080";

    private String username;

    private static String loginToken;

    private String response;
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


    public String register(Context applicationContext, String username, String password) throws IOException, JSONException {

        response = "";
        HashMap<String,String> params = new HashMap<>();

        params.put("username",username);
        params.put("password",password);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"register", new StringEntity(json), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) { try {
                response = new JSONObject(response.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });

        Log.i("response",response);
        return response;
    }

    public String login(Context applicationContext, String username, String password) throws IOException, JSONException {


        response = "";
        HashMap<String,String> params = new HashMap<>();

        params.put("username",username);
        params.put("password",password);
        String json = generateJson(params);

        HttpUtils.post(applicationContext,"login", new StringEntity(json), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) { try {
                    response = new JSONObject(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });

        Log.i("response",response);
        return response;
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
     */
    public List<Album> getUserAlbums(Context applicationContext, String username, String token) throws IOException, JSONException {

        response = "";
        HashMap<String, String> params = new HashMap<>();

        params.put("token",token);
        params.put("username", username);

        String json = generateJson(params);

        HttpUtils.get(applicationContext, "getUserAlbums",  new StringEntity(json), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Log.i("valueof", response.toString());

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("failure","failure");
            }
        });

        Log.i("response", "xd" + response);

        return transformResponse(response);

    }

    private List<Album> transformResponse(String response) {
        ArrayList<Album> albums = new ArrayList<>();

        return albums;

    }


    public void shareAlbum() {


    }

    public String createAlbum(Context applicationContext, String token, String username, String name, String url, String fileID) throws IOException, JSONException {

        response = "";
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
                try {
                    response = new JSONObject(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });

        Log.i("response", response);

        return response;
    }

    private String generateJson(Map<String,String> params) throws IOException, JSONException {

        JSONObject jsonObject = new JSONObject();
        for(Map.Entry<String,String> entry : params.entrySet())
            jsonObject.put(entry.getKey(),entry.getValue());

        return jsonObject.toString();

    }

}
