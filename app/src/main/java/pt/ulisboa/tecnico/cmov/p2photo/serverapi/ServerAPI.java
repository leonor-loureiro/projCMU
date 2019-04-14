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


    public boolean register(String username, String password){


        //TODO: set token with return from register


        return false;
    }

    public boolean login(Context applicationContext, String username, String password) throws IOException {


        JsonFactory jsonFactory = new JsonFactory();
        Writer writer=new StringWriter();
        JsonGenerator g= jsonFactory.createJsonGenerator(writer);
        g.writeStartObject();
        g.writeStringField("username",username);
        g.writeStringField("password",password);
        g.writeEndObject();
        g.close();

        String json = writer.toString();

        HttpUtils.post(applicationContext,"login", "",new StringEntity(json), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asd", "---------------- this is response : " + response);
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("failed","pepehands");
            }
        });
        return true;
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
     */
    public List<Album> getUserAlbums() {

        return null;
    }

    public void shareAlbum() {


    }

    public boolean createAlbum() {

        return false;
    }



}
