package com.mrsmyx.weband.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.MainActivity;
import com.mrsmyx.weband.R;
import com.mrsmyx.weband.adapters.GameAdapter;
import com.mrsmyx.weband.utils.Network;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by Charlton on 9/5/2015.
 */
public class GameFragment extends Fragment {

    public void setViewType(GameAdapter.VIEW_TYPE viewType) {
        GameAdapter.CATEGORY cate = gameAdapter.getCategory();
        GameAdapter.VIEW_TYPE view = gameAdapter.getViewType();
        Map<String, List<GameAdapter.PSGame>> gameList = gameAdapter.getGameList();
        gRecycler.setAdapter(gameAdapter = new GameAdapter(1, cate, viewType) {
            @Override
            public void OnGameClicked(View view, final int position) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Network.download(getItemAtPosition(position).getLink());
                    }
                }).start();
                Snackbar.make(view, getResources().getString(R.string.game_frag_launching_text) + getItemAtPosition(position).getTitle(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public boolean OnGameLongClicked(View view, int position) {
                return true;
            }

            @Override
            public void OnRefreshClicked(View view) {
                start(view.getContext());
                Snackbar.make(view, R.string.game_frag_game_refreshed_text, Snackbar.LENGTH_SHORT).show();
            }
        });
        gameAdapter.setAdapter(gameList);
    }

    public interface OnDrawerFeedback {
        void OnDrawer();
    }

    public final static String gameUrl = "http://%s/dev_hdd0/xmlhost/game_plugin/mygames.xml";

    RecyclerView gRecycler;
    GameAdapter gameAdapter;
    OnDrawerFeedback onDrawerFeedback;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.game_fragment, container, false);
        gRecycler = (RecyclerView) view.findViewById(R.id.game_recycler);
        gRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        gRecycler.setHasFixedSize(true);
        gRecycler.setAdapter(gameAdapter = new GameAdapter(1, GameAdapter.CATEGORY.PS3, GameAdapter.VIEW_TYPE.LARGE) {

            @Override
            public void OnGameClicked(View view, final int position) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Network.download(getItemAtPosition(position).getLink());
                    }
                }).start();
                Snackbar.make(view, getResources().getString(R.string.game_frag_launching_text) + getItemAtPosition(position).getTitle(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public boolean OnGameLongClicked(View view, int position) {
                return true;
            }

            @Override
            public void OnRefreshClicked(View view) {
                start(view.getContext());
                Snackbar.make(view, R.string.game_frag_game_refreshed_text, Snackbar.LENGTH_SHORT).show();
            }

        });
        return view;
    }


    android.os.Handler handler = new android.os.Handler();


    public void start(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Map<String, List<GameAdapter.PSGame>> gameList = Network.searchGames(context, Global.IP);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(gameList == null){
                                Toast.makeText(GameFragment.this.getActivity(), "Error, it seems that this error is unrecoverable. Please click on the floating action button to resync your ps3", Toast.LENGTH_LONG).show();
                                return;
                            }
                            gameAdapter.clear();

                            gRecycler.setItemViewCacheSize(gameList.size());
                            gameAdapter.setAdapter(gameList);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Could not find games", Toast.LENGTH_LONG);
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            e.printStackTrace(pw);
                            String s = sw.toString();
                            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("COPY ERROR", s));
                            Toast.makeText(getContext(), "Error has been copied, please report it to Mr Smithy x on NextGenUpdate with the error that has been copied.", Toast.LENGTH_LONG).show();
                            onDrawerFeedback.OnDrawer();
                        }
                    });
                }

            }
        }).start();
    }

    public void searchGame(String query) {
        gameAdapter.search(query);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            onDrawerFeedback = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDrawerFeedback = null;
    }
}
