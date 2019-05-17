package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;

public class ChooseModeActivity extends Activity {

    Button GoogleButton;

    Button P2PButton;
    private GlobalVariables globalVariables;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        this.globalVariables = (GlobalVariables)getApplicationContext();


        P2PButton = findViewById(R.id.chooseP2p);

        P2PButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                P2PMode();
                startLoginActivity(view);
            }
        });

        GoogleButton = findViewById(R.id.chooseGoogle);

        GoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleMode();
                startLoginActivity(view);
            }
        });
    }

    public void P2PMode(){

        globalVariables.google = false;


    }

    public void GoogleMode(){

        globalVariables.google = true;

    }

    public void startLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
