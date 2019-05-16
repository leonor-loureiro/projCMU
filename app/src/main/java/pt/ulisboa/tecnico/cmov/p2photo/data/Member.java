package pt.ulisboa.tecnico.cmov.p2photo.data;

import java.io.Serializable;

public class Member implements Serializable{
    String name;

    String ip;

    public Member(String name){
        this.name = name;
    }

    public Member(String name,String ip){
        this.name = name;
        this.ip = ip;
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
