package com.mrsmyx.weband.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.MainActivity;
import com.mrsmyx.weband.R;

import java.io.IOException;

/**
 * Created by cj on 12/19/15.
 */
public class KeyDialog extends AppCompatDialogFragment implements View.OnClickListener {

    public interface KeyDialogListener{
        void OnSubmitListener(String key) throws IOException;
    }

    KeyDialogListener keyDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        keyDialogListener = (MainActivity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        keyDialogListener = null;
    }

    AppCompatEditText appCompatEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.key_layout, container, false);
        appCompatEditText = (AppCompatEditText) view.findViewById(R.id.key_text);
        try {
            if(Global.getPass() == null){
                getDialog().setTitle("Create A Key!");
            }else{
                getDialog().setTitle("Enter Your Key!");
            }
        } catch (IOException e) {
            getDialog().setTitle("Create A Key!");
            e.printStackTrace();
        }
        view.findViewById(R.id.submit_btn).setOnClickListener(this);
        view.findViewById(R.id.cancel_btn).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit_btn:
                try {
                    keyDialogListener.OnSubmitListener(appCompatEditText.getText().toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                dismiss();
                break;
            case R.id.cancel_btn:
                dismiss();
                break;
        }
    }
}
