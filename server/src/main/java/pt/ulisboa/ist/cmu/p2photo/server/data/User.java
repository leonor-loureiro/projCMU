package pt.ulisboa.ist.cmu.p2photo.server.data;

import pt.ulisboa.ist.cmu.p2photo.server.exception.UserAlreadyHasAlbumException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a P2Photo user
 */
public class User implements Serializable {

    // Username
    private String username;
    // Hashed password
    private String password;
    // User's albums
    private List<Album> albums = new ArrayList<Album>();

    public User(String username) {
        this.username = username;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void addAlbum(Album album) throws UserAlreadyHasAlbumException {
        for(Album alb : this.getAlbums())
            if (album.getName().equals(alb.getName()))
                throw new UserAlreadyHasAlbumException(alb.getName());

        albums.add(album);
    }

}
