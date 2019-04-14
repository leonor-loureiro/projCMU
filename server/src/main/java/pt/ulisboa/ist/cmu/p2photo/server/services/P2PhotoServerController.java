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

    @RequestMapping(value = "/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> credentials) {

        // Extract params
        String username = credentials.get("username");
        String password = credentials.get("password");

        // generate login token
        String token = P2PhotoServerInterface.getInstance().register(username, password);

        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @RequestMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {

        //Extract params
        String username = credentials.get("username");
        String password = credentials.get("password");

        // generate login token
        String token = P2PhotoServerInterface.getInstance().login(username, password);

        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @RequestMapping(value = "/getallusers")
    public ResponseEntity<String[]> getAllUsers(@RequestBody Map<String, String> credentials) {

        //Extract params
        String username = credentials.get("username");
        String token = credentials.get("token");

        //TODO: Verify Token


        String[] users = P2PhotoServerInterface.getInstance().getUsers().toArray(new String[0]);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @RequestMapping(value = "/getGroupMembership")
    public ResponseEntity<String> getGroupMembership(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");

        //TODO: Verify token

        return null;
    }

    @RequestMapping(value = "/getUsers")
    public ResponseEntity<String> getUsers(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");

        //TODO: Verify token

        return null;
    }

    @RequestMapping(value = "/getUserAlbums")
    public ResponseEntity<String> getUserAlbums(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");

        //TODO: Verify token


        // Get albums
        try {
            P2PhotoServerInterface.getInstance().getUserAlbums(username);
        } catch (UserNotExistsException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping(value = "/shareAlbum")
    public ResponseEntity<String> shareAlbum(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username1 = credentials.get("username1");
        String username2 = credentials.get("username2");
        String albumName = credentials.get("albumName");
        String url = credentials.get("url");
        //TODO: Verify token


        // Share Album
        try {
            P2PhotoServerInterface.getInstance().shareAlbum(username1, albumName, username2, url);
        } catch (UserNotExistsException e) {
            e.printStackTrace();
        } catch (AlbumNotFoundException e) {
            e.printStackTrace();
        }


        return null;
    }

    @RequestMapping(value = "/createAlbum")
    public ResponseEntity<String> createAlbum(@RequestBody Map<String, String> credentials) {

        String token = credentials.get("token");
        String username = credentials.get("username");
        String albumName = credentials.get("albumName");
        String url = credentials.get("url");

        //TODO: Verify token

        // Create Album
        try {
            P2PhotoServerInterface.getInstance().createAlbum(username, albumName, url);
        } catch (UserNotExistsException e) {
            e.printStackTrace();
        }


        return null;
    }


}
