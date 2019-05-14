package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import cz.msebera.android.httpclient.Header;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.ListAlbumsAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;
import pt.ulisboa.tecnico.cmov.p2photo.storage.FileManager;
import pt.ulisboa.tecnico.cmov.p2photo.storage.MemoryCacheManager;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectManager;



public class ListAlbumsActivity extends AppCompatActivity implements SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    ListAlbumsAdapter adapter;
    ListView listView;
    GoogleDriveHandler driveHandler;

    private GlobalVariables globalVariables;

    private WifiDirectManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_albums);
        this.globalVariables = (GlobalVariables)getApplicationContext();



        //Initialize the memory cache manager
        globalVariables.setCacheManager(new MemoryCacheManager(this));


        //Set the toolbar as the ActionBar for this window
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //Set the adapter responsible for showing the list of albums
        adapter = new ListAlbumsAdapter(this, new ArrayList<Album>());
        listView = findViewById(R.id.folders_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openAlbum(i);
            }
        });

        if(globalVariables.google)
            driveHandler = globalVariables.getGoogleDriveHandler();
        else {
            wifiManager =  new WifiDirectManager(this);
            globalVariables.setWifiDirectManager(wifiManager);
            // initialize the WDSim API
            wifiManager.initiateWifi();

            //Create the internal storage file manager
            globalVariables.setFileManager(new FileManager(this));

        }
        findViewById(R.id.idInGroupButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                wifiManager.requestGroupInfo(v);
            }
        });

        getAlbums();


    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
      wifiManager.onGroupInfoAvailable(devices,groupInfo);
    }




    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList) {

    }

    private void openAlbum(int i) {
        Album album = adapter.getItem(i);

        Intent intent = new Intent(ListAlbumsActivity.this, ListPhotosActivity.class);
        intent.putExtra("album", album);
        startActivity(intent);
    }

    /**
     * Send the server request to retrieve the list of the user albums
     */
    private void getAlbums() {

        if(!globalVariables.google){
            adapter.clear();
            adapter.addAll(
                globalVariables.getFileManager().getAlbumsList(globalVariables.getUser().getName())
            );
            adapter.notifyDataSetChanged();
        }else{
            try {
                ServerAPI.getInstance().getUserAlbums(
                        this.getApplicationContext(),
                        globalVariables.getUser().getName(),
                        globalVariables.getToken(),
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                Log.i("valueof", response.toString());
                                try {
                                    transformResults(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                                Log.i("Get Albums", "failed = " + throwable.getMessage());
                                    Toast.makeText(ListAlbumsActivity.this,
                                            ListAlbumsActivity.this.getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.failed_get_albums),
                                            Toast.LENGTH_SHORT)
                                            .show();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                if(statusCode == 401)
                                    ServerAPI.getInstance().tokenInvalid(ListAlbumsActivity.this);

                            }
                        });
            }catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Add the albums received from the server to the list
     * @param response server response
     */
    private void transformResults(JSONArray response) throws JSONException {
        for(int i = 0;i < response.length();i++){
            adapter.add(new Album((String)response.get(i)));
        }
        /*
        //Dummy albums
        adapter.add(new Album("sebas"));
        adapter.add(new Album("andre"));
        adapter.add(new Album("leonor"));*/

    }


    /**
     * Inflates the action bar with the menu options
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Asks the user if he wishes to logout. If yes, performs the logout operation.
     */
    public void logout(MenuItem item) {
        Utils.openYesNoBox(this, "Are you sure you want to logout?", null,new Callable<Void>() {
            public Void call() {
                Intent intent = new Intent(ListAlbumsActivity.this, LoginActivity.class);
                //Clears the activity stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return null;
            }
        }, null);
    }

    /**
     * Opens a dialog box asking the user to insert the name of the album
     * and perform the starts album operation
     */
    public void createAlbum(View view){
        Log.i("Albums", "createAlbum");
        AlertDialog alertDialog = new AlertDialog.Builder(ListAlbumsActivity.this).create();
        alertDialog.setTitle(getString(R.string.create_album));

        //Add field to insert album name
        final EditText input = new EditText(ListAlbumsActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);


        //Set listener for button yes
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if(name.equals("")){
                            Utils.openWarningBox(ListAlbumsActivity.this, null,"Invalid album name");
                            return;
                        }
                        createNewAlbum(name);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    /**
     * Creates the catalog file in the user's google drive and retrieves the share link.
     * Informs the server that a new album was created and adds the new album to the display
     * @param name album name
     */
    private void createNewAlbum(final String name) {
        final String albumName = name;

        if(adapter.contains(name)){

            Toast.makeText(ListAlbumsActivity.this,
                    "Album " + albumName + " already exists.",
                    Toast.LENGTH_SHORT)
                    .show();
            return;

        }

        if(!globalVariables.google) {
            Log.i("ListAlbumsActivity", "Create album in internal storage");
            if(globalVariables.getFileManager()
                    .updateAlbum(globalVariables.getUser().getName(), albumName, "")) {
                createAlbumSuccess(albumName, null);
            }else{
                createAlbumFailure(albumName, ".");
            }
        } else {
                final Task<Pair<String, String>> task = driveHandler.createAlbumSlice(name);
                //Add success listener
                task.addOnSuccessListener(new OnSuccessListener<Pair<String, String>>() {
                    @Override
                    public void onSuccess(Pair<String, String> result) {
                        final String fileID = result.first;
                        final String url = result.second;

                        Log.i("Create album", "url = " + url);
                        Log.i("Create album", "fileID = " + fileID);

                        try {
                            ServerAPI.getInstance().createAlbum(ListAlbumsActivity.this,
                                    globalVariables.getToken(),
                                    globalVariables.getUser().getName(),
                                    name,
                                    url,
                                    fileID,
                                    new JsonHttpResponseHandler() {

                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                            //Add the album to the list
                                            createAlbumSuccess(albumName, fileID);

                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                            Log.i("ListAlbums", "create album server = " + throwable.getMessage());
                                            createAlbumFailure(albumName, ".");
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                            if (statusCode == 401)
                                                ServerAPI.getInstance().tokenInvalid(ListAlbumsActivity.this);

                                            // HTTP Conflict, user already has album with such name
                                            if (statusCode == 409)
                                                createAlbumFailure(albumName, " already exists");

                                        }
                                    });


                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                //Add failure listener
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ListAlbumsActivity.this,
                                "Failed to create album " + albumName + ".",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        }

    private void createAlbumFailure(String albumName, String errorMsg) {
        Toast.makeText(ListAlbumsActivity.this,
                "Failed to create album " + albumName + errorMsg,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void createAlbumSuccess(String albumName, String fileID) {
        Album album = new Album(albumName, fileID);
        adapter.add(album);
        adapter.notifyDataSetChanged();


        Toast.makeText(ListAlbumsActivity.this,
                "Album " + albumName + " created successfully.",
                Toast.LENGTH_SHORT)
                .show();
    }



    public void openAdminMenu(MenuItem item) {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!globalVariables.google)
            wifiManager.unregisterReceiver();
    }


}
