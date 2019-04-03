package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Toast;

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

    ListView listViewMembers;

    ListView listViewAllUsers;

    private EditText filterText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        List<Member> members = new ArrayList<>();

        getMembers(members);

        adapter = new MembersAdapter(this,members,R.layout.member_row);

        listViewMembers = findViewById(R.id.members);

        listViewMembers.setAdapter(adapter);

        List<Member> memberstoadd = new ArrayList<>();

        getMembers(memberstoadd);

        adapterU = new MembersAdapter(this,memberstoadd,R.layout.add_user_row);

        listViewAllUsers= findViewById(R.id.allusers);

        listViewAllUsers.setAdapter(adapterU);


        int id = findViewById(R.id.searchView).getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        filterText = findViewById(R.id.searchView).findViewById(id);

        listViewMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Position " + position, Toast.LENGTH_LONG).show();
            }
        });
        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AddUserActivity.this.adapterU.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



    }


    private void getMembers(List<Member> members) {

        members.add(new Member("Sebastiao"));
        members.add(new Member("Leonor"));
        members.add(new Member("Andre"));
        members.add(new Member("OMEGAXD"));
        members.add(new Member("POGGERS"));

    }

    public void goBackToAlbums(View view) {
        finish();
    }
}
