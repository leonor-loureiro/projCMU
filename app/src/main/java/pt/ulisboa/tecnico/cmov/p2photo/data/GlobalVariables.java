package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;

import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;

public class GlobalVariables extends Application {

    //Google Account
    private GoogleSignInAccount account;
    //Google Drive Account
    private Drive driveService;
    //Handles google drive operations
    private GoogleDriveHandler googleDriveHandler;

    public GoogleSignInAccount getAccount() {
        return account;
    }

    public void setAccount(GoogleSignInAccount account) {
        this.account = account;
    }

    public Drive getDriveService() {
        return driveService;
    }

    public void setDriveService(Drive driveService) {
        this.driveService = driveService;
    }

    public GoogleDriveHandler getGoogleDriveHandler() {
        return googleDriveHandler;
    }

    public void setGoogleDriveHandler(GoogleDriveHandler googleDriveHandler) {
        this.googleDriveHandler = googleDriveHandler;
    }
}
