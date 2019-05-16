package pt.ulisboa.tecnico.cmov.p2photo.googledrive;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoginActivity;
import pt.ulisboa.tecnico.cmov.p2photo.security.SecurityManager;

/**
 * This class implements all the google drive file operations
 */
public class GoogleDriveHandler {

    //Simplifies the execution of asynchronous tasks
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    //Google Drive service
    private final Drive mDriveService;

    //Permission to share with anyone with the link
    private final Permission pubPermission;

    //Random generator for the file names
    private final Random random;

    //Constants
    private final static String TYPE_ALBUM_SLICE = "text/plain";
    private final static String TYPE_PHOTO = "image/jpeg";
    private final static String ALBUM_SLICE_EXT = "_album_";
    private final static String TAG = "GoogleDriveHandler";


    public GoogleDriveHandler(Drive mDrive) {
        this.mDriveService = mDrive;
        random = new Random();
        pubPermission = new Permission ();
        pubPermission.setRole("reader");
        pubPermission.setType("anyone");
    }

    /******************************************************************
     *                Application specific functions
     *****************************************************************/


    /**
     * This method creates a text file that will be the album slice
     * @param name album name
     * @return the share link for the file
     */
    public Task<Pair<String,String>> createAlbumSlice(final String name){

        Callable<Pair<String,String>> callable = new Callable<Pair<String,String>>() {
            @Override
            public Pair<String,String> call() throws Exception {
                File metadata = new File()
                        .setParents(Collections.singletonList("root"))  //place slice at root
                        .setMimeType(TYPE_ALBUM_SLICE)
                        .setName(name + ALBUM_SLICE_EXT + random.nextInt(90000)); //ensure no duplicate files

                //Create file
                File driveFile = mDriveService.files().create(metadata).execute();
                if(driveFile == null){
                    throw new IOException("Failed to create the album slice");
                }
                String fileID = driveFile.getId();
                String shareURL = shareFile(fileID);
                return Pair.create(fileID, shareURL);
            }
        };

        return Tasks.call(mExecutor, callable);
    }


    /**
     * Downloads a file from google drive
     * @param fileUrl download url
     * @return content as a list of strings
     */
    public Task<List<String>> downloadFile(final String fileUrl) {
        Callable<List<String>> callable = new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {

                List<String> content = new ArrayList<>();

                URL url = new URL(fileUrl);
                //Create an url connection
                URLConnection urlConnection = url.openConnection();

                //Wrap the url connection in a buffered reader
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                //Read contents
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    content.add(line);

                bufferedReader.close();
                Log.i("Drive", "Contents: " + content.toString());
                return content;
            }
        };

        return Tasks.call(mExecutor, callable);
    }

    /**
     * Uploads a photo to the google drive, and enables link sharing.
     * Adds the photo download url to the catalog file.
     * @param albumFileID catalog file ID
     * @param photos list of photos urls from the catalog
     * @param photoPath path of the photo to be uploaded in the device
     * @return photo download link
     */
    public Task<String> addPhotoToAlbum(final String albumFileID, List<String> photos, final String photoPath){

        // Build contents
        final StringBuilder stringBuilder = new StringBuilder();
        for (String line : photos)
            stringBuilder.append(line).append("\n");

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                //Upload photo
                String photoUrl = uploadPhoto(photoPath);

                //Add photo's url to album catalog
                stringBuilder.append(photoUrl);

                //Update album catalog
                updateFile(albumFileID, stringBuilder.toString());

                return photoUrl;
            }
        };

        return Tasks.call(mExecutor, callable);
    }

    /**
     * Download a photo
     * @param photoUrl photo download url
     * @return photo's bitmap
     */
    public Task<Bitmap> downloadPhoto(final String photoUrl){
        Callable<Bitmap> callable = new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {

                URL url = new URL(photoUrl);

                //Create an url connection
                URLConnection urlConnection = url.openConnection();

                //Get input stream
                InputStream input = urlConnection.getInputStream();

                Log.i("Drive", "input = " + (input==null));

                //Convert the stream to a bitmap
                Bitmap bitmap =  BitmapFactory.decodeStream(input);
                Log.i("Drive", "bitmap = " + (bitmap == null));
                return bitmap;
            }
        };

        return Tasks.call(mExecutor,callable);
    }

    /**********************************************************************
     *       Generic functions that manage google drive files
     *********************************************************************/


    /**
     * Update a file's contents
     * @param fileId google drive ID of the file
     * @param contents updated contents of the file
     */
    private void updateFile(final String fileId, final String contents) throws IOException {

        // Create a File containing any metadata changes.
        File metadata = new File();

        // Convert content to an AbstractInputStreamContent instance.
        ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", contents);

        // Update the metadata and contents.
        mDriveService.files().update(fileId, metadata, contentStream).execute();
        Log.d("Drive", "content update");
    }


    /**
     * Uploads an image file to google drive
     * @param path path of the file in the device
     * @return image file'd google drive file ID
     */
    private String uploadPhoto(final String path) throws IOException {
        //Get the file to upload
        java.io.File filePath = new java.io.File(path);

        //Create the metadata
        File metadata = new File()
                .setParents(Collections.singletonList("root"))  //place slice at root
                .setName(filePath.getName());

        //Create contents
        FileContent content = new FileContent(TYPE_PHOTO,filePath);

        //Create file
        File file = mDriveService.files().create(metadata, content).execute();
        String fileID = file.getId();

        //Enable link sharing
        return shareFile(fileID);
    }

    /**
     * Enables link sharing of a file
     * @param fileID google drive file ID
     * @return download link of the file
     */
    private String shareFile(String fileID) throws IOException {
        File driveFile;//Enable link sharing
        Permission permission = mDriveService.permissions().create(fileID, pubPermission).execute();
        if(permission == null)
            throw new IOException("Failed to enable link sharing");

        driveFile = mDriveService.files().get(fileID).setFields("webContentLink").execute();
        if(driveFile == null)
            throw new IOException("Failed to get file metadata");

        Log.d("Drive", "share link: " + driveFile.getWebContentLink());
        return driveFile.getWebContentLink();
    }

}
