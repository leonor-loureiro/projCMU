package pt.ulisboa.tecnico.cmov.p2photo.data;

import java.io.Serializable;

public class Member implements Serializable{
    String deviceName;
    String name;
    String publicKey;

    String ip;

    public Member(String name){
        this.name = name;
    }

    public Member(String name,String ip,String deviceName){
        this.name = name;
        this.ip = ip;
        this.deviceName = deviceName;
    }


    public Member(String name, String publicKey) {
        this.name = name;
        this.publicKey = publicKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Member){
            return  ((Member)obj).getName().equals(this.name);
        }
        return false;

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
}
}
