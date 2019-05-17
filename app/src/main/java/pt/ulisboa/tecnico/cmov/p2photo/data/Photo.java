package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Photo implements Serializable {
    String url;
    Bitmap bitmap;
    Boolean mine = true;

    public Photo(String url, Bitmap bitmap) {
        this.url = url;
        this.bitmap = bitmap;
    }

    public Photo(String url, Bitmap bitmap, boolean mine) {
        this.url = url;
        this.bitmap = bitmap;
        this.mine = mine;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Boolean getMine() {
        return mine;
    }

    public void setMine(Boolean mine) {
        this.mine = mine;
    }

    public boolean isMine() {
        return getMine();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Photo)
            return ((Photo) obj).getUrl().equals(url);
        return false;
    }
}
