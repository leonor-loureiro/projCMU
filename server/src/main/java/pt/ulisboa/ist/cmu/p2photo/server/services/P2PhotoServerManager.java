package pt.ulisboa.ist.cmu.p2photo.server.services;

import pt.ulisboa.ist.cmu.p2photo.server.data.Album;
import pt.ulisboa.ist.cmu.p2photo.server.data.User;
import pt.ulisboa.ist.cmu.p2photo.server.exception.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class P2PhotoServerManager {

    // singleton
    private static P2PhotoServerManager instance = null;

    private List<User> users = new ArrayList<>();

    private SecurityHandler security;


    /**
     * Stop unintended instances
     */
   private P2PhotoServerManager(){
       security = new SecurityHandler();

       try {
           users = AtomicFileManager.getUserList();
       } catch (IOException | ClassNotFoundException e) {
           e.printStackTrace();
       }
   }


    /**
     * Gets the singleton
     * @return the singleton instance
     */
    public static P2PhotoServerManager getInstance(){
        if(instance == null){
            instance = new P2PhotoServerManager();
        }
        return instance;
    }


    /**
     * Register the username into the server
     * Safely hashes the password to prevent attacks
     *
     * @param username the name of the user
     * @param password the secret password
     * @return the login token
     */
    public String register(String username, String password) throws UserAlreadyExistsException {

        printInfo("Registering " + username);

        if(userExists(username))
            throw new UserAlreadyExistsException(username);

        users.add(new User(username, security.hashPassword(password)));

        updateInformation();

        return security.generateToken(username);
    }


    /**
     * Logs in user, verifies if his password matches
     *
     * @param username the name of the user
     * @param password the secret password
     * @return the token
     */
    public String login(String username, String password) throws UserNotExistsException, WrongPasswordException {

        User user = findUser(username);

        if(!security.passwordMatches(password, user.getPassword()))
            throw new WrongPasswordException(username);

        printInfo("Logging in user " + username);


        return security.generateToken(username);
    }


    /**
     * Verifies if the token received is valid
     * @param username the user who the token belongs to
     * @param token the user's token
     * @return true if the token was valid
     */
    public boolean verifyTokenValidity(String username, String token){
        boolean result = security.validateJTW(token, username);
        printInfo("Verifying token validity for " + username + " ... is valid? " + result);
        return result;
    }


    /**
     * finds the list of all members belonging to a certain album
     * @param username name of the user owning the album
     * @param albumName the name id of the album
     * @return the list of all members
     */
    public Map<String, String> getGroupMembership(String username, String albumName) throws UserNotExistsException, AlbumNotFoundException {
        printInfo("getting members of album " + albumName);
        return findAlbum(username, albumName).getGroupMembership();
    }


    /**
     * @return the list of all users of this service
     */
    public List<String> getUsers() {
        printInfo("Getting all users.");

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
        printInfo("getting album all albums of user " + username);

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

        printInfo("Updating album info of user " + username);
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
    public void shareAlbum(String username, String albumName, String username2) throws UserNotExistsException, AlbumNotFoundException, UserAlreadyHasAlbumException {
        //Check if user2 exists
        User user2 = findUser(username2);

        Album album = findAlbum(username, albumName);

        album.addMember(username2, null, null);

        user2.addAlbum(album);

        updateInformation();

        printInfo("Sharing album of " + username + " with " + username2);
    }


    /**
     * Creates and album for given user
     *
     * @param username Owner of the album
     * @param albumName album unique identifier
     * @throws UserNotExistsException if user doesn't exist
     */
    public void createAlbum(String username, String albumName, String url, String fileID) throws UserNotExistsException, UserAlreadyHasAlbumException {

        printInfo("Creating album for " + username);
        User user = findUser(username);
        user.addAlbum(new Album(albumName, username, url, fileID));
        updateInformation();
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

        printInfo("getting names of all album of user " + username);

        return list;
    }



    /**
     * Finds the fileId corresponding to the user-album
     * @param username the name of the user
     * @param albumName the name of the album
     * @return the fileID representing the user's album catalog file ID
     * @throws UserNotExistsException If the user does not exist
     * @throws AlbumNotFoundException If the Album name does not exist
     */
    public String getFileID(String username, String albumName) throws UserNotExistsException, AlbumNotFoundException {

        Album album = findAlbum(username, albumName);

        printInfo("Getting file ID for " + username);
        return album.findFileID(username);
    }




    ///////////////////////////////////////////////////////////////
    //                                                           //
    //                    Auxiliary Functions                    //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    /**
     * User for debug purposes
     * @param info information to be displayed
     */
    public void printInfo(String info){
        System.out.println(info);
    }

    /**
     * Persists data
     */
    private void updateInformation(){

        try {
            AtomicFileManager.atomicWriteObjectToFile(users);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        printInfo("Updating storage...");
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


}
