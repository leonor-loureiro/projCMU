package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.graphics.Bitmap;

import java.io.Serializable;

public class PhotoToSend implements Serializable {
    String url;
    byte[] bitmap;

    public PhotoToSend(String url, byte[] bitmap) {
        this.url = url;
        this.bitmap = bitmap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }

}

