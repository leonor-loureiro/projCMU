package pt.ulisboa.ist.cmu.p2photo.server.exception;

public class UserAlreadyHasAlbumException extends Exception {
    private String albumName;
    public UserAlreadyHasAlbumException(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}
