package pt.ulisboa.tecnico.cmov.p2photo.googledrive;

import android.util.Log;
import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.p2photo.googledrive.exception.GoogleDriveException;

public class GoogleDriveHelper {

    //Simplifies the execution of asynchronous tasks
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    //Google Drive service
    private final Drive mDriveService;

    public static String TYPE_ALBUM_SLICE = "text/plain";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String ALBUM_SLICE_EXT = "_album";
    public final Permission pubPermission;


    public GoogleDriveHelper(Drive mDrive) {
        this.mDriveService = mDrive;
        pubPermission = new Permission ();
        pubPermission.setRole("reader");
        pubPermission.setType("anyone");
    }

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
                        .setName(name + ALBUM_SLICE_EXT);

                //Create file
                File driveFile = mDriveService.files().create(metadata).execute();
                if(driveFile == null){
                    throw new GoogleDriveException("Failed to create the album slice");
                }
                String fileID = driveFile.getId();
                String shareURL = shareFile(fileID);
                return Pair.create(fileID, shareURL);
            }
        };

        return Tasks.call(mExecutor, callable);
    }

    private String shareFile(String fileID) throws GoogleDriveException, IOException {
        File driveFile;//Enable link sharing
        Permission permission = mDriveService.permissions().create(fileID, pubPermission).execute();
        if(permission == null)
            throw new GoogleDriveException("Failed to enable link sharing");

        driveFile = mDriveService.files().get(fileID).setFields("webContentLink").execute();
        if(driveFile == null)
            throw new GoogleDriveException("Failed to get file metadata");

        Log.d("Drive", "share link: " + driveFile.getWebContentLink());
        return driveFile.getWebContentLink();
    }

    public Task<String> downloadFile(final String fileUrl) throws IOException {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                StringBuilder content = new StringBuilder();

                URL url = new URL(fileUrl);
                //Create an url connection
                URLConnection urlConnection = url.openConnection();

                //Wrap the url connection in a buffered reader
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                //Read contents
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    content.append(line + "\n");

                bufferedReader.close();
                Log.i("Drive", "Contents: " + content.toString());
                return content.toString();
            }
        };

        return Tasks.call(mExecutor, callable);
    }

    public Task<Void> updateFile(final String fileId, final List<String> contents){
        Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                // Create a File containing any metadata changes.
                File metadata = new File().setName("Test_album");

                // Build contents
                StringBuilder stringBuilder = new StringBuilder();
                for (String line : contents)
                    stringBuilder.append(line + "\n");

                // Convert content to an AbstractInputStreamContent instance.
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", stringBuilder.toString());

                // Update the metadata and contents.
                mDriveService.files().update(fileId, metadata, contentStream).execute();
                Log.d("Drive", "content update");
                return null;
            }
        };
        return Tasks.call(mExecutor, callable);
    }
}
