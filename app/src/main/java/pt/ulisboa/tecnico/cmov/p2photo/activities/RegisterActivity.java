package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Constants;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;


public class RegisterActivity extends AppCompatActivity {

    EditText usernameET;
    EditText passwordET;
    EditText confirmPasswordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);
        confirmPasswordET = findViewById(R.id.password2);

    }

    public void register(View view) {
        String username = usernameET.getText().toString();
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

        Intent intent = new Intent(this, ListAlbums.class);
        //Clears the activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void startLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
