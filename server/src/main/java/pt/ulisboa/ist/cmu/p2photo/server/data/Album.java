package pt.ulisboa.ist.cmu.p2photo.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a P2Photo album
 */
public class Album {

    //Album name
    private String name;
    //Group membership metadata: url for the catalog file of each member
    private Map<String, String> groupMembership = new HashMap<>();


    public Album(String name, String username, String catalogURL) {
        this.name = name;
        addMember(username, catalogURL);
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

    public void addMember(String username, String catalogURL){
        this.groupMembership.put(username, catalogURL);
    }

    public List<String> getMembers(){
        return new ArrayList<>(groupMembership.keySet());
    }

}
