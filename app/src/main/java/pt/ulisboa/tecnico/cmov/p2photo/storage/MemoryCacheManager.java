package pt.ulisboa.tecnico.cmov.p2photo.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Contacts;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Photo;

/**
 * This class is responsible for reading/writing into cache
 */
public class MemoryCacheManager {

    private File cacheDir;
    private final Context mContext;
    private static final String TAG = "MemoryCacheManager";


    public MemoryCacheManager(Context mContext) {
        this.mContext = mContext;
        cacheDir = mContext.getCacheDir();
    }


    /**
     * Returns cache maximum size in MB
     */
    public long getLimit(){
        int heapSize = (int) Math.floor(Runtime.getRuntime().maxMemory()/1024./1024.);
        SharedPreferences sharedPref =
                mContext.getSharedPreferences(mContext.getString(R.string.cache_settings_pref), Context.MODE_PRIVATE);
        //Default = 25% of available heap size
        return sharedPref.getInt(mContext.getString(R.string.cache_size_pref), heapSize/4);
    }


    /**
     * Returns the list of albums photos added by the given user, stored in cache
     * @param username user that added the photos
     * @param albumName album name
     * @return album photos from that user stored in cache
     */
    public List<Photo> getAlbumPhotos(String username, String albumName){
        List<Photo> cachedPhotos = new ArrayList<>();
        String prefix = getPrefix(username, albumName);
        Log.i(TAG, "Get cached album photos: " + prefix);
        for(File file: cacheDir.listFiles()) {
            Log.i(TAG, "Cached file: " + file.getName());
            if (file.getName().startsWith(prefix)) {
                cachedPhotos.add(
                        new Photo(file.getName().replaceFirst(prefix, ""),
                                loadImageFromCache(file.getName()),
                                false
                        )
                );
            }
        }
        return cachedPhotos;
    }

    private String getPrefix(String username, String albumName) {
        Log.i(TAG, "Prefix: " + albumName + "_" + username);
        return albumName + "_" + username + "_";
    }

    public void addAlbumPhoto(String username, String albumName, Photo photo){
        String filename = getPrefix(username, albumName) + photo.getUrl();
        File file = new File(cacheDir.getAbsolutePath() + "/" + filename);
        if(file.exists()){
            Log.i(TAG, "File already in cache: " + filename);
            return;
        }
        Log.i(TAG, "Add album photo: " + filename);
        saveImageToCache(filename, photo.getBitmap());
        manageCacheSize();
    }
    /**
     * Save image in cache
     */
    public void saveImageToCache(String fileName, Bitmap bitmap){
        File imageFile = new File(cacheDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads image from cache
     * @return image's bitmap
     */
    public Bitmap loadImageFromCache(String filename){
        File imageFile = new File(cacheDir, filename);
        if(!imageFile.exists()){
            Log.i(TAG, "Image " + filename + " not in cache");
            return null;
        }

        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Returns the current cache size
     */
    public long getCacheSize(){
        return getDirectorySize(cacheDir);
    }

    /**
     * Returns the size of a directory
     */
    private long getDirectorySize(File directory) {
        long size = 0;
        for (File file : directory.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirectorySize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        Log.i(TAG, "Used space = " + size/1024./1024.+"MB" + " / " + getLimit()+"MB");
        return size;
    }

    /**
     * Checks used cache space. If reached limit, delete oldest files first,
     * until cache is at 1/3 capacity.
     */
    public void manageCacheSize(){
        long size = getCacheSize();
        long limit = getLimit() * 1024 * 1024;

        Log.i(TAG, "Manage cache size: " + size + "/" + limit);
        if(size <= limit)
            return;

        //Get cache files
        File[] files = cacheDir.listFiles();

        //Sort files from oldest -> newest
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });

        int i = 0;
        while(size > limit/3){
            File file = files[i];
            Log.i(TAG, "Deleting file " + file.getName());
            long fileSize = file.length();
            if(file.delete())
                size -= fileSize;
            i++;

        }
        Log.i(TAG, "Cache cleaned " + size/1024./1024.+"MB / " + limit + "MB");
        Toast.makeText(mContext, "Cache cleaned.", Toast.LENGTH_SHORT).show();

    }




}
