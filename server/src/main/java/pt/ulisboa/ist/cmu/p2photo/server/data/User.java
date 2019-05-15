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
    private List<Album> albumsP2Peer = new ArrayList<Album>();

    private List<Album> albumsGoogle = new ArrayList<>();

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


    public void addAlbumGoogle(Album album) throws UserAlreadyHasAlbumException {
        for(Album alb : this.getAlbumsGoogle())
            if (album.getName().equals(alb.getName()))
                throw new UserAlreadyHasAlbumException(alb.getName());

        albumsGoogle.add(album);
    }


    public void addAlbumP2P(Album album) throws UserAlreadyHasAlbumException {
        for(Album alb : this.getAlbumsP2P())
            if (album.getName().equals(alb.getName()))
                throw new UserAlreadyHasAlbumException(alb.getName());

        albumsP2Peer.add(album);
    }

    public List<Album> getAlbumsGoogle() {
        return albumsGoogle;
    }

    public List<Album> getAlbumsP2P() {
        return albumsP2Peer;
    }


}
