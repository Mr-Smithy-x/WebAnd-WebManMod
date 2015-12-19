package com.mrsmyx.weband.adapters;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Environment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mrsmyx.weband.R;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;

import it.sauronsoftware.ftp4j.FTPFile;

/**
 * Created by CJ on 9/28/2015.
 */
public abstract class IOAdapter extends RecyclerView.Adapter<IOAdapter.IOHolder> {

    private final int HEADER = 0, NORMAL =1;
    private File currentDir;

    public IOAdapter() {
        currentDir = Environment.getExternalStorageDirectory();
    }

    public File getCurrentDir(){
        return currentDir;
    }

    @Override
    public IOHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == HEADER){
            return new IOHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ftp_card, parent, false));
        }else if(viewType == NORMAL){
            return new IOHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ftp_card, parent, false));
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == HEADER){
            return HEADER;
        }else return NORMAL;
    }

    @Override
    public void onBindViewHolder(IOHolder holder, int position) {
        if(getItemViewType(position) == HEADER){
            holder.imageView.setImageResource(R.mipmap.ic_folder_black_48dp);
            holder.title.setText("..");
            holder.sub.setText("Go up");
        }else if(getItemViewType(position) == NORMAL){
            if(currentDir.listFiles()[position-1].isDirectory()){
                holder.imageView.setImageResource(R.mipmap.ic_folder_black_48dp);
            }else{
                holder.imageView.setImageResource(R.mipmap.ic_insert_drive_file_black_48dp);
            }
            holder.title.setText(currentDir.listFiles()[position-1].getName());
            holder.sub.setText(new Date(currentDir.listFiles()[position-1].lastModified()).toLocaleString());
        }
    }

    @Override
    public int getItemCount() {
        if(currentDir.list() != null)
        return currentDir.list().length+1;
        else return 1;
    }

    public void goUp(){
        currentDir = currentDir.getParentFile();
        notifyDataSetChanged();
    }
    public void changeDir(File file){
        this.currentDir = file;
        notifyDataSetChanged();
    }
    public abstract void OnIOClicked(View view, int position);
    public abstract void OnIOFolderClicked(View view, int position);
    public abstract boolean OnIOLongClicked(View view, int position);

    public class IOHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView title, sub;
        ImageView imageView;
        @SuppressLint("NewApi")
        public IOHolder(View itemView) {
            super(itemView);
            title = (AppCompatTextView) itemView.findViewById(R.id.ftp_title);
            sub = (AppCompatTextView) itemView.findViewById(R.id.ftp_sub);
            imageView = (ImageView) itemView.findViewById(R.id.ftp_img);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }



        @Override
        public void onClick(View v) {
            if(getLayoutPosition() > 0 &&  currentDir.listFiles()[getLayoutPosition()-1].isDirectory()){
                OnIOFolderClicked(v, getLayoutPosition());
                return;
            }else if(getLayoutPosition() == 0){
                goUp();
                return;
            }
            OnIOClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return OnIOLongClicked(v, getLayoutPosition());
        }

    }


}
