package com.mrsmyx.weband;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mrsmyx.weband.struct.PS3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cj on 12/19/15.
 */
public class ConfidentialActivity extends AppCompatActivity {

    public static final String CONFID_KEY = "CONFID";
    public static final String PASS = "PASS";
    private PS3Info ps3Info;
    private RecyclerView recyclerView;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confidential_activity);
        recyclerView = (RecyclerView) findViewById(R.id.confidential_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ps3Info = (PS3Info) getIntent().getSerializableExtra(CONFID_KEY);
        pass = getIntent().getStringExtra(PASS);
        if(ps3Info!=null){
            recyclerView.setAdapter(new RecAdapter());
        }else{
            finish();
        }
    }

    class RecAdapter extends RecyclerView.Adapter<RecAdapter.ConfidHolder>{


        @Override
        public RecAdapter.ConfidHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ConfidHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_confid, parent, false));
        }

        @Override
        public void onBindViewHolder(RecAdapter.ConfidHolder holder, int position) {
            String data = getItemAtPosition(position);
            try {
                String val = Base64.encodeToString(pass.getBytes(), Base64.NO_PADDING);
                if(!Global.getPass().equals(val)) {
                    holder.textView.setText(data.substring(0, data.length() / 2));
                }else{
                    holder.textView.setText(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
                holder.textView.setText(data.substring(0, data.length()/2));
            }
        }

        public String getItemAtPosition(int index){
            return ps3Info.toConfidArrayList().get(index);
        }

        @Override
        public int getItemCount() {
            return ps3Info.toConfidArrayList().size();
        }

        public class ConfidHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            TextView textView;
            public ConfidHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.confid_text);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {

            }

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        }
    }

}
