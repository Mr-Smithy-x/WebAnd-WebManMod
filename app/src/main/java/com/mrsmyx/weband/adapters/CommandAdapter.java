package com.mrsmyx.weband.adapters;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mrsmyx.weband.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charlton on 9/6/2015.
 */
public abstract class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandHolder> {

    List<Command> commandList = new ArrayList<Command>();
    @Override
    public CommandHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommandHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.command_struct, parent, false));
    }

    @Override
    public void onBindViewHolder(CommandHolder holder, int position) {
        holder.title.setText(getItemAtPosition(position).getTitle());
        holder.desc.setText(getItemAtPosition(position).getDesc());
    }

    public Command getItemAtPosition(int position) {
        return commandList.get(position);
    }

    @Override
    public int getItemCount() {
        return commandList.size();
    }

    public abstract void OnCommandSelected(View view, int position);
    public abstract boolean OnCommandLongSelected(View view, int position);

    public void append(Command command) {
        commandList.add(command);
        notifyItemInserted(commandList.size());
    }

    public void clear() {
        int size = commandList.size();
        commandList.clear();
        notifyItemRangeRemoved(0,size);
    }

    public class CommandHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView title, desc;
        ImageView icon;
        public CommandHolder(View itemView) {
            super(itemView);
            title = (AppCompatTextView) itemView.findViewById(R.id.command_title);
            desc = (AppCompatTextView) itemView.findViewById(R.id.command_desc);
            icon = (ImageView) itemView.findViewById(R.id.command_icon);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OnCommandSelected(v, getPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return OnCommandLongSelected(v, getPosition());
        }
    }

    public static class Command {
        public Command setTitle(String title) {
            this.title = title;
            return this;
        }

        public Command setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public Command setCommand(String command) {
            this.command = command;
            return this;
        }

        public Command setIcon(int icon) {
            this.icon = icon;
            return this;
        }

        private String title, desc, command;

        public String getTitle() {
            return title;
        }

        public String getDesc() {
            return desc;
        }

        public String getCommand() {
            return command;
        }

        public int getIcon() {
            return icon;
        }

        private  int icon;
        public static Command Build(){
            return new Command();
        }


    }
}
