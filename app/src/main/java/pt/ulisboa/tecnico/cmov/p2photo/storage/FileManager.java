package pt.ulisboa.tecnico.cmov.p2photo.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.Photo;

/**
 * This class is responsible for handling the read/write operations in internal memory
 */
public class FileManager {

    private static final String TAG = "FileManager";
    private final Context mContext;
    private final Random random = new Random();

    public FileManager(Context mContext) {
        this.mContext = mContext;
    }


    public List<Album> getAlbumsList(String username){
        List<Album> userAlbums = new ArrayList<>();

        File filesDir = mContext.getFilesDir();
        for(File file: filesDir.listFiles())
            if(file.getName().startsWith(username)) {
                Log.i(TAG, "File: " + file.getName());
                userAlbums.add(new Album(file.getName().replaceFirst(username+"_", ""), file.getName()));
            }
        return userAlbums;
    }

    public String updateAlbum(String username, String albumName, String content){
        //TODO: check file already exists
        String filename = username + "_" + albumName;
        if(writeFile(filename, content))
            return filename;
        return null;
    }

    public boolean addPhotoToAlbum(String username, String albumName, Bitmap image){
        String imageFilename = "image" + random.nextInt();
        Log.i(TAG, "Saving image " + imageFilename);
        if(saveImage(imageFilename, image)) {
            Log.i(TAG, "Updating album " + albumName);
            return updateAlbum(username, albumName, imageFilename + "\n") != null;
        }
        return false;
    }

    public List<Photo> getAlbumPhotos(String filename){
        List<Photo> photos = new ArrayList<>();
        String fileContent = readFile(filename);
        if(fileContent != null){
            if(fileContent.trim().length() == 0)
                return new ArrayList<>();

            Log.i(TAG, "File content -> " + fileContent.trim().length());
            for(String photoFile: fileContent.split("\n")){
                Log.i(TAG, "Adding photo: " + photoFile );
                photos.add(new Photo(photoFile, loadImage(photoFile)));
            }
            return photos;
        }
        return null;
    }

    /**
     * Appends the content to the file
     * @return true if successful, false otherwise
     */
    private boolean writeFile(String filename, String content){
        try {
            FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_APPEND);
            fos.write(content.getBytes());
            fos.close();
            Log.i(TAG, "Write success " + filename);
            return true;
        } catch (IOException e) {
            Log.i(TAG, "Write failed " + filename);
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Reads file content
     * @return file content
     */
    public String readFile(String filename){
        try {
            FileInputStream fis = mContext.openFileInput(filename);
            StringBuilder sb = new StringBuilder();
            while (fis.available() > 0){
                sb.append((char) fis.read());
            }
            fis.close();
            return sb.toString();
        } catch (IOException e) {
            Log.i(TAG, "Read failed");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Stores image in internal memory
     * @param fileName name of the image file
     * @param bitmap bitmap of the image
     * @return true if successful, false otherwise
     */
    public boolean saveImage(String fileName, Bitmap bitmap){
        try {
            FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return true;

        } catch (IOException e) {
            Log.i(TAG, "Save image failed");
            e.printStackTrace();
            return false;
        }


    }

    /**
     * Loads image from internal storage
     * @return image's bitmap
     */
    public Bitmap loadImage(String fileName){
        try{
            FileInputStream fis = mContext.openFileInput(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();

            return bitmap;
        } catch (IOException e) {
            Log.i(TAG, "Load image failed");
            e.printStackTrace();
        }
        return null;
    }
}
