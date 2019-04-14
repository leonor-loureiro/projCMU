package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Constants;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleSignInHelper;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class LoginActivity extends AppCompatActivity {

    EditText usernameET;
    EditText passwordET;

    private GoogleSignInHelper signInHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);

        signInHelper = new GoogleSignInHelper(this);


    }

    /**
     * This function is responsible for performin the login operation
     * @param view
     */
    public void login(View view) throws IOException {
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();

        if(!username.matches(Constants.USERNAME_REGEX) || !password.matches(Constants.PASSWORD_REGEX)){
            Utils.openWarningBox(this,  getString(R.string.invalidCredentials), null);

        }else {
            //TODO: login operation
            ServerAPI.getInstance().login(this.getApplicationContext(),username,password);
            //Perform google sign in to get drive permissions
            //Launch app's first screen once it's successfully logged in
            //signInHelper.googleSignIn();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == GoogleSignInHelper.REQUEST_CODE_SIGN_IN &&
                resultCode == Activity.RESULT_OK) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            signInHelper.handleSignInResult(task);
        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    public void startRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }



}
