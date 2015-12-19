package com.mrsmyx.weband.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.R;

/**
 * Created by Charlton on 9/5/2015.
 */
public class SettingsFragment extends Fragment {

    private WebView mWeb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        mWeb = (WebView) view.findViewById(R.id.set_web);
        mWeb.getSettings().setBuiltInZoomControls(true);

        mWeb.setWebViewClient( new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        return view;
    }
    public void start() {
        if(mWeb != null){
            mWeb.getSettings().setJavaScriptEnabled(true);
            mWeb.getSettings().setAppCacheEnabled(true);
            mWeb.getSettings().setAllowFileAccess(true);
            mWeb.getSettings().setLoadsImagesAutomatically(true);
            mWeb.loadUrl("http://" + Global.IP + ":80");
        }
    }
}
