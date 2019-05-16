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
import android.widget.Button;
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
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.crypto.SecretKey;

import cz.msebera.android.httpclient.Header;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.ListAlbumsAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;
import pt.ulisboa.tecnico.cmov.p2photo.security.SecurityManager;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;
import pt.ulisboa.tecnico.cmov.p2photo.storage.FileManager;
import pt.ulisboa.tecnico.cmov.p2photo.storage.MemoryCacheManager;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectManager;



public class ListAlbumsActivity extends AppCompatActivity
        implements SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    private static final String TAG = "ListAlbumsActivity";
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
        Button connectButton = findViewById(R.id.idInGroupButton);
        if(globalVariables.google){
            connectButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    wifiManager.requestGroupInfo(v);
                }
            });
        }else{
            connectButton.setVisibility(View.INVISIBLE);
        }

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

        /*if(!globalVariables.google){
            adapter.clear();
            adapter.addAll(
                globalVariables.getFileManager().getAlbumsList(globalVariables.getUser().getName())
            );
            adapter.notifyDataSetChanged();
        }else{*/
            try {
                ServerAPI.getInstance().getUserAlbums(
                        this.getApplicationContext(),
                        globalVariables.getUser().getName(),
                        globalVariables.getToken(),this.globalVariables.google + "",
                        new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    Log.i(TAG, "user albums response: " + response.toString());
                                    if(response.length() != 0)
                                        transformResults(response);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            /*@Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                try {
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }*/

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
        //}

    }

    /**
     * Add the albums received from the server to the list
     * @param response server response
     */
    private void transformResults(JSONObject response) throws JSONException {
        String albumName = null, fileID = null;
        for(int i = 0;i < response.names().length();i++){
            albumName = response.names().getString(i);
            fileID = response.getString(albumName);
            Log.i(TAG, "Album " + albumName + ": " + fileID);
            adapter.add(new Album(albumName, fileID));
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
     * @param albumName album name
     */
    private void createNewAlbum(final String albumName) {

        if(adapter.contains(albumName)){

            Toast.makeText(ListAlbumsActivity.this,
                    "Album " + albumName + " already exists.",
                    Toast.LENGTH_SHORT)
                    .show();
            return;

        }

        if(!globalVariables.google) {
            createP2PAlbum(albumName);
        } else {
            createCloudAlbum(albumName);
        }
    }

    private void createCloudAlbum(final String albumName) {
        Log.i(TAG, "Create Cloud Album");

        final Task<Pair<String, String>> task = driveHandler.createAlbumSlice(albumName);
        //Add success listener
        task.addOnSuccessListener(new OnSuccessListener<Pair<String, String>>() {
            @Override
            public void onSuccess(Pair<String, String> result) {
                final String fileID = result.first;
                final String url = result.second;

                Log.i("Create album", "url = " + url);
                Log.i("Create album", "fileID = " + fileID);

                createAlbumInServer(fileID, url, albumName);
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

    private void createAlbumInServer(final String fileID, String url, final String albumName) {
        String cipheredKey = null;
        if(globalVariables.google) {
            //Generate a secret key
            SecretKey secretKey = SecurityManager.generateSecretKey();
            //Encrypt the url
            url = SecurityManager.encryptAES(secretKey, url);
            Log.i(TAG, "Encrypted url = " + url);

            //TODO: public loaded at login
            PublicKey publicKey = SecurityManager.getPublicKey(globalVariables.getUser().getName());
            cipheredKey = SecurityManager.encryptRSA(publicKey, secretKey.getEncoded());
            Log.i(TAG, "Encrypted secret key = " + cipheredKey);
        }

        try {
            ServerAPI.getInstance().createAlbum(ListAlbumsActivity.this,
                    globalVariables.getToken(),
                    globalVariables.getUser().getName(),
                    albumName,
                    url,
                    fileID,
                    cipheredKey,
                    this.globalVariables.google + "",

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

    private void createP2PAlbum(String albumName) {
        Log.i(TAG, "Create P2P Album");
        String filename = globalVariables.getFileManager()
                .updateAlbum(globalVariables.getUser().getName(), albumName, "");
        if(filename != null){
            createAlbumInServer(filename, null, albumName);
        }else{
            createAlbumFailure(albumName, ".");
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

    /*@Override
    public void onPause() {
        super.onPause();
        if(!globalVariables.google)
            wifiManager.unregisterReceiver();
    }*/




}
