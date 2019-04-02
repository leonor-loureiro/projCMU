package pt.ulisboa.ist.cmu.p2photo.server.services;

import pt.ulisboa.ist.cmu.p2photo.server.data.Album;
import pt.ulisboa.ist.cmu.p2photo.server.data.User;
import pt.ulisboa.ist.cmu.p2photo.server.exception.AlbumNotFoundException;
import pt.ulisboa.ist.cmu.p2photo.server.exception.UserNotExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class P2PhotoServerInterface {

    private List<User> users = new ArrayList<>();


    public boolean register(String username, String password) {
        return true;
    }


    public boolean login(String username, String password) {
        return true;
    }


    public Map<String, String> getGroupMembership(String albumName) {
        return null;
    }

    public List<String> getUsers() {
        List<String> userIds = new ArrayList<>();
        for (User user: users)
            userIds.add(user.getUsername());
        return userIds;
    }

    public List<Album> getUserAlbums(String username) throws UserNotExistsException {
        User user = findUser(username);
        return user.getAlbums();
    }

    public void shareAlbum(String username, String albumName, String username2) throws UserNotExistsException, AlbumNotFoundException {
        User user = findUser(username);
        //Check if user2 exists
        findUser(username2);
        Album album = findAlbum(user, albumName);
        album.addMember(username2, null);

    }

    public void createAlbum(String username, String albumName) throws UserNotExistsException {
        User user = findUser(username);
        user.addAlbum(new Album(albumName, username, null));
    }

    /**
     * This function returns the user with the given username
     */
    private User findUser(String username) throws UserNotExistsException {
        for(User user : users)
            if(user.getUsername().equals(username))
                return user;
        throw new UserNotExistsException(username);
    }

    /**
     * This method returns a user's album with the given album name
     */
    private Album findAlbum(User user, String albumName) throws AlbumNotFoundException {
        for(Album album : user.getAlbums()){
            if(album.getName().equals(albumName))
                return album;
        }
        throw new AlbumNotFoundException(albumName);
    }
}
