package pt.ulisboa.tecnico.cmov.p2photo.data;

import java.io.Serializable;

public class Member implements Serializable{
    String name;
    String publicKey;

    public Member(String name){
        this.name = name;
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
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
