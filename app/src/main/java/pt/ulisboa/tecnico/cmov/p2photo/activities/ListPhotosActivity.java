package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.Photo;
import pt.ulisboa.tecnico.cmov.p2photo.data.PhotoAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;

/**
 * This activity is responsible for showing the photos of an album
 */
public class ListPhotosActivity extends AppCompatActivity {

    private static final int GALLERY = 1327;
    private static final int STORAGE_PERMISSION = 100 ;
    MaterialButton shareButton;
    MaterialButton addPhotoButton;
    FloatingActionButton addButton;
    boolean actionButtonExpanded = false;

    Album album;
    PhotoAdapter adapter;

    //Handles all google drive operations
    private GoogleDriveHandler driveHandler;


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

        driveHandler = ((GlobalVariables) getApplicationContext()).getGoogleDriveHandler();

        //Set the adapter for the photos grid view
        GridView gridView = findViewById(R.id.photos_grid);
        adapter = new PhotoAdapter(this, new ArrayList<Photo>());
        gridView.setAdapter(adapter);

        List<String> urls = new ArrayList<>();
        urls.add("https://drive.google.com/uc?id=1DczyIArphZj8gRcmi4050zAmnGBigVO4&export=download");
        getAlbumPhotos(urls);

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

    /**
     * This method is responsible for download all the photos and displaying then
     * @param urls list of photo's urls
     */
    private void getAlbumPhotos(List<String> urls) {
        for(final String url : urls) {
            driveHandler.downloadPhoto(url).addOnSuccessListener(new OnSuccessListener<Bitmap>() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    Log.i("Drive", "OnSuccessListener");
                    adapter.addPhoto(new Photo(url, bitmap));
                }
            });
        }
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
     * This function starts the activity responsible for adding a new user
     * @param view
     */
    public void addUserScreen(View view) {
        Intent intent = new Intent(ListPhotosActivity.this, AddUserActivity.class);
        intent.putExtra("album",album);
        startActivity(intent);
    }

    /**
     * This function adds the menu options to the toolbar
     * @param menu
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
                Intent intent = new Intent(ListPhotosActivity.this, LoginActivity.class);
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
        Intent intent = new Intent(this, ListAlbumsActivity.class);
        startActivity(intent);
    }

    /**
     * Adds a photo from user's photo gallery
     * @param view
     */
    public void onClickAddPhoto(View view) {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Request permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION);
        }else {
            Log.d("Permission", "Already has permission");

            openGallery();
        }
    }

    /**
     * Opens the device's gallery to allow the user to choose a photo
     */
    private void openGallery() {
        closeActionMenu();
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    /**
     * Handles the result from the open gallery
     * @param data contains the result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // receives photo from gallery
        if (requestCode == GALLERY) {
            if (data != null) {
                final Uri photoUri = data.getData();

                //Get real file path
                String filePath = Utils.getPath(this, photoUri);

                //Upload photo to google drive
                Task<String> task = driveHandler.addPhotoToAlbum(album.getFileID(), new ArrayList<String>(), filePath);

                //Add on success listener
                task.addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String url) {
                        addPhotoSuccess(url, photoUri);
                    }
                });

                //Add on failure listener
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ListPhotosActivity.this,
                                "Failed to add photo to album.",
                                Toast.LENGTH_SHORT)
                                .show();
                        e.printStackTrace();
                    }
                });

            }
        }
    }

    /**
     * Displays the photo after it has been uploaded
     * @param url download url of the photo
     * @param photoUri uri of the photo on the device
     */
    public void addPhotoSuccess(String url, Uri photoUri) {
        try {
            //Display photo
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(ListPhotosActivity.this.getContentResolver(), photoUri);
            adapter.addPhoto(new Photo(url, bitmap));

            Toast.makeText(ListPhotosActivity.this,
                    "Photo successfully added to album.",
                    Toast.LENGTH_SHORT)
                    .show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the result from the permissions request
     * @param grantResults result of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION) {

            //If permission is granted open gallery
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                //If permission not granted, inform user he wont be able to add photos
                Toast.makeText(this, "Permissions to read the storage was denied. Can't add photos.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
