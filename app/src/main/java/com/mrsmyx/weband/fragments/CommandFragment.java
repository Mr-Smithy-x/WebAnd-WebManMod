package com.mrsmyx.weband.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.R;
import com.mrsmyx.weband.adapters.CommandAdapter;
import com.mrsmyx.weband.utils.Network;

/**
 * Created by Charlton on 9/5/2015.
 */
public class CommandFragment extends Fragment {

    private RecyclerView gRecycler;
    private CommandAdapter commandAdapter;
    private Handler handler = new Handler();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.command_fragment, container, false);
        gRecycler = (RecyclerView) view.findViewById(R.id.command_recycler);
        gRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        gRecycler.setAdapter(commandAdapter = new CommandAdapter() {

            @Override
            public void OnCommandSelected(final View view, final int position) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (Global.CONNECTED) {
                            Network.download(String.format(getItemAtPosition(position).getCommand(), Global.IP));
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Snackbar.make(view,getResources().getString(R.string.game_frag_sent_text) + getItemAtPosition(position).getTitle(), Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Snackbar.make(view, "Not Connected :/" + getItemAtPosition(position).getTitle(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }

            @Override
            public boolean OnCommandLongSelected(View view, int position) {
                return false;
            }
        });
        start();
        return view;
    }


    public void start(){
        commandAdapter.clear();
        commandAdapter.append(CommandAdapter.Command.Build().setTitle(getResources().getString(R.string.game_frag_refresh_text)).setIcon(R.mipmap.ic_launcher).setCommand("http://%s:80/refresh.ps3").setDesc(getResources().getString(R.string.game_frag_refresh_desc_text)));
        commandAdapter.append(CommandAdapter.Command.Build().setTitle(getResources().getString(R.string.game_frag_unmount_text)).setIcon(R.mipmap.ic_launcher).setCommand("http://%s:80/mount.ps3/unmount").setDesc(getResources().getString(R.string.game_frag_unmount_desc_text)));
        commandAdapter.append(CommandAdapter.Command.Build().setTitle(getResources().getString(R.string.game_frag_insert_text)).setIcon(R.mipmap.ic_launcher).setCommand("http://%s:80/insert.ps3").setDesc(getResources().getString(R.string.game_frag_insert_desc_text)));
        commandAdapter.append(CommandAdapter.Command.Build().setTitle(getResources().getString(R.string.game_frag_eject_text)).setIcon(R.mipmap.ic_launcher).setCommand("http://%s:80/eject.ps3").setDesc(getResources().getString(R.string.game_frag_eject_desc_text)));
        commandAdapter.append(CommandAdapter.Command.Build().setTitle(getResources().getString(R.string.game_frag_restart_text)).setIcon(R.mipmap.ic_launcher).setCommand("http://%s:80/restart.ps3").setDesc(getResources().getString(R.string.game_frag_restart_desc_text) ));
        commandAdapter.append(CommandAdapter.Command.Build().setTitle(getResources().getString(R.string.game_frag_shutdown_text)).setIcon(R.mipmap.ic_launcher).setCommand("http://%s:80/shutdown.ps3").setDesc(getResources().getString(R.string.game_frag_shutdown_desc_text)));
    }
}
