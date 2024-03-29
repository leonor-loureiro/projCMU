package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.AddUserActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListAlbumsActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListPhotosActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoginActivity;
import pt.ulisboa.tecnico.cmov.p2photo.security.SecurityManager;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class MembersAdapter extends ArrayAdapter<Member> implements Filterable {
    private static final String TAG = "MembersAdapter";
    private MembersAdapter currentMemberAdapter;
    private List<Member> members;
    private Context mContext;
    private int ID;
    private GlobalVariables globalVariables;
    private Album album;

    public MembersAdapter(Context context, List<Member> data, int id, Album album, MembersAdapter adapter) {
        super(context, R.layout.member_row, data);
        Log.i("Members", "Start");

        members = new ArrayList<>();
        members.addAll(data);

        mContext = context;
        ID = id;
        Log.i("Members", data.size() + "");

        this.globalVariables = (GlobalVariables) context.getApplicationContext();

        this.album = album;

        this.currentMemberAdapter = adapter;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Get album data
        final Member member = getItem(position);

        //Get inflater service
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView;
        //Check if existing view is being reused, otherwise, inflate the view
        if(convertView == null){
            itemView = inflater.inflate(this.ID, null);
        }else{
            itemView = convertView;
        }

        TextView memberName = itemView.findViewById(R.id.membername);
        memberName.setText(member.getName());

        final Button addButton =  itemView.findViewById(R.id.adduserbutton);


        //if user already added button is disabled
        if(addButton != null) {
            if(currentMemberAdapter.contains(member))
                addButton.setEnabled(false);
            else {
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addUserHandle(member, addButton);
                    }
                });
            }
        }
        
        

        return itemView;


    }

    /**
     * sends a request to the server to add the user to the album
     * @param member member to be added
     */
    private void addUserHandle(final Member member, final Button addUserButton) {
        try {
            String cipheredKey = null;
            if(globalVariables.google) {
                //Get user's public key
                PublicKey publicKey = SecurityManager.getPublicKeyFromString(member.getPublicKey());
                //Encrypt the secret key
                cipheredKey = SecurityManager.encryptRSA(publicKey, album.getSecretKey().getEncoded());
                Log.i(TAG, "Ciphered key = " + cipheredKey);
            }
            if(!currentMemberAdapter.contains(member))
                ServerAPI.getInstance().shareAlbum(mContext,
                        globalVariables.getToken(),
                        globalVariables.getUser().getName(),
                        member.getName(),
                        album.getName(),
                        cipheredKey,
                        this.globalVariables.google + "",
                        new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.i("MembersAdapter","SUCCESS: addUserHandle " + member.getName());
                            currentMemberAdapter.add(member);
                            addUserButton.setEnabled(false);
                            globalVariables.addOperation(new Operation("addUser",globalVariables.getUser().getName(),album.getName(),globalVariables.google).toString());


                        }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                            Log.i("MembersAdapter","FAILURE: addUserHandle " + member.getName());
                                Toast.makeText(mContext,
                                        mContext.getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.failed_add_member) + " " + member.getName(),
                                        Toast.LENGTH_SHORT)
                                .show();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                if(statusCode == 401)
                                    ServerAPI.getInstance().tokenInvalid(mContext);

                                // HTTP Conflict, target user already has album with such name
                                if(statusCode == 409){
                                    Toast.makeText(mContext,
                                            "Failed to share album " +album.getName() +", "+member.getName() +" already has an album with same name.",
                                            Toast.LENGTH_LONG)
                                            .show();
                                }


                            }
                        });
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }


    public void add(Member member){
        super.add(member);
        members.add(member);
        notifyDataSetChanged();
    }


    /**
     * Creates a new filter to search trough the Users in the system
     * @return the newly created filter
     */
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Member> FilteredArrayMembers = new ArrayList<Member>();


                //performm the filtering
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < members.size(); i++) {
                    String dataNames = members.get(i).getName();
                    Log.d("name",members.get(i).getName());
                    if (dataNames.toLowerCase().startsWith(constraint.toString())){
                        Log.d("text",constraint.toString());
                        FilteredArrayMembers.add(members.get(i));
                    }
                }

                results.count = FilteredArrayMembers.size();
                results.values = FilteredArrayMembers;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (results.count == 0) {
                    clear();
                    notifyDataSetInvalidated();

                } else {
                    clear();
                    addAll((List<Member>)results.values);
                    notifyDataSetChanged();
                }
                Log.d("membersize",members.size() + "");
            }
        };

        return filter;
    }

    public boolean contains(Member member){
        for(Member m : members ) {
            if (m.equals(member))
                return true;
        }
        return false;
    }

}
