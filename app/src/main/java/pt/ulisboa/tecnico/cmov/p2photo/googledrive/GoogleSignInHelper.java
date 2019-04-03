package pt.ulisboa.tecnico.cmov.p2photo.googledrive;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

import pt.ulisboa.tecnico.cmov.p2photo.activities.ListAlbums;

/**
 * This class is responsible for handling the google login
 */
public class GoogleSignInHelper {

    public static final int REQUEST_CODE_SIGN_IN = 100;
    private Activity activity;



    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * This method checks if a user already signed in on google exists,
     * if not, performs the google sign and request permissions for the google drive
     */
    public void googleSignIn() {
        //Check if exists an already signed-in user
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if(account != null){
            createCredential(account);
            return;
        }
        //Configure sign-in to request permission to the google drive
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient googleClient = GoogleSignIn.getClient(activity, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        activity.startActivityForResult(googleClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);

    }


    /**
     * This method handles the response for the sign in operation
     */
    public void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            Log.i("Google Sign In", "sign in sucessfully performed");
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.i("Google", account.getEmail());
            createCredential(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Log.w("SignInResult", "signInResult:failed code=" + e.getStatusCode());
        }
    }


    /**
     * Uses the signed-in account to sign in on Google Drive
     * @param account google account
     * @return google drive credential
     */
    private void createCredential(GoogleSignInAccount account) {

        Log.d("Google", "create credential");

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        activity, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        Drive driveService =  new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("P2Photo")
                .build();

        GoogleDriveHelper helper = new GoogleDriveHelper(driveService);
        //Todo: save GoogleDriveHelper in global state


        /*helper.createAlbumSlice("Test").addOnCompleteListener(
                new OnCompleteListener<Pair<String, String>>() {
                    @Override
                    public void onComplete(@NonNull Task<Pair<String, String>> task) {
                        final Pair<String, String> fileInfo = task.getResult();
                        List<String> contents = new ArrayList<String>();
                        contents.add("AAAA");
                        contents.add("BBBB");
                        contents.add("CCCC");
                        helper.updateFile(fileInfo.first, contents ).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("Drive", "Update on success listener");
                                        try {
                                            helper.downloadFile(fileInfo.second);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        );
                    }
                }
        );*/
        startMainActivity();
    }

    /**
     * Starts the app's main activity
     * @see ListAlbums
     */
    private void startMainActivity() {
        //Start first activity
        Intent intent = new Intent(activity, ListAlbums.class);
        //Clears the activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }



}
