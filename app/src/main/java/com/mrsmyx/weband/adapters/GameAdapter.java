package com.mrsmyx.weband.adapters;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.mrsmyx.weband.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Charlton on 9/5/2015.
 */
public abstract class GameAdapter extends RecyclerView.Adapter {

    public VIEW_TYPE getViewType() {
        return view_Type;
    }

    public enum VIEW_TYPE {
        NORMAL,
        LARGE
    }

    VIEW_TYPE view_Type;

    public GameAdapter(int param, CATEGORY category, VIEW_TYPE view_Type) {
        this.param = param;
        this.category = category;
        this.view_Type = view_Type;
    }

    public Map<String, List<PSGame>> getGameList(){
        return gameList;
    }

    public void search(String query) {
        List<PSGame> games = new ArrayList<>();
        for(String key : gameList.keySet()){
            for(PSGame game : gameList.get(key)){
                if(game.getTitle().toLowerCase().contains(query.toLowerCase()) && !games.contains(game)){
                    games.add(game);
                }
            }
        }
        if(!gameList.entrySet().contains(CATEGORY.QUERY.name())){
            gameList.put(CATEGORY.QUERY.name(), games);
        }else{
            gameList.get(CATEGORY.QUERY.name()).clear();
            gameList.get(CATEGORY.QUERY.name()).addAll(games);
        }
        setViewTypeAndCategory(CATEGORY.QUERY, view_Type);
    }

    public enum CATEGORY {
        PS3,
        PSP,
        PS2,
        PSX,
        QUERY,
    }

    public final int HEADER_TYPE = 0, NORMAL_TYPE = 1;
    private int param;
    private CATEGORY category;
    private Map<String, List<PSGame>> gameList = new HashMap<String, List<PSGame>>();
    public abstract void OnGameClicked(View view, int position);
    public abstract boolean OnGameLongClicked(View view, int position);
    public abstract void OnRefreshClicked(View view);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_TYPE) {
            return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_struct, parent, false));
        } else{
            if(view_Type == VIEW_TYPE.NORMAL) {
                return new GameHolder(LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.game_struct, parent, false)
                );
            }else if(view_Type == VIEW_TYPE.LARGE){
                return new GameHolder(LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.game_struct_2, parent, false)
                );
            }
        }
        return null;
    }

    public void setViewTypeAndCategory(CATEGORY category, VIEW_TYPE viewType){
        this.view_Type = viewType;
        this.category = category;
        notifyDataSetChanged();
    }



    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == HEADER_TYPE) {
            ((HeaderHolder)holder).appCompatSpinner.setSelection(category.ordinal(),true);
        } else {
            PSGame game = getItemAtPosition(position);
            ((GameHolder) holder).title.setText(game.getTitle());
            ((GameHolder) holder).location.setText(game.getInfo());
            Picasso.with(((GameHolder) holder).img.getContext()).load(new File(game.getIcon())).into(((GameHolder) holder).img, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    ((GameHolder) holder).img.setImageResource(R.mipmap.ic_launcher);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return HEADER_TYPE;
        else return NORMAL_TYPE;
    }

    @Override
    public int getItemCount() {
        if (!gameList.keySet().contains(category.name())) gameList.put(category.name(), new ArrayList<PSGame>());
        return gameList.get(category.name()).size()+1;
    }

    public CATEGORY getCategory(){
        return category;
    }

    public PSGame getItemAtPosition(int index) {

        return gameList.get(category.name()).get(index - 1);
    }


    public void setAdapter(Map<String, List<PSGame>> gameList) {
        this.gameList = gameList;
        notifyDataSetChanged();
    }

    public void clear() {
        int size = getItemCount();
        gameList.clear();
        notifyItemRangeRemoved(0, size);
    }



    public class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView title;
        AppCompatTextView location;
        ImageView img;

        public GameHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.game_icon);
            if (param == 1)
                //img.setBackgroundColor(img.getResources().getColor(R.color.cardview_light_background));

            title = (AppCompatTextView) itemView.findViewById(R.id.game_title);
            location = (AppCompatTextView) itemView.findViewById(R.id.game_location);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OnGameClicked(v, getPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return OnGameLongClicked(v, getPosition());
        }
    }


    private class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AppCompatSpinner appCompatSpinner;
        AppCompatButton appCompatButton;
        public HeaderHolder(View itemView) {
            super(itemView);
            appCompatButton = (AppCompatButton) itemView.findViewById(R.id.header_refresh);
            appCompatSpinner = (AppCompatSpinner) itemView.findViewById(R.id.header_spinner);
            appCompatButton.setOnClickListener(this);
            appCompatSpinner.setAdapter(new ArrayAdapter<CATEGORY>(appCompatSpinner.getContext(), R.layout.support_simple_spinner_dropdown_item, CATEGORY.values()));
            appCompatSpinner.setSelection(category.ordinal(), true);
            appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    setViewTypeAndCategory((CATEGORY)parent.getItemAtPosition(pos), view_Type);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public void onClick(View v) {
            OnRefreshClicked(v);
        }
    }

    public static class PSGame {
        private String info;

        public PSGame(String title, String icon, String link, String info) {
            this.title = title;
            this.icon = icon;
            this.link = link;
            this.info = info;
        }

        public String getTitle() {
            return title;
        }

        public String getIcon() {
            return icon;
        }

        public String getLink() {
            return link;
        }

        public String title;
        public String icon;
        public String link;

        public String getInfo() {
            return info;
        }
    }
}
