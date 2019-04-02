package pt.ulisboa.ist.cmu.p2photo.server.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/server")
public class P2PhotoServerController {

    @RequestMapping(value = "/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> credentials) {
        return null;
    }

    @RequestMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        return null;
    }

    @RequestMapping(value = "/getGroupMembership")
    public ResponseEntity<String> getGroupMembership(@RequestBody Map<String, String> credentials) {
        return null;
    }

    @RequestMapping(value = "/getUsers")
    public ResponseEntity<String> getUsers(@RequestBody Map<String, String> credentials) {
        return null;
    }

    @RequestMapping(value = "/getUserAlbums")
    public ResponseEntity<String> getUserAlbums(@RequestBody Map<String, String> credentials) {
        return null;
    }

    @RequestMapping(value = "/shareAlbum")
    public ResponseEntity<String> shareAlbum(@RequestBody Map<String, String> credentials) {
        return null;
    }

    @RequestMapping(value = "/createAlbum")
    public ResponseEntity<String> createAlbum(@RequestBody Map<String, String> credentials) {
        return null;
    }



}
