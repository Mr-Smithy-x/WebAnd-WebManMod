package com.mrsmyx.weband.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SearchView;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.MainActivity;
import com.mrsmyx.weband.R;

/**
 * Created by Charlton on 9/10/2015.
 */
public class SearchPS3Dialog extends AppCompatDialogFragment {

    public interface OnFeedBack{
        public void OnFeedBack(SearchPS3Dialog searchPS3Dialog, String ip);
    }

    OnFeedBack onFeedBack;
    AppCompatEditText searchView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_ps3_dialog, container, false);
        searchView = (AppCompatEditText) view.findViewById(R.id.search_ps3_view);
        searchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    onFeedBack.OnFeedBack(SearchPS3Dialog.this, searchView.getText().toString());
                    return true;
                }
                return false;
            }
        });
        return  view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d =  super.onCreateDialog(savedInstanceState);
        d.setTitle("Manual PS3 IP");
        return  d;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFeedBack = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onFeedBack = (MainActivity)activity;
    }
}
