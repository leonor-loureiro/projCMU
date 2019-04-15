package pt.ulisboa.ist.cmu.p2photo.server.exception;

public class UserAlreadyExistsException extends Exception {
    String username;

    public UserAlreadyExistsException(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
