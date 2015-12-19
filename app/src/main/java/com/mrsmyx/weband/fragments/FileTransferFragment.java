package com.mrsmyx.weband.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.R;
import com.mrsmyx.weband.adapters.FTPAdapter;
import com.mrsmyx.weband.adapters.IOAdapter;
import com.mrsmyx.weband.utils.Network;

import java.io.File;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/**
 * Created by CJ on 9/27/2015.
 */
public class FileTransferFragment extends Fragment {

    private RecyclerView recyclerView, ioRecycler;
    private FTPAdapter ftpAdapter;
    private IOAdapter ioAdapter;
    public boolean isLocal = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ftp_fragment, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.ftp_recycler);
        ioRecycler = (RecyclerView) view.findViewById(R.id.ftp_home_recycler);
        ioRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ioRecycler.setItemViewCacheSize(10);
        ioRecycler.setAdapter(ioAdapter = new IOAdapter() {
            @Override
            public void OnIOClicked(View view, int position) {
                Snackbar.make(view, getCurrentDir().listFiles()[position - 1].getName(), Snackbar.LENGTH_LONG).show();
                ftpAdapter.upload(getCurrentDir().listFiles()[position - 1], view.getContext());
            }

            @Override
            public void OnIOFolderClicked(View view, int position) {
                changeDir(getCurrentDir().listFiles()[position - 1]);
            }

            @Override
            public boolean OnIOLongClicked(View view, final int position) {

                PopupMenu popupMenu =  makePopupup(view, GravityCompat.END, R.menu.menu_local);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.local_open:
                                final File file = ioAdapter.getCurrentDir().listFiles()[position - 1];
                                break;
                            case R.id.local_details:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(ftpAdapter = new FTPAdapter() {

            @Override
            public void OnFTPClicked(View view, int position) {
                Snackbar.make(view, String.valueOf(position), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public boolean OnFTPLongClicked(View view, final int position) {
                PopupMenu popupMenu = makePopupup(view, GravityCompat.END, R.menu.menu_ftp);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.ftp_upload:
                                final Handler h = new Handler();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            FTPFile f = ftpAdapter.getFileAtPosition(position - 1);
                                            String file = null;
                                            Log.e("FTP FILE", file = "http://" + Global.IP + ":80" + ftpClient.currentDirectory() + "/" + f.getName());
                                            String filename = file.substring(file.lastIndexOf('/') + 1, file.length());

                                            Log.e("FTP FILE", filename);
                                            Network.downloadFile(file, Environment.getExternalStorageDirectory().toString() + "/FTP", filename);
                                            h.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "Finished", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            h.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                }).start();
                                break;
                            case R.id.ftp_details:
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return true;
            }

            @Override
            public void OnFTPFolderClicked(View view, int position) {
                Snackbar.make(view, getFileAtPosition(position - 1).getName(), Snackbar.LENGTH_LONG).show();
                ftpAdapter.changeDir(getFileAtPosition(position - 1).getName());
            }
        });
        return view;
    }


    public PopupMenu makePopupup(View view, int gravity, int menuId) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view, gravity);
        popupMenu.inflate(menuId);
        return popupMenu;
    }

    public void connect(String ip) throws FTPException, IOException, FTPIllegalReplyException {
        ftpAdapter.connect(ip);
    }


    public void uploadHere() {
        isLocal = !isLocal;
        if (isLocal) {
            CircularRevealGone(recyclerView);
            CircularReveal(ioRecycler);
        } else {
            CircularRevealGone(ioRecycler);
            CircularReveal(recyclerView);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void CircularReveal(View myView) {
        int cx = myView.getWidth() / 2;
        int cy = myView.getHeight() / 2;
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        myView.setVisibility(View.VISIBLE);
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void CircularRevealGone(final View myView) {
        int cx = myView.getWidth() / 2;
        int cy = myView.getHeight() / 2;
        int initialRadius = myView.getWidth();
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });

// start the animation
        anim.start();
    }


}
