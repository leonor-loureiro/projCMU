package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleDriveHandler;
import pt.ulisboa.tecnico.cmov.p2photo.storage.FileManager;
import pt.ulisboa.tecnico.cmov.p2photo.storage.MemoryCacheManager;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectManager;

public class GlobalVariables extends Application {

    //members in group termite
    private ArrayList<Member> membersInGroup = new ArrayList<>();
    //Logged in user
    private Member user;
    //Mode (Cloud / P2P)
    public boolean google = true;
    //Session token
    private String token;
    //Google Account
    private GoogleSignInAccount account;
    //Google Drive Account
    private Drive driveService;
    //Handles google drive operations
    private GoogleDriveHandler googleDriveHandler;
    //Manages the app's cache memory
    private MemoryCacheManager cacheManager;
    //Manages the wifi direct operations
    private WifiDirectManager wifiDirectManager;
    //Manages the internal storage operations
    private FileManager fileManager;
    //FileIDs
    private Map<String,String> fileIDs = new HashMap<>();

    private List<String> operationsLog = new ArrayList<>();

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

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setCacheManager(MemoryCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public WifiDirectManager getWifiDirectManager() {
        return wifiDirectManager;
    }

    public void setWifiDirectManager(WifiDirectManager wifiDirectManager) {
        this.wifiDirectManager = wifiDirectManager;
    }

    public List<String> getOperationsLog() {
        return operationsLog;
    }

    public void setOperationsLog(List<String> operationsLog) {
        this.operationsLog = operationsLog;
    }

    public void addOperation(String operation){
        operationsLog.add(operation);
    }

    public ArrayList<Member> getMembersInGroup() {
        return membersInGroup;
    }

    public void setMembersInGroup(ArrayList<Member> membersInGroup) {
        this.membersInGroup = membersInGroup;
    }
    public void updateFileID(String album, String fileID){
        fileIDs.put(album, fileID);
    }

    public String getFileID(String album){
        return fileIDs.get(album);
    }
}
