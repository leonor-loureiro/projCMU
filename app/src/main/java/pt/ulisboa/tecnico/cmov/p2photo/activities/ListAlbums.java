package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

    MaterialButton shareButton;
    MaterialButton addPhotoButton;
    FloatingActionButton addButton;
    boolean actionButtonExpanded = false;

    ListAlbumsAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_albums);

        //Set the toolbar as the ActionBar for this window
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Action buttons menu animation
        shareButton = findViewById(R.id.share);
        addPhotoButton = findViewById(R.id.add_photo);
        addButton = findViewById(R.id.add);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(actionButtonExpanded)
                    closeActionMenu();
                else
                    openActionMenu();
            }
        });

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
                //TODO add go to list photos of album
                Toast.makeText(ListAlbums.this, "Open album " + album.getName(), Toast.LENGTH_SHORT).show();
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

    private void closeActionMenu() {
        actionButtonExpanded = false;
        shareButton.setVisibility(View.INVISIBLE);
        shareButton.animate().translationY(0);
        addPhotoButton.setVisibility(View.INVISIBLE);
    }

    private void openActionMenu() {
        actionButtonExpanded = true;
        addPhotoButton.setVisibility(View.VISIBLE);
        shareButton.animate().translationY(-getResources().getDimension(R.dimen.standard_70));
        shareButton.setVisibility(View.VISIBLE);
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
}
