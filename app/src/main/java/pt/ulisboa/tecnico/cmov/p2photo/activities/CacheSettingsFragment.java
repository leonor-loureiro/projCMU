package pt.ulisboa.tecnico.cmov.p2photo.activities;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;


/**
 * This fragment allows a user to change the maximum cache size
 */
public class CacheSettingsFragment extends Fragment {


    NumberPicker numberPicker;
    SharedPreferences sharedPref;

    public CacheSettingsFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cache_settings, container, false);

        sharedPref = getContext().getSharedPreferences(getString(R.string.cache_settings_pref), Context.MODE_PRIVATE);

        numberPicker = view.findViewById(R.id.numberPicker);

        int heapSize = (int) Math.floor(Runtime.getRuntime().maxMemory()/1024./1024.);

        //Set min cache size
        numberPicker.setMinValue(0);
        //Set max cache size
        numberPicker.setMaxValue(heapSize /2);
        //Set current value
        numberPicker.setValue(sharedPref.getInt(getString(R.string.cache_size_pref), heapSize /4));

        //Set format of value
        numberPicker.setFormatter(new NumberPicker.Formatter(){
            @Override
            public String format(int i) {
                return i + getString(R.string.size_unit);
            }
        });

        //Add save changes listener
        view.findViewById(R.id.saveChanges).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCacheSize();
            }
        });

        return view;
    }

    private void updateCacheSize() {
        //TODO save cache size key as resource
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.cache_size_pref), numberPicker.getValue());
        editor.apply();
        Toast.makeText(getContext(),
                getString(R.string.max_cache_size) + numberPicker.getValue() + getString(R.string.size_unit),
                Toast.LENGTH_SHORT)
        .show();
        ((GlobalVariables)getContext().getApplicationContext()).getCacheManager().getCacheSize();
    }

}
