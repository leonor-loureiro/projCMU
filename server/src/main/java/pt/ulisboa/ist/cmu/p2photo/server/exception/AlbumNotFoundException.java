package pt.ulisboa.ist.cmu.p2photo.server.exception;

public class AlbumNotFoundException extends Exception {
    private String albumName;
    public AlbumNotFoundException(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}
