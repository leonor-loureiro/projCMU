package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;

import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;
import pt.ulisboa.tecnico.cmov.p2photo.storage.MemoryCacheManager;

public class GlobalVariables extends Application {

    //TODO alguem que tenha o login funcional mude isto
    private Member user;

    //TODO alguem que tenha o login funcional mude isto

    private String token;
    //Google Account
    private GoogleSignInAccount account;
    //Google Drive Account
    private Drive driveService;
    //Handles google drive operations
    private GoogleDriveHandler googleDriveHandler;
    //Manages the app's cache memory
    private MemoryCacheManager cacheManager;

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

    public Member getUser() {
        return user;
    }

    public void setUser(Member user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MemoryCacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(MemoryCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
