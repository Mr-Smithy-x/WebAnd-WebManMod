package com.mrsmyx.weband.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charlton on 9/5/2015.
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {
    List<TabPage> pages = new ArrayList<>();

    public MainPagerAdapter(FragmentManager fm, ArrayList<TabPage> pages) {
        super(fm);
        this.pages = pages;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pages.get(position).getTitle();
    }

    @Override
    public Fragment getItem(int position) {
        return pages.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    public static class TabPage{
        String title;
        Fragment fragment;

        public static TabPage Build(String title, Fragment fragment){
            TabPage tabPage = new TabPage();
            tabPage.title = title;
            tabPage.fragment = fragment;
            return tabPage;
        }

        public String getTitle(){return title;}
        public Fragment getFragment(){return fragment;}

    }
}
