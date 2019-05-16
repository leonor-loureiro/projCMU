package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.MembersAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class AddUserActivity extends AppCompatActivity {


    private static final String TAG = "AddUserActivity";
    MembersAdapter adapter;

    MembersAdapter adapterU;

    ListView listViewMembers;

    ListView listViewAllUsers;

    private Album album;

    private GlobalVariables globalVariables;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        this.globalVariables = (GlobalVariables)getApplicationContext();

        //Get album object
        Intent intent = getIntent();
        album = (Album) intent.getSerializableExtra("album");


        //Create adapter for the album members list
        adapter = new MembersAdapter(this,album.getMembers(),R.layout.member_row,album,null);
        listViewMembers = findViewById(R.id.members);
        listViewMembers.setAdapter(adapter);

        //Create adapter for the application user's list
        adapterU = new MembersAdapter(this,new ArrayList<Member>(),R.layout.add_user_row,album,adapter);
        listViewAllUsers= findViewById(R.id.allusers);
        listViewAllUsers.setAdapter(adapterU);

        //Get list of all users from server
        try {
            getAllMembers();
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }



        int id = findViewById(R.id.searchView).getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        EditText filterText = findViewById(R.id.searchView).findViewById(id);

        //Create text changed listener for search bar
        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AddUserActivity.this.adapterU.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    /**
     * Gets the members in the system from the server
     * @throws UnsupportedEncodingException
     * @throws JSONException
     */

    private void getAllMembers() throws UnsupportedEncodingException, JSONException {

        ServerAPI.getInstance().getUsers(this.getApplicationContext(),
                globalVariables.getToken(),
                globalVariables.getUser().getName(),
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            Log.i("ListPhotos", "Correctly got album information from server");

                            extractAllMembers(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {

                            Log.i("Get users", "FAILED: " + throwable.getMessage());
                                Toast.makeText(AddUserActivity.this,
                                    AddUserActivity.this.getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.failed_get_users),
                                    Toast.LENGTH_SHORT)
                                    .show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        if(statusCode == 401)
                            ServerAPI.getInstance().tokenInvalid(AddUserActivity.this);

                    }
                });

    }

    /**
     * parses the response from the server, and adds the Users in the system to the adapter
     * @param response
     */
    private void extractAllMembers(JSONObject response) throws JSONException {

        String currentUser = globalVariables.getUser().getName();
        for(int i = 0;i < response.names().length();i++){
            String username = response.names().getString(i);
            String publicKey = response.getString(username);
            Log.i(TAG, "User: " + username + "/" + publicKey);
                if(currentUser.equals(username))
                    continue;
                adapterU.add(new Member(username, publicKey));
        }
    }


    public void goBackToAlbums(View view) {
        //Start first activity
        Intent intent = new Intent(this, ListPhotosActivity.class);
        intent.putExtra("album", album);
        setResult(0, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBackToAlbums(null);
    }


}
