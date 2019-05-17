package pt.ulisboa.tecnico.cmov.p2photo.data;

import java.io.Serializable;

/*
class used to send a photo via socket, because the bitmap needs to be enconded
 */
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

