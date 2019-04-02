package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.R;

public class MembersAdapter extends ArrayAdapter<Member> implements Filterable {
    private List<Member> members;
    private Context mContext;
    private int ID;

    public MembersAdapter(Context context, List<Member> data,int id) {
        super(context, R.layout.member_row, data);
        Log.i("Members", "Start");

        members= data;
        mContext = context;
        ID = id;
        Log.i("Members", data.size() + "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Get album data
        Member member = getItem(position);

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

        //Set album name
        TextView memberName = (TextView) itemView.findViewById(R.id.membername);
        memberName.setText(member.getName());

        return itemView;


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
                    notifyDataSetInvalidated();
                } else {
                    members = (List<Member>) results.values;
                    notifyDataSetChanged();
                }
                for (int i = 0; i < members.size(); i++) {
                    String dataNames = members.get(i).getName();
                    if (dataNames.toLowerCase().startsWith(constraint.toString())) {
                        Log.d("text", members.get(i).getName());
                    }
                }
            }
        };

        return filter;
    }
}
