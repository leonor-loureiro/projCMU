package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.ListAlbumsAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;


public class ListAlbums extends AppCompatActivity {

    ListAlbumsAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_albums);

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
                Album album = (Album) adapter.getItem(i);
                Intent intent = new Intent(ListAlbums.this, ListPhotos.class);
                intent.putExtra("album", album);
                startActivity(intent);
            }
        });
    }

    private void getAlbums(List<Album> albums) {
        //Dummy albums
        albums.add(new Album("Album 1", "url1"));
        albums.add(new Album("Album 2", "url2"));
        albums.add(new Album("Album 3", "url3"));
        albums.add(new Album("Album 4", "url4"));
    }


    // Inflates the action bar with the menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void logout(MenuItem item) {
        Utils.openYesNoBox(this, "Are you sure you want to logout?", null,new Callable<Void>() {
            public Void call() {
                Intent intent = new Intent(ListAlbums.this, LoginActivity.class);
                //Clears the activity stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return null;
            }
        }, null);
    }

    public void createAlbum(View view){
        Log.i("Albums", "createAlbum");
        AlertDialog alertDialog = new AlertDialog.Builder(ListAlbums.this).create();
        alertDialog.setTitle(getString(R.string.create_album));

        //Add field to insert album name
        final EditText input = new EditText(ListAlbums.this);
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
                            Utils.openWarningBox(ListAlbums.this, null,"Invalid album name");
                            return;
                        }
                        createNewAlbum(name);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    private void createNewAlbum(String name) {
        //TODO move this to post execute network request
        Album album = new Album(name, "url_" + name);
        adapter.add(album);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Album " + name + "created successfully.", Toast.LENGTH_SHORT).show();
    }


}
