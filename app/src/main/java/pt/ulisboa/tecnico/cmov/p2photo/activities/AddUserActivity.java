package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.MembersAdapter;

public class AddUserActivity extends AppCompatActivity {


    ArrayList <String> listUsers = new ArrayList<String>();

    MembersAdapter adapter;

    MembersAdapter adapterU;

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add User");
        setSupportActionBar(toolbar);


        List<Member> members = new ArrayList<>();

        getMembers(members);

        adapter = new MembersAdapter(this,members,R.layout.member_row);

        listView = findViewById(R.id.members);

        listView.setAdapter(adapter);

        List<Member> memberstoadd = new ArrayList<>();

        getMembers(memberstoadd);

        adapterU = new MembersAdapter(this,memberstoadd,R.layout.add_user_row);

        listView = findViewById(R.id.allusers);

        listView.setAdapter(adapterU);



    }

    private void getMembers(List<Member> members) {

        members.add(new Member("Sebastiao"));
        members.add(new Member("Leonor"));
        members.add(new Member("Andre"));
        members.add(new Member("OMEGAXD"));
        members.add(new Member("POGGERS"));

    }

}
