package pt.ulisboa.tecnico.cmov.p2photo.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

public class Album implements Serializable{
    String name;
    List<String> groupMembership = new ArrayList<>();
    List<Member> members = new ArrayList<>();
    String fileID;
    SecretKey secretKey;

    public Album(String name, String fileID) {
        this.name = name;
        this.fileID = fileID;
    }

    public Album(String name) {
        this.name = name;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGroupMembership() {
        return groupMembership;
    }

    public void setGroupMembership(List<String> groupMembership) {
        this.groupMembership = groupMembership;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public void addMember(Member member){
        members.add(member);
    }

}
