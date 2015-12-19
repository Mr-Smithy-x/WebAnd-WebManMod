package com.mrsmyx.weband;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.mrsmyx.weband.adapters.GameAdapter;
import com.mrsmyx.weband.adapters.MainPagerAdapter;
import com.mrsmyx.weband.dialogfragments.SearchPS3Dialog;
import com.mrsmyx.weband.fragments.CommandFragment;
import com.mrsmyx.weband.fragments.FileTransferFragment;
import com.mrsmyx.weband.fragments.GameFragment;
import com.mrsmyx.weband.fragments.KeyDialog;
import com.mrsmyx.weband.struct.PS3Info;
import com.mrsmyx.weband.utils.Network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

public class MainActivity extends AppCompatActivity implements GameFragment.OnDrawerFeedback, Network.NetworkListener, SearchPS3Dialog.OnFeedBack, View.OnClickListener, MenuAsync.MenuAsyncListener, NavigationView.OnNavigationItemSelectedListener, KeyDialog.KeyDialogListener {

    //region Fields
    private CoordinatorLayout mCoords;
    private AppBarLayout mAppBar;
    private CollapsingToolbarLayout mCollapse;
    private Toolbar mToolbar;
    private ImageView mImageView;
    private TabLayout mTab;
    private ViewPager mPager;
    private MainPagerAdapter mAdapter;
    private FloatingActionButton mFab, mSmallFab;
    private NavigationView mNav;
    private ProgressDialog pd;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawer;
    private SearchView mSearch;
    private PS3Info ps3Info = PS3Info.Build();
    private android.os.Handler handler = new android.os.Handler();
    private SwitchCompat mSwitch;
    private PowerManager.WakeLock wakeLock;
    private BroadcastReceiver br;
    private AppCompatEditText mMsgConsoleEditText;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //region BROADCAST RECIEVER, PICKS UP WHETHER YOU HAVE JUST CONNECTED OR DISCONNECTED WIFI
        registerReceiver(br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("NETWORK CHANGE RECIEVER", "onReceive - intent => " + intent.getAction());
                String action = intent.getAction();
                final ConnectivityManager connMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                final android.net.NetworkInfo wifi = connMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (!wifi.isConnected()) {
                    Global.CONNECTED = false;
                    Toast.makeText(context, "ERROR, YOU ARE NOT CONNECTED TO WIFI", Toast.LENGTH_LONG).show();
                } else if (wifi.isConnectedOrConnecting() && wifi.isConnected() && Global.ATTEMPTING == false) {
                    Global.ATTEMPTING = true;
                    start();
                }
            }
        }, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        //endregion
        wakeLock.acquire();
        init();
        complete();
    }

    //region Events when returning and exiting the application
    @Override
    protected void onPause() {
        super.onPause();
        wakeLock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPager.setOffscreenPageLimit(4);
        wakeLock.acquire();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getIntent().getBooleanExtra("fanup", false)) {
            Toast.makeText(this, "Fan Up", Toast.LENGTH_SHORT).show();
            getIntent().getExtras().clear();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
        wakeLock.release();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
    //endregion

    private void startProgress() {
        if (pd == null) {
            pd = new ProgressDialog(this);
        }
        pd.setTitle("Please wait...");
        pd.setMessage("Locating PS3...");
        pd.setIndeterminate(true);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        if (!pd.isShowing()) {
            pd.show();
        }
    }

    private void complete() {
        mCollapse.setCollapsedTitleTextColor(getResources().getColor(R.color.md_white_1000));
        mCollapse.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        mCollapse.setTitle("WebAnd - Beta");
        mToolbar.setTitle("WebAnd - Beta");
        mPager.setAdapter(mAdapter = new MainPagerAdapter(getSupportFragmentManager(), new ArrayList<>(
                Arrays.asList(
                        MainPagerAdapter.TabPage.Build(getString(R.string.main_act_command_text), new CommandFragment()),
                        MainPagerAdapter.TabPage.Build(getString(R.string.main_act_games_text), new GameFragment()),
                        MainPagerAdapter.TabPage.Build("FTP", new FileTransferFragment())

                )
                //getString(R.string.main_act_settings_text)
        )));
        mPager.setOffscreenPageLimit(4);
        mTab.setupWithViewPager(mPager);
        mFab.setOnClickListener(this);
        mPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTab) {

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    CircularRevealGone(mFab);
                } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                    CircularReveal(mFab);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                ;
            }

            @Override
            public void onPageSelected(final int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 0:
                        mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_pink_A700)));
                        mFab.setImageResource(R.mipmap.ic_wifi_tethering_white_48dp);
                        break;
                    case 1:
                        mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_indigo_A700)));
                        mFab.setImageResource(R.mipmap.ic_autorenew_white_48dp);
                        break;
                    case 2:
                        mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_purple_A700)));
                        mFab.setImageResource(R.mipmap.ic_add_white_48dp);
                        mAppBar.setExpanded(false, true);

                        break;
                }

            }
        });
    }

    //region Animations for VERSION >= LOLLIPOP
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
    //endregion

    public void init() {
        mCoords = (CoordinatorLayout) findViewById(R.id.main_coord);
        mAppBar = (AppBarLayout) findViewById(R.id.main_appbar);
        mCollapse = (CollapsingToolbarLayout) findViewById(R.id.main_collapse);
        mImageView = (ImageView) findViewById(R.id.main_image);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mTab = (TabLayout) findViewById(R.id.main_tab);
        mPager = (ViewPager) findViewById(R.id.main_pager);
        mFab = (FloatingActionButton) findViewById(R.id.main_fab);
        mDrawer = (DrawerLayout) findViewById(R.id.main_drawer);
        setSupportActionBar(mToolbar);
        mNav = (NavigationView) findViewById(R.id.main_nav);
        mNav.setNavigationItemSelectedListener(this);
        mMsgConsoleEditText = (AppCompatEditText) mNav.getHeaderView(0).findViewById(R.id.msg_console);
        //region SENDS MSG TO CONSOLE -> LOCATED IN NAVIGATION MENU
        mMsgConsoleEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Network.download("http://" + Global.IP + ":80/popup.ps3/" + URLEncoder.encode(mMsgConsoleEditText.getText().toString(), "UTF-8").replace("+", "%20"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    return true;
                }
                return false;
            }
        });
        //endregion
        mSmallFab = (FloatingActionButton) findViewById(R.id.small_fab);
        mSmallFab.setOnClickListener(this);
        mCollapse.setTitle("WebAnd - Beta");
        mDrawer.setDrawerListener(mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.app_name, R.string.app_name) {

        });
        mSwitch = (SwitchCompat) mNav.getHeaderView(0).findViewById(R.id.header_switch);
        mDrawer.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        if (!getSharedPreferences("WebAnd", MODE_PRIVATE).getBoolean("ack", false)) {
            AlertDialog.Builder s = new AlertDialog.Builder(this);
            s.setMessage("Beta Version, Created By Mr Smithy x");
            s.setTitle("Welcome");
            s.setPositiveButton("Acknowledge", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getSharedPreferences("WebAnd", MODE_PRIVATE).edit().putBoolean("ack", true).commit();
                    dialog.dismiss();
                }
            });
            s.show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //region Look for connections
    public boolean start() {
        //startProgress();
        Searching();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Network.printReachableHosts(Network.getWLANipAddress("IPv4"), MainActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
            return true;
        } else {
            return false;
        }
    }

    public void start(final String ip) {
        startProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Network.exists("http://" + ip + ":80/index.ps3")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Global.CONNECTED = true;
                                Global.IP = ip;
                                try {
                                    OnNetworkFound(InetAddress.getByName(ip));
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(MainActivity.this).setTitle("Error").setMessage(e.getMessage()).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                        }
                    });
                }

            }
        }).start();
    }
    //endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearch = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        mSearch.setQueryHint(getString(R.string.search_my_games));
        mSearch.setQueryRefinementEnabled(true);
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                mSearch.onActionViewCollapsed();
                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                mCollapse.setTitle("WebAnd - Beta");
                ((GameFragment) mAdapter.getItem(1)).searchGame(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (mPager.getCurrentItem() != 1) {
                    mPager.setCurrentItem(1, true);
                }
                if (newText.length() > 0) ((GameFragment) mAdapter.getItem(1)).searchGame(newText);
                return false;
            }
        });
        mSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mCollapse.setTitle("WebAnd - Beta");
                mSearch.onActionViewCollapsed();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //region UPDATE_CODE
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = Network.requestUpdate();
                        final String changelog = jsonObject.getString("changelog");
                        final String version = jsonObject.getString("version");
                        final String link = jsonObject.getString("link");
                        final String update_message = jsonObject.getString("update_message");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PackageInfo pInfo = null;
                                try {
                                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                                    String ver = pInfo.versionName;
                                    Log.i("VERSION 1", version);
                                    Log.i("VERSION 2", ver);

                                    if (Float.valueOf(version) > Float.valueOf(ver)) {
                                        new AlertDialog.Builder(MainActivity.this).setTitle("Information").setMessage(update_message + "\nChangeLog:\n" + changelog)
                                                .setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    final String file = Network.downloadFile(link, Environment.getExternalStorageDirectory().toString(), "WebAnd_Beta.apk");
                                                                    handler.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                                                            intent.setDataAndType(Uri.fromFile(new File(file)), "application/vnd.android.package-archive");
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                            MainActivity.this.startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    });
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }).start();
                                                    }
                                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                    } else {
                                        Snackbar.make(getWindow().getDecorView(), R.string.main_up_to_date_text, Snackbar.LENGTH_LONG).show();
                                    }

                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();

                                    Snackbar.make(getWindow().getDecorView(), "Could not update", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            //endregion
            return true;
        } else if (id == R.id.action_exit) {
            MainActivity.this.finish();
        } else if (id == R.id.action_normal_view) {
            if (mPager.getCurrentItem() != 1) {
                mPager.setCurrentItem(1, true);
            }
            ((GameFragment) mAdapter.getItem(1)).setViewType(GameAdapter.VIEW_TYPE.NORMAL);
        } else if (id == R.id.action_large_view) {
            if (mPager.getCurrentItem() != 1) {
                mPager.setCurrentItem(1, true);
            }
            ((GameFragment) mAdapter.getItem(1)).setViewType(GameAdapter.VIEW_TYPE.LARGE);
        }else if(id == R.id.action_confid){
            try {
                if(ps3Info != null) {

                        new KeyDialog().show(getSupportFragmentManager(),"Key");

                }
                else{
                    Toast.makeText(this,"You're not actively connected to a ps3.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchActivity(String key) {
        Intent i = new Intent(this, ConfidentialActivity.class);
        i.putExtra(ConfidentialActivity.CONFID_KEY, ps3Info);
        i.putExtra(ConfidentialActivity.PASS, key);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(i, ActivityOptionsCompat.makeScaleUpAnimation(mToolbar, (int)mToolbar.getX(), (int)mToolbar.getY(), mToolbar.getWidth(), mToolbar.getHeight()).toBundle());
        }else{
            startActivity(i);
        }
    }

    @Override
    public void OnNetworkFound(final InetAddress inetAddress) {
        final String hostname = inetAddress.getHostAddress();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (pd != null && pd.isShowing()) pd.dismiss();
                Global.IP = hostname;
                Global.CONNECTED = true;
                Global.ATTEMPTING = false;
                Snackbar.make(getWindow().getDecorView(), getString(R.string.main_ps3_connected_at) + Global.IP, Snackbar.LENGTH_SHORT).show();
                ((GameFragment) mAdapter.getItem(1)).start(MainActivity.this);
                //((SettingsFragment) mAdapter.getItem(2)).start();
                try {
                    ((FileTransferFragment) mAdapter.getItem(2)).connect(hostname);
                } catch (FTPException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FTPIllegalReplyException e) {
                    e.printStackTrace();
                }
                new MenuAsync(MainActivity.this).execute(ps3Info);
            }
        });
    }

    @Override
    public void OnNetworkFail(final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.small_fab:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Network.download("http://" + Global.IP + ":80/refresh.ps3");
                    }
                }).start();
                break;
            case R.id.main_fab:
                switch (mTab.getSelectedTabPosition()) {
                    case 0:
                        start();
                        break;
                    case 1:
                        break;
                    case 2:


                        ((FileTransferFragment) mAdapter.getItem(mPager.getCurrentItem())).uploadHere();

                        break;
                }
                break;
        }
    }

    @Override
    public void OnFinished(PS3Info ps3Info) {
        try {
            this.ps3Info = ps3Info;
            mNav.getMenu().findItem(R.id.main_nav_firm).setTitle(ps3Info.getFIRM());
            mNav.getMenu().findItem(R.id.main_nav_ip).setTitle(getString(R.string.ps3_ip) + Global.IP);
            mNav.getMenu().findItem(R.id.main_nav_cpu).setTitle(mSwitch.isChecked() ? ps3Info.getCPU() : ps3Info.getCPU_C());
            mNav.getMenu().findItem(R.id.main_nav_rsx).setTitle(mSwitch.isChecked() ? ps3Info.getRSX() : ps3Info.getRSX_C());
            mNav.getMenu().findItem(R.id.main_nav_hdd).setTitle(ps3Info.getHDD());
            mNav.getMenu().findItem(R.id.main_nav_mem).setTitle(ps3Info.getMEM());
            mNav.getMenu().findItem(R.id.main_nav_fan).setTitle(ps3Info.getFAN().replace("FAN SPEED:", getResources().getString(R.string.ps3_fan)));
            mNav.getMenu().findItem(R.id.main_nav_utime).setTitle(getString(R.string.ps3_uptime) + ps3Info.getUP_T());
            notification();
            ((AppCompatTextView) mNav.getHeaderView(0).findViewById(R.id.header_firmware)).setText(mSwitch.isChecked() ? getString(R.string.temp_imp_unit) : getString(R.string.temp_met_unit));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (!isDestroyed() || !isFinishing()){
                            new MenuAsync(MainActivity.this).execute(MainActivity.this.ps3Info);
                        }
                    }else{
                        if(!isFinishing()){
                            new MenuAsync(MainActivity.this).execute(MainActivity.this.ps3Info);
                        }
                    }
                }
            }, 5000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void OnFail() {
        if (!Global.CONNECTED) {
            if (!start()) {

            }
        }
    }

    @Override
    public void OnFeedBack(SearchPS3Dialog searchPS3Dialog, String ip) {
        start(ip);
        searchPS3Dialog.dismiss();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.main_fan_mode:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Network.download("http://" + Global.IP + ":80/cpursx.ps3?mode");
                    }
                }).start();
                break;
            case R.id.main_fan_up:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Network.download("http://" + Global.IP + ":80/cpursx.ps3?up");
                    }
                }).start();
                break;
            case R.id.main_fan_dowm:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Network.download("http://" + Global.IP + ":80/cpursx.ps3?dn");
                    }
                }).start();
                break;
            case R.id.main_game_xml:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String download = Network.download(String.format(GameFragment.gameUrl, Global.IP));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(ClipData.newPlainText("GAMES XML", download));
                                Toast.makeText(MainActivity.this, R.string.main_copied_text, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                break;
        }
        return false;
    }

    @Override
    public void OnDrawer() {
        mNav.getMenu().setGroupVisible(R.id.grp_split, true);
        mDrawer.openDrawer(GravityCompat.START);
    }

    //region NOTIFICATION DISPLAY: SEARCHING FOR PS3 & SHOWING PS3 STATS
    public void Searching() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.main_please_wait_text))
                .setContentText(getString(R.string.main_search_for_ps3_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(true).build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }


    public void notification() {
        if (isFinishing()) return;
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(ps3Info.getFIRM())
                .setContentText(mSwitch.isChecked() ? ps3Info.getCPU() : ps3Info.getCPU_C())
                .setContentInfo(mSwitch.isChecked() ? ps3Info.getRSX() : ps3Info.getRSX_C())
                .setSubText(getString(R.string.ps3_ip) + Global.IP)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(true).build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);

    }

    @Override
    public void OnSubmitListener(String key) throws IOException {
        String val = Global.getPass();
        if(val == null){
            Global.setPass(key);
            launchActivity(key);
        }else if(val.equals(Base64.encodeToString(key.getBytes(), Base64.NO_PADDING))){
            launchActivity(key);
        }else{
            Toast.makeText(this,"Invalid Key", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

}
