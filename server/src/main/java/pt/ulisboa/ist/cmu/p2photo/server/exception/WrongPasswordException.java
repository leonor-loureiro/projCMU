package pt.ulisboa.ist.cmu.p2photo.server.exception;

public class WrongPasswordException extends Exception {
    String msg;

    public WrongPasswordException(String msg) {
        this.msg = msg;
    }

    public String getmsg() {
        return msg;
    }

    public void setUsername(String msg) {
        this.msg = msg;
    }
}
