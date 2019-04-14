package pt.ulisboa.tecnico.cmov.p2photo.serverapi;

import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.p2photo.data.Album;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;

public class ServerAPI {

    // Emulator accessing pc's localhost
    private String url = "10.0.2.2/";

    private String port = "8080";

    private String username;

    private static String loginToken;

    // singleton
    private static ServerAPI instance = null;


    /**
     * Private constructor to prevent unwanted instantiation
     */
    private ServerAPI(){

    }


    /**
     * Gets the singleton
     * @return the singleton instance
     */
    public static ServerAPI getInstance(){
        if(instance == null)
            instance = new ServerAPI();
        return instance;
    }


    public boolean register(String username, String password){


        //TODO: set token with return from register


        return false;
    }

    public boolean login(String username, String password){

        //TODO: set token with return from login

        return false;
    }

    public Map<String, String> getGroupMembership() {



        return null;
    }

    /**
     * @return list of all members of the P2Pservice
     */
    public List<Member> getUsers() {

        //TODO: parse usernames into Members
        return null;
    }

    /**
     * Lists all the albums belonging to the user
     * @return list of all user's album
     */
    public List<Album> getUserAlbums() {

        return null;
    }

    public void shareAlbum() {


    }

    public boolean createAlbum() {

        return false;
    }

}
