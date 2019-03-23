package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.Photo;
import pt.ulisboa.tecnico.cmov.p2photo.data.PhotoAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;

/**
 * This activity is responsible for showing the photos of an album
 */
public class ListPhotos extends AppCompatActivity {

    MaterialButton shareButton;
    MaterialButton addPhotoButton;
    FloatingActionButton addButton;
    boolean actionButtonExpanded = false;

    Album album;
    PhotoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_photos);

        //Get album object
        Intent intent = getIntent();
        album = (Album) intent.getSerializableExtra("album");
        Log.i("List Photos", album.getName());

        //Set the toolbar as the ActionBar for this window
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView album_name = findViewById(R.id.album_name);
        album_name.setText(album.getName());

        List<Photo> photos = new ArrayList<>();
        getAlbumPhotos(photos);

        //Set the adapter for the photos grid view
        GridView gridView = findViewById(R.id.photos_grid);
        adapter = new PhotoAdapter(this, photos);
        gridView.setAdapter(adapter);

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
    }

    private void getAlbumPhotos(List<Photo> photos) {
        //TODO Network request
        photos.add(new Photo("url1", null));
        photos.add(new Photo("url2", null));
        photos.add(new Photo("url3", null));
        photos.add(new Photo("url4", null));
        photos.add(new Photo("url5", null));
    }

    /**
     * This function is responsible for hiding the option in
     * the expandable menu
     */
    private void closeActionMenu() {
        actionButtonExpanded = false;
        shareButton.setVisibility(View.INVISIBLE);
        shareButton.animate().translationY(0);
        addPhotoButton.setVisibility(View.INVISIBLE);
    }

    /**
     * This function is responsible for expanding the menu
     */
    private void openActionMenu() {
        actionButtonExpanded = true;
        addPhotoButton.setVisibility(View.VISIBLE);
        shareButton.animate().translationY(-getResources().getDimension(R.dimen.standard_70));
        shareButton.setVisibility(View.VISIBLE);
    }

    /**
     * This function starts the activity responsable for adding a new user
     * @param view
     */
    public void addUser(View view) {
        Intent intent = new Intent(ListPhotos.this, AddUserActivity.class);
        startActivity(intent);
    }

    /**
     * This function adds the menu options to the toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_photos, menu);
        return true;
    }

    /**
     * This function performs the logout operation
     * @param item
     */
    public void logout(MenuItem item) {
        Utils.openYesNoBox(this, "Are you sure you want to logout?", null,new Callable<Void>() {
            public Void call() {
                Intent intent = new Intent(ListPhotos.this, LoginActivity.class);
                //Clears the activity stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return null;
            }
        }, null);
    }

    /**
     * This function returns to the list albums
     * @param item
     */
    public void startListAlbums(MenuItem item) {
        Intent intent = new Intent(this, ListAlbums.class);
        startActivity(intent);
    }
}
