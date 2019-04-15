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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.Constants;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.ListAlbumsAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;


public class ListAlbumsActivity extends AppCompatActivity {

    ListAlbumsAdapter adapter;
    ListView listView;
    GoogleDriveHandler driveHandler;

    private GlobalVariables globalVariables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_albums);
        this.globalVariables = (GlobalVariables)getApplicationContext();



        //Set the toolbar as the ActionBar for this window
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<Album> albums = new ArrayList<>();
        getAlbums(albums);

        //Set the adapter responsible for showing the list of albums
        adapter = new ListAlbumsAdapter(this, albums);
        listView = findViewById(R.id.folders_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openAlbum(i);
            }
        });


        driveHandler = ((GlobalVariables) getApplicationContext()).getGoogleDriveHandler();


    }

    private void openAlbum(int i) {
        Album album = adapter.getItem(i);

        Intent intent = new Intent(ListAlbumsActivity.this, ListPhotosActivity.class);
        intent.putExtra("album", album);
        startActivity(intent);
    }

    /**
     * Send the server request to retrieve the list of the user albums
     * @param albums
     */
    private void getAlbums(List<Album> albums) {

            //TODO: login operation
        try {
            albums = ServerAPI.getInstance().getUserAlbums(this.getApplicationContext(),globalVariables.getUser().getName(),globalVariables.getToken());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ;
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
        final Task<Pair<String,String>> task = driveHandler.createAlbumSlice(name);

        //Add success listener
        task.addOnSuccessListener(new OnSuccessListener<Pair<String, String>>() {
            @Override
            public void onSuccess(Pair<String, String> result) {

                //TODO: update server

                //Add the album to the list
                Album album = new Album(albumName, result.first);
                adapter.add(album);
                adapter.notifyDataSetChanged();


                Toast.makeText(ListAlbumsActivity.this,
                        "Album " + albumName + " created successfully.",
                        Toast.LENGTH_SHORT)
                        .show();

                try {
                    ServerAPI.getInstance().createAlbum(getApplicationContext(),globalVariables.getToken(),globalVariables.getUser().getName(),name,result.second,result.first);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
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
