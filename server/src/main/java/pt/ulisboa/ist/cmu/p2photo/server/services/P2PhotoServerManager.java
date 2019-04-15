package pt.ulisboa.ist.cmu.p2photo.server.services;

import pt.ulisboa.ist.cmu.p2photo.server.data.Album;
import pt.ulisboa.ist.cmu.p2photo.server.data.User;
import pt.ulisboa.ist.cmu.p2photo.server.exception.AlbumNotFoundException;
import pt.ulisboa.ist.cmu.p2photo.server.exception.UserNotExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class P2PhotoServerManager {

    // singleton
    private static P2PhotoServerManager instance = null;

    private List<User> users = new ArrayList<>();


    /**
     * Stop unintended instances
     */
   private P2PhotoServerManager(){

   }


    /**
     * If user exists
     * @param username unique identifier of the user
     * @return true if the user exists
     */
   private boolean userExists(String username){
       try {
           findUser(username);
           return true;
       } catch (UserNotExistsException e) {
           return false;
       }
   }


    /**
     * Gets the singleton
     * @return the singleton instance
     */
    public static P2PhotoServerManager getInstance(){
        if(instance == null){
            instance = new P2PhotoServerManager();
            //TODO: load serialized list if it exists;
        }
        return instance;
    }


    /**
     * Register the username into the server
     *
     * @param username the name of the user
     * @param password the secret password
     * @return the login token
     */
    public String register(String username, String password) {

        users.add(new User(username, password));

        //TODO: properly generate token

        updateInformation();

        return "TOKEEEEEN";
    }


    /**
     * Logs in user, verifies if his password matches
     *
     * @param username the name of the user
     * @param password the secret password
     * @return the token
     */
    public String login(String username, String password) {
        //TODO: properly generate token
        return "TOKENNN";
    }


    /**
     * finds the list of all members belonging to a certain album
     * @param username name of the user owning the album
     * @param albumName the name id of the album
     * @return the list of all members
     */
    public Map<String, String> getGroupMembership(String username, String albumName) throws UserNotExistsException, AlbumNotFoundException {

        return findAlbum(username, albumName).getGroupMembership();
    }


    /**
     * @return the list of all users of this service
     */
    public List<String> getUsers() {
        List<String> userIds = new ArrayList<>();
        for (User user: users)
            userIds.add(user.getUsername());
        return userIds;
    }


    /**
     * Finds all the albums belonging to given user
     * @param username name of the user owning the albums
     * @return the list of albums that belong to the user
     * @throws UserNotExistsException if the user does not exist
     */
    public List<Album> getUserAlbums(String username) throws UserNotExistsException {
        User user = findUser(username);
        return user.getAlbums();
    }


    /**
     * Edits the state of a given album of the user
     *
     * @param username username whose album is gonna be edited
     * @param albumName the name of the album to be edited
     * @param url the new url that's gonna be set to
     * @param fileID the fileId of the catalog
     * @throws UserNotExistsException If the user doesn't exist
     * @throws AlbumNotFoundException If the album name does not exist
     */
    public void updateAlbum(String username, String albumName, String url, String fileID) throws UserNotExistsException, AlbumNotFoundException {

        Album album = findAlbum(username, albumName);
        album.updateForUser(username, url, fileID);
        updateInformation();
    }


    /**
     * Shares an album with a user, however to be fully complete,
     * the new user must update his info on the album
     * with the method {@link #updateAlbum(String, String, String, String)}
     *
     * @param username user who owns the album
     * @param albumName name of the album that's being shared
     * @param username2 user to be added to album
     * @throws UserNotExistsException if user does not exist
     * @throws AlbumNotFoundException if album does not exist
     */
    public void shareAlbum(String username, String albumName, String username2) throws UserNotExistsException, AlbumNotFoundException {
        //Check if user2 exists
        User user2 = findUser(username2);

        Album album = findAlbum(username, albumName);

        album.addMember(username2, null, null);

        user2.addAlbum(album);

        updateInformation();
    }


    /**
     * Creates and album for given user
     *
     * @param username Owner of the album
     * @param albumName album unique identifier
     * @throws UserNotExistsException if user doesn't exist
     */
    public void createAlbum(String username, String albumName, String url, String fileID) throws UserNotExistsException {

        User user = findUser(username);
        user.addAlbum(new Album(albumName, username, url, fileID));
        updateInformation();
    }


    /**
     * This function returns the user with the given username
     * @param username username of the user to be found
     * @return the user matching the username
     */
    private User findUser(String username) throws UserNotExistsException {
        for(User user : users)
            if(user.getUsername().equals(username))
                return user;
        throw new UserNotExistsException(username);
    }


    /**
     * This method returns a user's album with the given album name
     *
     * @param username the name of the user owning the album
     * @param albumName the name of the album
     * @return the album
     * @throws AlbumNotFoundException if the album wasn't found
     */
    private Album findAlbum(String username, String albumName) throws AlbumNotFoundException, UserNotExistsException {
        User user = findUser(username);

        for(Album album : user.getAlbums()){
            if(album.getName().equals(albumName))
                return album;
        }
        throw new AlbumNotFoundException(albumName);
    }


    /**
     * @param username the name of the use whose albums will be found
     * @return the list of album names
     */
    public List<String> getUserAlbumsNames(String username) throws UserNotExistsException {
        ArrayList<String> list = new ArrayList<>();
        List <Album> albums = getUserAlbums(username);

        for (Album album : albums) {
            list.add(album.getName());
        }

        return list;
    }


    /**
     * Persists data
     */
    public void updateInformation(){

    }
}
