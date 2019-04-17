package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class MembersAdapter extends ArrayAdapter<Member> implements Filterable {
    private MembersAdapter currentMemberAdapter;
    private List<Member> members;
    private Context mContext;
    private int ID;
    private GlobalVariables globalVariables;
    private String album;

    public MembersAdapter(Context context, List<Member> data, int id, String album, MembersAdapter adapter) {
        super(context, R.layout.member_row, data);
        Log.i("Members", "Start");

        members = new ArrayList<>();
        for(int i = 0;i < data.size();i++){
            members.add(data.get(i));
        }
        mContext = context;
        ID = id;
        Log.i("Members", data.size() + "");

        this.globalVariables = (GlobalVariables) context.getApplicationContext();

        this.album = album;

        this.currentMemberAdapter = adapter;
    }

    public List<Member> getMembers() {
        return members;
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

    private void addUserHandle(final Member member, final Button addUserButton) {
        try {
            if(!currentMemberAdapter.contains(member))
                ServerAPI.getInstance().shareAlbum(mContext,
                        globalVariables.getToken(),
                        globalVariables.getUser().getName(),
                        member.getName(),
                        album,
                        new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.i("MembersAdapter","SUCCESS: addUserHandle " + member.getName());
                            currentMemberAdapter.add(member);
                            addUserButton.setEnabled(false);

                        }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.i("MembersAdapter","FAILURE: addUserHandle " + member.getName());
                                Toast.makeText(mContext,
                                        mContext.getString(pt.ulisboa.tecnico.cmov.p2photo.R.string.failed_add_member) + " " + member.getName(),
                                        Toast.LENGTH_SHORT)
                                .show();
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


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Member> FilteredArrayMembers = new ArrayList<Member>();

                // perform your search here using the searchConstraint String.

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

    public void removeAll(List<Member> currentAdded){
        members.removeAll(currentAdded);
        notifyDataSetChanged();
    }
}
