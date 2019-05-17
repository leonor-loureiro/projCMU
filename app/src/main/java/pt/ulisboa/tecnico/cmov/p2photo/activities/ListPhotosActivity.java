package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.crypto.SecretKey;

import cz.msebera.android.httpclient.Header;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.Operation;
import pt.ulisboa.tecnico.cmov.p2photo.data.Photo;
import pt.ulisboa.tecnico.cmov.p2photo.data.PhotoAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.PhotoToSend;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;
import pt.ulisboa.tecnico.cmov.p2photo.security.SecurityManager;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;
import pt.ulisboa.tecnico.cmov.p2photo.storage.MemoryCacheManager;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectManager;

/**
 * This activity is responsible for showing the photos of an album
 */
public class ListPhotosActivity extends AppCompatActivity{

    private static final int GALLERY = 1327;
    private static final int ADD_USER = 200;
    private static final int STORAGE_PERMISSION = 100 ;
    private static final String TAG = "ListPhotosActivity" ;
    MaterialButton shareButton;
    MaterialButton addPhotoButton;
    FloatingActionButton addButton;
    RelativeLayout loadingBarLayout;
    boolean actionButtonExpanded = false;
    //ProgressDialog progressDialog;

    private GlobalVariables globalVariables;


    Album album;
    PhotoAdapter adapter;

    //Handles all google drive operations
    private GoogleDriveHandler driveHandler;

    //Url of the current user's catalog
    private String mCatalogUrl;
    //Contents of the current user's catalog
    private ArrayList<String> mCatalogContent = new ArrayList<>();
    private int nrPhotos = 0;
    private boolean errorDownload;
    private int nrCatalogs = 0;
    private ArrayList<Member> membersInGroup;
    private WifiDirectManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_photos);

        this.globalVariables = (GlobalVariables)getApplicationContext();

        //Get album object
        Intent intent = getIntent();
        album = (Album) intent.getSerializableExtra("album");
        Log.i("List Photos", album.getName());

        loadingBarLayout = findViewById(R.id.loading_bar_layout);

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

        /*progressDialog= ProgressDialog.show(this, "",
                "Loading photos...", true);*/

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


        showLoadingBar();

        if(globalVariables.google)
            getSecretKey();
        else
            getAlbumInfo();


        membersInGroup = this.globalVariables.getMembersInGroup();
        wifiManager = this.globalVariables.getWifiDirectManager();
    }

    /**
     * This method is responsible for download all the photos and displaying then
     */
    private void getAlbumInfo() {
        Log.i("ListPhotos", "getting album's photos");
        try {
            Log.i("ListPhotos", "Checking for updates on server.");

            ServerAPI.getInstance().getGroupMembership(this,
                    globalVariables.getToken(),
                    globalVariables.getUser().getName(),
                    album.getName(),this.globalVariables.google + "",
                    new JsonHttpResponseHandler() {

                        /**
                         * If connection to server is available, get updated urls and users
                         */
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Log.i("ListPhotos", "Correctly got album information from server");

                                updateAlbumInfo(response);

                            } catch (JSONException | UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            Log.i("ListPhotos", "failed to get group membership = " + throwable.getMessage());
                            getAlbumPhotosFailure();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            if (statusCode == 401)
                                ServerAPI.getInstance().tokenInvalid(ListPhotosActivity.this);
                        }


                    });

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    private void loadP2PAlbum()
    {
        globalVariables.addOperation(new Operation("loadP2PAlbum",globalVariables.getUser().getName(),album.getName(),globalVariables.google).toString());
        Log.i(TAG, "Load P2P album: ");
        if(album.getFileID() != null && !album.getFileID().equals("null")) {
            List<Photo> photos = globalVariables.getFileManager().getAlbumPhotos(album.getFileID());
            if (photos != null) {
                adapter.clear();
                adapter.addAllPhotos(photos);
            } else
                getAlbumPhotosFailure();
        }
        hideLoadingBar();
    }

    private void getAlbumPhotosFailure() {
        Toast.makeText(ListPhotosActivity.this,
                ListPhotosActivity.this.getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.failed_get_photos),
                Toast.LENGTH_SHORT)
                .show();
    }


    /**
     * Downloads the album catalogs
     * @param urls list of catalogs to download
     */
    public void downloadAlbumCatalogs(List <String> urls){
        getCachedCloudPhotos(album);
        globalVariables.addOperation(new Operation("downloadAlbumCatalogs",globalVariables.getUser().getName(),album.getName(),globalVariables.google).toString());

        Log.i("ListPhotos", "download album catalogs #" + urls.size());
        //If no catalog dismiss progress
        if(urls.isEmpty())
            hideLoadingBar();


        for(final String url : urls){
            if(url == null || url.equals("null"))
                continue;

            nrCatalogs++;

            Task<List<String>> task = driveHandler.downloadFile(url);
            task.addOnSuccessListener(new OnSuccessListener<List<String>>() {
                @Override
                public void onSuccess(List<String> catalogUrls) {
                    Log.i("ListPhotos",
                            "SUCCESS: download album catalog = " + url + " -> " + catalogUrls.size()) ;

                    //Save the catalog content of the current user
                    if(url.equals(mCatalogUrl)) {
                        mCatalogContent = new ArrayList<>();
                        mCatalogContent.addAll(catalogUrls);
                    }

                    downloadPhotos(catalogUrls);
                    downloadCatalogsFinished();

                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("ListPhotos", "FAILED: download album catalog = " + url);
                    e.printStackTrace();
                    downloadCatalogsFinished();
                    errorDownload = true;
                }
            });
        }
    }

    private void getCachedCloudPhotos(Album album) {
        MemoryCacheManager cacheManager = globalVariables.getCacheManager();

        List<Photo> cachedPhotos = cacheManager.getAlbumPhotos("", album.getName(), globalVariables.google);

        for(Photo photo : cachedPhotos){
            if(!adapter.contains(photo))
                adapter.addPhoto(photo);
        }
        Log.i(TAG, "Cloud cached photos = " + cachedPhotos.size());
    }

    private void downloadCatalogsFinished() {
        nrCatalogs--;
        if(nrCatalogs == 0 && nrPhotos == 0) {
            hideLoadingBar();
            if(errorDownload)
                Toast.makeText(this,
                        getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.failed_load_photos),
                        Toast.LENGTH_SHORT)
                        .show();
        }
    }

    /**
     * Download album photos
     * @param urls list of photo's to download
     */
    public void downloadPhotos(List<String> urls) {
        final MemoryCacheManager cacheManager = globalVariables.getCacheManager();

        nrPhotos += urls.size();
        Log.i("ListPhotos", "downloading photos from google drive");

        for(final String url : urls) {
            Task<Bitmap> task =  driveHandler.downloadPhoto(url);
            task.addOnSuccessListener(new OnSuccessListener<Bitmap>() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    String id = Uri.parse(url).getQueryParameter("id");
                    Photo photo = new Photo(id, bitmap);
                    if(!adapter.contains(photo))
                        adapter.addPhoto(photo);

                    cacheManager.addAlbumPhoto("", album.getName(), photo, globalVariables.google);
                    photosDownloadFinished();
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("ListPhotos", "failed to download photo = " + url);
                    e.printStackTrace();
                    errorDownload = true;
                    photosDownloadFinished();
                }
            });
        }
    }

    private void photosDownloadFinished() {
        nrPhotos--;
        if(nrPhotos == 0) {
            hideLoadingBar();
            if(errorDownload)
                Toast.makeText(this,
                        getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.failed_load_photos),
                        Toast.LENGTH_SHORT)
                .show();
        }
    }


    /**
     * Updates the album information based on response from server
     */
    private void updateAlbumInfo(JSONObject resp) throws JSONException, UnsupportedEncodingException {

        String username, url;
        String currentUser = globalVariables.getUser().getName();
        String albumName = album.getName();

        Log.i("album", "updating album information");


        List<String> catalogUrls = new ArrayList<>();
        List<Member> albumMembers = new ArrayList<>();

        for(int i = 0;i < resp.names().length();i++){
            username = resp.names().getString(i);
            url = resp.getString(username);

            //If url is not defined, don't decrypt
            if(url != null && !url.equals("null"))
                url = SecurityManager.decryptAES(album.getSecretKey(), url);

            if(currentUser.equals(username)){
                //If the user's catalog ID is not yet defined, create album and update sever
                if(album.getFileID() == null || album.getFileID().equals("null")){
                    Log.i("ListPhotos", "updateAlbumInfo -> " + currentUser + " slice not initialize");
                    url = null;
                    if(globalVariables.google)
                        updateSharedCloudAlbum(albumName, album.getSecretKey());
                    else
                        updateSharedP2PAlbum(albumName);
                }
                mCatalogUrl = url;
            }else{

                albumMembers.add(new Member(username));
            }
            catalogUrls.add(url);
        }

        album.setGroupMembership(catalogUrls);
        album.setMembers(albumMembers);



        if(globalVariables.google) {
            //Download album
            downloadAlbumCatalogs(album.getGroupMembership());

        }else{
            loadP2PAlbum();
            Log.d("MembersInRange",membersInGroup.size() + "");
            handleMembers();
        }
    }

    private void updateSharedP2PAlbum(String albumName) {
        globalVariables.addOperation(new Operation("updateSharedP2PAlbum",globalVariables.getUser().getName(),albumName,globalVariables.google).toString());
        String filename = globalVariables.getFileManager()
                .createAlbum(globalVariables.getUser().getName(), albumName);
        if(filename != null){
            updateSharedAlbumInServer(null, filename, albumName);
        }
    }

    /**
     * When we update an album we must check if any users in the current wifidirect group, have any of the albums we possess so we can ask
     * for their photos.
     */
    private void handleMembers() {

        MemoryCacheManager cacheManager = globalVariables.getCacheManager();


        for(Member memberOfAlbum : album.getMembers()){

            int i = membersInGroup.indexOf(memberOfAlbum);
            if(i != -1){
                //Member in group: request photos
                if(globalVariables.getSimWifiP2pInfo().askIsConnectionPossible(membersInGroup.get(i).getDeviceName())){
                    globalVariables.addOperation(new Operation("askedforphotos",memberOfAlbum.getName(),album.getName()).toString());
                    askForPhotos(membersInGroup.get(i));
                }
                else {
                    globalVariables.addOperation(new Operation("couldntaskforphotos",memberOfAlbum.getName(),album.getName()).toString());
                    Toast.makeText(this, getString(R.string.force_p2p), Toast.LENGTH_SHORT).show();
                }


            }else{
                //Member not in group: get cached photos
                Log.i(TAG, "Get cached photos from: " + memberOfAlbum.getName());
                List<Photo> cachedPhotos = cacheManager.getAlbumPhotos(memberOfAlbum.getName(), album.getName(), globalVariables.google);
                Log.i(TAG, "Add cached photos: " + cachedPhotos.size());
                adapter.addAllPhotos(cachedPhotos);
            }
        }

    }

      /**
     * Ask a device for its photos of a certain album
     * @param member
     */
    private void askForPhotos(Member member) {

        wifiManager.send(member.getIp(), member.getName(), album.getName(),this);
    }

    public void getSecretKey(){
        try {
            ServerAPI.getInstance().getSecretKey(this,
                    globalVariables.getToken(),
                    globalVariables.getUser().getName(),
                    album.getName(),
                    new JsonHttpResponseHandler() {

                //TODO: check when server is down
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            try {
                                Log.i(TAG, "Secret Key = " + response.get(0));

                                PrivateKey privateKey = SecurityManager.getPrivateKey( globalVariables.getUser().getName());

                                //Decipher secret key
                                byte[] encodedKey = SecurityManager.decryptRSA(privateKey,(String) response.get(0));
                                //Set album secret key
                                album.setSecretKey(SecurityManager.getSecretKeyFromBytes(encodedKey));

                                getAlbumInfo();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            if(statusCode == 401)
                                ServerAPI.getInstance().tokenInvalid(ListPhotosActivity.this);

                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the information of a album that was shared with user but not yet setted
     */
    private void updateSharedCloudAlbum(String name, final SecretKey secretKey){

        globalVariables.addOperation(new Operation("updateSharedCloudAlbum",globalVariables.getUser().getName(),album.getName(),globalVariables.google).toString());


        final String albumName = name;
        final Task<Pair<String,String>> task = driveHandler.createAlbumSlice(name);

        Log.i("UpdateSharedAlbum", albumName);

        //Add success listener
        task.addOnSuccessListener(new OnSuccessListener<Pair<String, String>>() {
            @Override
            public void onSuccess(Pair<String, String> result) {

            //Encrypt url
            final String url = SecurityManager.encryptAES(secretKey, result.second);
            final String fileID = result.first;


            Log.i("UpdateSharedAlbum", "fileID = " + fileID);
            Log.i("UpdateSharedAlbum", "url = " + url);

                updateSharedAlbumInServer(url, fileID, albumName);
            }
        });

        //Add failure listener
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });



    }

    private void updateSharedAlbumInServer(String url, String fileID, String albumName) {
        Log.i(TAG, "Update shared album " + albumName + ": " + fileID + "/" + url);
        try {
            //Send url and fileID to the server
            ServerAPI.getInstance().updateAlbum(getApplicationContext(),
                    globalVariables.getToken(),
                    globalVariables.getUser().getName(),
                    albumName,url,fileID, globalVariables.google + "");

            //Set album file ID
            album.setFileID(fileID);
            globalVariables.updateFileID(albumName, fileID);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
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
        startActivityForResult(intent, ADD_USER);
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
                if(!globalVariables.google)
                    globalVariables.getWifiDirectManager().unbindService();
                Intent intent = new Intent(ListPhotosActivity.this, LoginActivity.class);
                //Clears the activity stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return null;
            }
        }, null);
      /*  if(!globalVariables.google)
            wifiManager.unregister */
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

        if(requestCode == ADD_USER){
            album = (Album) data.getSerializableExtra("album");
        }
        // receives photo from gallery
        else if (requestCode == GALLERY) {
            if (data == null)
                return;

            final Uri photoUri = data.getData();

            showLoadingBar();

            if (!globalVariables.google) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    if(globalVariables.getFileManager()
                            .addPhotoToAlbum(globalVariables.getUser().getName(), album.getName(), bitmap))
                        adapter.addPhoto(new Photo("", bitmap));
                    hideLoadingBar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                //Get real file path
                String filePath = Utils.getPath(this, photoUri);

                //Upload photo to google drive
                Task<String> task = driveHandler.addPhotoToAlbum(
                        album.getFileID(),
                                    mCatalogContent,
                                    filePath);

                //Add on success listener
                task.addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String url) {
                        addPhotoSuccess(url, photoUri);
                        hideLoadingBar();


                    }
                });

                //Add on failure listener
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoadingBar();
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

    public void showLoadingBar() {
        //Show loading bar
        loadingBarLayout.setVisibility(View.VISIBLE);
        //Hide add button
        addButton.setVisibility(View.INVISIBLE);
    }

    public void hideLoadingBar() {
        //Show loading bar
        loadingBarLayout.setVisibility(View.INVISIBLE);
        //Hide add button
        addButton.setVisibility(View.VISIBLE);
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

            Log.i(TAG, "Add photo: " + url);
            Log.i(TAG, mCatalogContent.toString());
            //Save photo in current catalog content
            mCatalogContent.add(url);

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


    public void addPhotos(String username, String albumName, ArrayList<PhotoToSend> result) {
        MemoryCacheManager cacheManager = globalVariables.getCacheManager();

        for(PhotoToSend photo : result){
           Photo newPhoto = new Photo(photo.getUrl(),Utils.decodeBitmap(photo.getBitmap()));
           newPhoto.setMine(false);
           adapter.addPhoto(newPhoto);
           cacheManager.addAlbumPhoto(username, albumName, newPhoto, globalVariables.google);
        }
    }

    public void syncPhotos(MenuItem item) {
        getAlbumInfo();
    }

}
