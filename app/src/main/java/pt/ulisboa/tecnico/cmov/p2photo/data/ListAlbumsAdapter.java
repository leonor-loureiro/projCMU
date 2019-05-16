package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListPhotosActivity;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class ListAlbumsAdapter extends ArrayAdapter<Album>{
    private List<Album> albums;
    private Context mContext;

    private GlobalVariables globalVariables;


    public ListAlbumsAdapter(Context context, List<Album> data, GlobalVariables globalVariables) {
        super(context, R.layout.album_item, data);
        Log.i("Album", "Start");

        albums = data;
        mContext = context;
        this.globalVariables = globalVariables;
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

/*    public void getFileIDOfAlbums(){
        for( int i = 0;i < albums.size();i++){
            Log.d("fileid","getting id of file" + albums.get(i).getName());
            final int j = i;
            final String name = albums.get(i).getName();
                try {
                    ServerAPI.getInstance().getFileID(mContext,
                            globalVariables.getToken(),
                            globalVariables.getUser().getName(),
                            name, this.globalVariables.google + "",
                            new JsonHttpResponseHandler() {

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                    try {

                                        albums.get(j).setFileID((response.get(0).toString()));
                                        Log.i("fileid", "newFileID = " + (response.get(0).toString()));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    if (statusCode == 401)
                                        ServerAPI.getInstance().tokenInvalid(mContext);

                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

    } */

    public String getFileID(String album) {

        for(int i = 0; i < albums.size();i++){
            if(albums.get(i).getName().equals(album))
                return albums.get(i).getFileID();
        }

        return null;
    }
}
