package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.R;

public class ListAlbumsAdapter extends ArrayAdapter<Album>{
    private List<Album> albums;
    private Context mContext;

    public ListAlbumsAdapter(Context context, List<Album> data) {
        super(context, R.layout.album_item, data);
        Log.i("Album", "Start");

        albums = data;
        mContext = context;
        Log.i("Albums", data.size() + "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Get album data
        Album album = getItem(position);

        //Get inflater service
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView;
        //Check if existing view is being reused, otherwise, inflate the view
        if(convertView == null){
            itemView = inflater.inflate(R.layout.album_item, null);
        }else{
            itemView = convertView;
        }

        //Set album name
        TextView albumName = (TextView) itemView.findViewById(R.id.album_name);
        albumName.setText(album.getName());

        return itemView;


    }

    public boolean contains(String albumName) {
        for (Album album:
             albums) {

            if(album.getName().equals(albumName))
                return true;
        }
        return false;
    }
}
