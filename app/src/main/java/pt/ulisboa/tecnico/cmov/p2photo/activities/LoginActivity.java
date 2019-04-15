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
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Constants;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleSignInHelper;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class LoginActivity extends AppCompatActivity {

    EditText usernameET;
    EditText passwordET;

    private GoogleSignInHelper signInHelper;

    private GlobalVariables globalVariables;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);

        signInHelper = new GoogleSignInHelper(this);

        this.globalVariables = (GlobalVariables)getApplicationContext();


    }

    /**
     * This function is responsible for performin the login operation
     * @param view
     */
    public void login(View view){


        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String result = "";


        if(!username.matches(Constants.USERNAME_REGEX) || !password.matches(Constants.PASSWORD_REGEX)){
            Utils.openWarningBox(this,  getString(R.string.invalidCredentials), null);

        }else {
            //TODO: login operation
            try {
                result = ServerAPI.getInstance().login(getApplicationContext(), username, password);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(result.equals("")) {
                // retry login multithreaded
            }

            //Perform google sign in to get drive permissions
            //Launch app's first screen once it's successfully logged in
            signInHelper.googleSignIn();
            globalVariables.setToken(result);
            globalVariables.setUser(new Member(username));


        }
        Log.i("result",result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.i("Google", "onActivityResult" + CommonStatusCodes.getStatusCodeString(resultCode));
        if(requestCode == GoogleSignInHelper.REQUEST_CODE_SIGN_IN) {

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
