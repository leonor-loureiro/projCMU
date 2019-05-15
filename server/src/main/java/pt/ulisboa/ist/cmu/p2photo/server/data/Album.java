package pt.ulisboa.ist.cmu.p2photo.server.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a P2Photo album
 */
public class Album implements Serializable {

    // Album name
    private String name;
    // Group membership metadata: url for the catalog file of each member
    private Map<String, String> groupMembership = new HashMap<>();
    // The file ID for each user
    private Map<String, String> fileIDs = new HashMap<>();
    //Secret keys encrypted for each user
    private Map<String, String> secretKeys = new HashMap<>();

    public Map<String, String> getFileIDs() {
        return fileIDs;
    }

    public Album(String name, String username, String catalogURL, String fileID, String secretKey) {
        this.name = name;
        addMember(username, catalogURL, fileID);
        this.secretKeys.put(username, secretKey);
    }

    public Album(String name, String username, String catalogURL, String fileID) {
        this.name = name;
        addMember(username, catalogURL, fileID);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getGroupMembership() {
        return groupMembership;
    }

    public void addMember(String username, String catalogURL, String fileID){
        this.groupMembership.put(username, catalogURL);
        this.fileIDs.put(username, fileID);
    }


    public void updateForUser(String username, String url, String fileID){
        this.groupMembership.replace(username, url);
        this.fileIDs.replace(username, fileID);
    }

    public String findFileID(String username) {
        return fileIDs.get(username);
    }

    public String findSecretKey(String username) {
        return secretKeys.get(username);
    }


}
