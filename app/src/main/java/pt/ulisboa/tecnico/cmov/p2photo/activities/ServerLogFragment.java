package pt.ulisboa.tecnico.cmov.p2photo.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;


public class ServerLogFragment extends Fragment {


    private static final String TAG = "ServerLogFragment" ;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    public ServerLogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.server_log_fragment, container, false);

        listView = view.findViewById(R.id.logList);

        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<String>());

        listView.setAdapter(adapter);

        getServerLog();



        view.findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getServerLog();
            }
        });
        return view;
    }

    public void getServerLog(){
        try {
            ServerAPI.getInstance().getOperationsLog(getContext(),
                new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        String[] log = new String[response.length()];
                        for(int i = 0;i < response.length();i++){
                            try {
                                Log.i(TAG, response.getString(i));
                                log[i] = response.getString(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.clear();
                        adapter.addAll(log);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), getContext().getString(R.string.server_log_sync), Toast.LENGTH_SHORT).show();
                    }
                }
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
