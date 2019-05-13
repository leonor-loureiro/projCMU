package pt.ulisboa.ist.cmu.p2photo.server.data;

import java.io.Serializable;
import java.lang.reflect.Field;

public class Operation implements Serializable {
    long timestamp;
    String type;
    String user;
    String album;
    String user2;

    public Operation(String type, String user) {
        timestamp = System.currentTimeMillis();
        this.type = type;
        this.user = user;
    }

    public Operation(String type, String user, String album) {
        timestamp = System.currentTimeMillis();
        this.type = type;
        this.user = user;
        this.album = album;
    }

    public Operation(String type, String user, String album, String user2) {
        timestamp = System.currentTimeMillis();
        this.type = type;
        this.user = user;
        this.album = album;
        this.user2 = user2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toString(){
        StringBuilder str = new StringBuilder();

        try {
            //Iterate to all fields of the message
            Field[] fields = Operation.class.getDeclaredFields();
            for (Field field : fields) {
                Object obj = field.get(this);
                if (obj != null) {
                    str.append(field.getName()).append(" = ").append(obj).append(";");
                }
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return str.toString();
    }
}
