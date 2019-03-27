package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.R;

public class MembersAdapter extends ArrayAdapter<Member>{
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
}
