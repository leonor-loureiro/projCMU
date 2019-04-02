package pt.ulisboa.ist.cmu.p2photo.server.exception;

public class UserNotExistsException extends Exception {
    String username;

    public UserNotExistsException(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
