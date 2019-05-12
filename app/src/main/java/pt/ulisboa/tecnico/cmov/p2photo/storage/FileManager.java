package pt.ulisboa.tecnico.cmov.p2photo.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class is responsible for handling the read/write operations in internal memory
 */
public class FileManager {

    private final Context mContext;

    public FileManager(Context mContext) {
        this.mContext = mContext;
    }


    /**
     * Appends the content to the file
     * @return true if successful, false otherwise
     */
    public boolean writeFile(String filename, String content){
        try {
            FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_APPEND);
            fos.write(content.getBytes());
            fos.close();
            Log.i("FileManager", "Write success");
            return true;
        } catch (IOException e) {
            Log.i("FileManager", "Write failed");
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
            Log.i("FileManager", "Read failed");
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
            Log.i("FileManager", "Save image failed");
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
            Log.i("FileManager", "Load image failed");
            e.printStackTrace();
        }
        return null;
    }
}
