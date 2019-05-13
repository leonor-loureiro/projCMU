package pt.ulisboa.tecnico.cmov.p2photo.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import pt.ulisboa.tecnico.cmov.p2photo.R;

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

    }




}
