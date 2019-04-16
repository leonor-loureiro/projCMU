package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.MembersAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class AddUserActivity extends AppCompatActivity {


    ArrayList <String> listUsers = new ArrayList<String>();

    MembersAdapter adapter;

    MembersAdapter adapterU;

    ListView listViewMembers;

    ListView listViewAllUsers;

    private EditText filterText;

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
        List<Member> members = new ArrayList<>();



        adapter = new MembersAdapter(this,members,R.layout.member_row,album.getName(),null);

        try {
            getAlbumMembers();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listViewMembers = findViewById(R.id.members);

        listViewMembers.setAdapter(adapter);

        List<Member> membersToAdd = new ArrayList<>();

        adapterU = new MembersAdapter(this,membersToAdd,R.layout.add_user_row,album.getName(),adapter);

        listViewAllUsers= findViewById(R.id.allusers);

        listViewAllUsers.setAdapter(adapterU);

        try {
            getAllMembers();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        int id = findViewById(R.id.searchView).getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        filterText = findViewById(R.id.searchView).findViewById(id);

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

    private void getAlbumMembers() throws UnsupportedEncodingException, JSONException {

        ServerAPI.getInstance().getGroupMembership(this.getApplicationContext(),globalVariables.getToken(),globalVariables.getUser().getName(),album.getName(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    changeAlbumMembers(response);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });

    }

    private void changeAlbumMembers(JSONObject response) throws UnsupportedEncodingException, JSONException {

        String link;
        String currentUser = globalVariables.getUser().getName();
        for(int i = 0;i < response.names().length();i++){
            if(currentUser.equals((String)response.names().get(i)))
                continue;
            adapter.add(new Member((String) response.names().get(i)));
            link = response.getString(response.names().getString(i));

            List<String> newMembership = album.getGroupMembership();

            newMembership.add(link);

            album.setGroupMembership(newMembership);
        }

        ServerAPI.getInstance().getFileID(this.getApplicationContext(),globalVariables.getToken(),globalVariables.getUser().getName(),album.getName(),new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    Log.i("newFileID", (String) response.get(0));
                    album.setFileID((String) response.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        });


    }

    private void getAllMembers() throws UnsupportedEncodingException, JSONException {

        ServerAPI.getInstance().getUsers(this.getApplicationContext(),globalVariables.getToken(),globalVariables.getUser().getName(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    changeAllMembers(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    private void changeAllMembers(JSONArray response) throws JSONException {

        String currentUser = globalVariables.getUser().getName();
        for(int i = 0;i < response.length();i++){
                if(currentUser.equals(response.get(i)))
                    continue;

                adapterU.add(new Member((String) response.get(i)));

        }
    }


    public void goBackToAlbums(View view) {
        finish();
    }


}
