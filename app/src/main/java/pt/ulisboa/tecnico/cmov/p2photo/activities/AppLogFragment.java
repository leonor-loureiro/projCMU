package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;

public class AppLogFragment extends Fragment {

    private static final String TAG = "AppLogFragment" ;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private GlobalVariables globalVariables;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.app_log_fragment, container, false);

        listView = view.findViewById(R.id.ApplogList);

        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<String>());

        listView.setAdapter(adapter);


        getAppLog();

        return view;
    }

    private void getAppLog() {
        this.globalVariables = (GlobalVariables)getContext().getApplicationContext();
        adapter.clear();
        adapter.addAll(globalVariables.getOperationsLog());
        adapter.notifyDataSetChanged();

    }
}
