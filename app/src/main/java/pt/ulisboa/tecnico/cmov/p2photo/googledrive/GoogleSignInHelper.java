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

    public static final int RC_SIGN_IN = 100;
    private Activity activity;
    private GoogleSignInClient mGoogleSignInClient;



    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * This method checks if a user already signed in on google exists,
     * if not, performs the google sign and request permissions for the google drive
     */
    public void googleSignIn() {

        //Check if the user is already sign-in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if(account != null) {
            Log.i("Google Sign In", "User already signed in");
            createCredential(account);
            startMainActivity();
            return;
        }


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/drive"))
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

        //Perform google Sign In
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    /**
     * This method handles the response for the sign in operation
     */
    public void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            Log.i("Google Sign In", "sign in sucessfully performed");
            GoogleSignInAccount account = task.getResult(ApiException.class);
            createCredential(account);
            startMainActivity();


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Log.w("SignInResult", "signInResult:failed code=" + e.getStatusCode());
        }
    }


    /**
     * Creates a credential that will allows us to access google drive
     * @param account google account
     * @return google drive credential
     */
    public Drive createCredential(GoogleSignInAccount account) {

        Log.d("Google", "create credential");

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        activity, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("P2Photo")
                .build();
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
