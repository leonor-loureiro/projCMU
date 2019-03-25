package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import pt.ulisboa.tecnico.cmov.p2photo.R;

public class AddUserActivity extends AppCompatActivity {


    ArrayList <String> listUsers = new ArrayList<String>();

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add User");
        setSupportActionBar(toolbar);

        ListView Members = (ListView)findViewById(R.id.members);

        String[] membersS =new String[]{"Leonor","Andre","Sebastiao","OMEGAXD","Pepega","Poggers","4Head"};

        ArrayList<String> members = new ArrayList<String>();

        members.addAll(Arrays.asList(membersS));

        ArrayAdapter listAdapter = new ArrayAdapter<String>(this,R.layout.member_row,members);

        Members.setAdapter(listAdapter);

        ListView AllUsers = (ListView)findViewById(R.id.allusers);

        String[] allusers =new String[]{"Leonor","Andre","Sebastiao"};

        ArrayList<String> alluserS = new ArrayList<String>();

        alluserS.addAll(Arrays.asList(allusers));

        ArrayAdapter allUserslistAdapter = new ArrayAdapter<String>(this,R.layout.add_user_row,alluserS);

        AllUsers.setAdapter(allUserslistAdapter);

    }

}
