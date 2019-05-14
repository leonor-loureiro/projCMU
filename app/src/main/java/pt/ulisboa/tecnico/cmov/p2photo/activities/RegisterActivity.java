package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import javax.crypto.SecretKey;

import cz.msebera.android.httpclient.Header;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Constants;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleSignInHelper;
import pt.ulisboa.tecnico.cmov.p2photo.security.SecurityManager;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;


public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "RegisterActivity";
    EditText usernameET;
    EditText passwordET;
    EditText confirmPasswordET;
    private GoogleSignInHelper signInHelper;


    private GlobalVariables globalVariables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);
        confirmPasswordET = findViewById(R.id.password2);

        this.globalVariables = (GlobalVariables)getApplicationContext();

        if(globalVariables.google)
            signInHelper = new GoogleSignInHelper(this);




    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void register(View view) {
        final String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String confirmPassword = confirmPasswordET.getText().toString();

        //Check if credentials are valid formats
        if(!username.matches(Constants.USERNAME_REGEX) ||
                !password.matches(Constants.PASSWORD_REGEX) || !confirmPassword.matches(Constants.PASSWORD_REGEX)) {
            Utils.openWarningBox(this, getString(R.string.invalidCredentials), null);
            return;
        }

        //Check if password confirmation is valid
        if(!password.equals(confirmPassword)){
            Utils.openWarningBox(this, getString(R.string.invalidCredentials), null);
            return;
        }

        try {
            ServerAPI.getInstance().register(this.getApplicationContext(), username, password,new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    try {
                        globalVariables.setToken((String) response.get(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    globalVariables.setUser(new Member(username));

                    Toast.makeText(RegisterActivity.this,
                                "Register successfuly",
                                Toast.LENGTH_SHORT)
                                .show();
                    if(globalVariables.google)
                        signInHelper.googleSignIn();
                    else{
                        Intent intent = new Intent(RegisterActivity.this, ListAlbumsActivity.class);
                        //Clears the activity stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }


                }

                @Override
                public void onFailure(int statusCode, Header[] headers,Throwable throwable,JSONArray response) {
                    if(statusCode == 400){
                        Toast.makeText(RegisterActivity.this,
                                "User with that name already exists.",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Perform google sign in to get drive permissions
        //Launch app's first screen once it's successfully logged in

    }


    public void startLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GoogleSignInHelper.REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            signInHelper.handleSignInResult(task);
        }
    }


}
