package pt.ulisboa.tecnico.cmov.p2photo.googledrive.exception;

public class GoogleDriveException extends Exception {
    String message;
    public GoogleDriveException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
