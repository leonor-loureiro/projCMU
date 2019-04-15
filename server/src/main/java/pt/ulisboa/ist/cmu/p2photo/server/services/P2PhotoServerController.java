package pt.ulisboa.ist.cmu.p2photo.server.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.ist.cmu.p2photo.server.exception.AlbumNotFoundException;
import pt.ulisboa.ist.cmu.p2photo.server.exception.UserNotExistsException;

import java.util.Map;

@RestController
@RequestMapping("/server")
public class P2PhotoServerController {

    @RequestMapping(value = "/test")
    public ResponseEntity<String> test() {

        return new ResponseEntity<>("Hello World!", HttpStatus.OK);
    }

    /**
     * Register the user
     *
     * @param credentials must contain username and password
     * @return a login token in case of success
     */
    @RequestMapping(value = "/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> credentials) {

        // Extract params
        String username = credentials.get("username");
        String password = credentials.get("password");

        // generate login token
        String token = P2PhotoServerManager.getInstance().register(username, password);

        return new ResponseEntity<>(token, HttpStatus.OK);
    }


    /**
     * verified if user exists and password matches
     *
     * @param credentials username and password
     * @return login token in case of success
     */
    @RequestMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {

        //Extract params
        String username = credentials.get("username");
        String password = credentials.get("password");

        System.out.println(credentials.get("username"));
        System.out.println(credentials.get("password"));

        // generate login token
        String token = P2PhotoServerManager.getInstance().login(username, password);

        return new ResponseEntity<>(token, HttpStatus.OK);
    }


    /**
     * finds all the members and their urls
     *
     * @param credentials must contain token, username, albumName
     * @return
     */
    @RequestMapping(value = "/getGroupMembership")
    public ResponseEntity<Map<String, String>> getGroupMembership(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");
        String albumName = credentials.get("albumName");

        //TODO: Verify token

        try {
            return new ResponseEntity<>(P2PhotoServerManager.getInstance().getGroupMembership(username, albumName), HttpStatus.OK);

        } catch (UserNotExistsException | AlbumNotFoundException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }


    /**
     * lists all the users of the service
     *
     * @param credentials must contain token and username
     * @return list with all users
     */
    @RequestMapping(value = "/getUsers")
    public ResponseEntity<String[]> getUsers(@RequestBody Map<String, String> credentials) {
        String token = credentials.get("token");
        String username = credentials.get("username");

        //TODO: Verify token
        String[] users = P2PhotoServerManager.getInstance().getUsers().toArray(new String[0]);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }


    /**
     * finds the name of all the user's albums
     *
     * @param credentials must contain token and username
     * @return a list will all the album's names that belong to the user
     */
    @RequestMapping(value = "/getUserAlbums")
    public ResponseEntity<String[]> getUserAlbums(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");

        //TODO: Verify token


        // get albums
        try {
            String[] albumNames = P2PhotoServerManager.getInstance().getUserAlbumsNames(username).toArray(new String[0]);
            return new ResponseEntity<>(albumNames, HttpStatus.OK);

        } catch (UserNotExistsException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }


    /**
     * shares a album with a given user, for the operation to be complete, the other user must update it's
     * part of the album
     *
     * @param credentials must contain token, username1, albumName
     * @return Success if share album is successful
     */
    @RequestMapping(value = "/shareAlbum")
    public ResponseEntity<String> shareAlbum(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username1 = credentials.get("username1");
        String username2 = credentials.get("username2");
        String albumName = credentials.get("albumName");

        //TODO: Verify token


        // Share Album
        try {
            P2PhotoServerManager.getInstance().shareAlbum(username1, albumName, username2);
            return new ResponseEntity<>("Success", HttpStatus.OK);

        } catch (UserNotExistsException | AlbumNotFoundException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }


    /**
     * Creates an album with the given link and file id for the given user
     *
     * @param credentials token, username, albumName, url, fileID
     * @return success if the operatiojn is successful
     */
    @RequestMapping(value = "/createAlbum")
    public ResponseEntity<String> createAlbum(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");
        String albumName = credentials.get("albumName");
        String url = credentials.get("url");
        String fileID = credentials.get("fileID");

        //TODO: Verify token

        // Create Album
        try {
            P2PhotoServerManager.getInstance().createAlbum(username, albumName, url, fileID);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (UserNotExistsException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }


    /**
     * Updates the information of a album.
     * Should be done to confirm the album share
     * Can also be used to change the url and file ID of an album belonging to the user
     *
     * @param credentials token, username, albumName, url, fileID
     * @return success if it was successful
     */
    @RequestMapping(value = "/updateAlbum")
    public ResponseEntity<String> updateAlbum(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");
        String albumName = credentials.get("albumName");
        String url = credentials.get("url");
        String fileID = credentials.get("fileID");

        //TODO: Verify token

        // Update Album
        try {
            P2PhotoServerManager.getInstance().updateAlbum(username, albumName, url, fileID);
            return new ResponseEntity<>("Success", HttpStatus.OK);

        } catch (UserNotExistsException | AlbumNotFoundException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

}
