package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
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
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import javax.crypto.SecretKey;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Constants;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.googledrive.GoogleSignInHelper;
import pt.ulisboa.tecnico.cmov.p2photo.security.SecurityManager;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
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

        this.globalVariables = (GlobalVariables)getApplicationContext();

        if(globalVariables.google)
            signInHelper = new GoogleSignInHelper(this);




    }

    /**
     * This function is responsible for performin the login operation
     * @param view
     */
    public void login(View view){


        final String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();


        if(!username.matches(Constants.USERNAME_REGEX) || !password.matches(Constants.PASSWORD_REGEX)){
            Utils.openWarningBox(this,  getString(R.string.invalidCredentials), null);

        }else {
            try {
                ServerAPI.getInstance().login(getApplicationContext(), username, password, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                        try {
                            globalVariables.setToken((String) response.get(0));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        globalVariables.setUser(new Member(username));


                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_sucess),
                                Toast.LENGTH_SHORT)
                                .show();

                        if(globalVariables.google)
                            signInHelper.googleSignIn();
                        else
                            startListAlbumsActivity();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,Throwable throwable, JSONArray response) {
                        Toast.makeText(LoginActivity.this,
                                    getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.login_failure),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);

                    }
                });
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }


        }
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

    private void startListAlbumsActivity() {
        //Start first activity
        Intent intent = new Intent(this, ListAlbumsActivity.class);
        //Clears the activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
    }



}
