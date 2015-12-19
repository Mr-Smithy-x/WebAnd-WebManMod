package com.mrsmyx.weband.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.R;
import com.mrsmyx.weband.adapters.GameAdapter;
import com.mrsmyx.weband.fragments.GameFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParserException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Charlton on 9/5/2015.
 */
public class Network {

    public static JSONObject requestUpdate() throws JSONException {
        String data = download("https://dl.dropboxusercontent.com/u/24305940/WebAnd/version.json");
        return new JSONObject(data);
    }

    public static interface NetworkListener {
        public void OnNetworkFound(InetAddress inetAddress);
        void OnNetworkFail(String s);
    }


    public static String download(Context context, int raw) throws IOException {
        InputStream in = context.getResources().openRawResource(R.raw.game);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String buffer =  "";
        while((buffer = br.readLine())!= null){
            sb.append(buffer);
        }
        in.close();
        br.close();
        return sb.toString();
    }


    public static Map<String,List<GameAdapter.PSGame>> searchGames(Context context, String ip) throws IOException {
        String xml = String.format(GameFragment.gameUrl, ip);
        Log.e("URL", xml);
        String file = download(xml);
//        String file = download(context, R.raw.game);
        if(file == null) return null;
        Document document = Jsoup.parse(file, "", Parser.xmlParser());
        //document.outputSettings().escapeMode(Entities.EscapeMode.extended);
        List<GameAdapter.PSGame> ps3GameList = new ArrayList<GameAdapter.PSGame>();
        try {
            ps3GameList = getPS3Games(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<GameAdapter.PSGame> ps2GameList = new ArrayList<GameAdapter.PSGame>();
        try {
            ps2GameList = getPS2Games(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<GameAdapter.PSGame> pspGameList = new ArrayList<GameAdapter.PSGame>();
        try {
            pspGameList = getPSPGames(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<GameAdapter.PSGame> psxGameList = new ArrayList<GameAdapter.PSGame>();;
        try {
            psxGameList = getPSXGames(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, List<GameAdapter.PSGame>> gameList = new HashMap<String, List<GameAdapter.PSGame>>();
        gameList.put(GameAdapter.CATEGORY.PS3.name(), ps3GameList);
        gameList.put(GameAdapter.CATEGORY.PS2.name(),ps2GameList);
        gameList.put(GameAdapter.CATEGORY.PSP.name(),pspGameList);
        gameList.put(GameAdapter.CATEGORY.PSX.name(),psxGameList);
        return gameList;
    }

    private static List<GameAdapter.PSGame> getPSXGames(Document document) throws Exception {
        Element ps2_games = document.getElementById("seg_wm_psx_items");
        Elements ps2_tables = ps2_games.getElementsByTag("Table");
        List<GameAdapter.PSGame> ps2Games = new ArrayList<>();
        for (Element table : ps2_tables) {
            if(table.attr("key").equals("ps2_classic_launcher")) continue;
            Elements singleton = table.children();
            String title = "", icon = "", link = "", info = "";
            for (Element s : singleton) {
                if (s.attr("key").equals("title")) {
                    title = s.text();
                } else if (s.attr("key").equals("icon")) {
                    icon = s.text();
                } else if (s.attr("key").equals("module_action")) {
                    link = s.text().replace("127.0.0.1", Global.IP);
                } else if (s.attr("key").equals("info")) {
                    info = s.text();
                }
            }
            try {
                icon = downloadFile("http://" + Global.IP + ":80" + icon.replace(" ", "%20"), Environment.getExternalStorageDirectory().toString(), title + "icon.png").toString();
            }catch (Exception ex){
                ex.printStackTrace();
                icon = "";
            }
            ps2Games.add(new GameAdapter.PSGame(title, icon, link, info));
        }
        return ps2Games;
    }
    private static List<GameAdapter.PSGame> getPSPGames(Document document) throws Exception {
        Element ps2_games = document.getElementById("seg_wm_psp_items");
        Elements ps2_tables = ps2_games.getElementsByTag("Table");
        List<GameAdapter.PSGame> ps2Games = new ArrayList<>();
        for (Element table : ps2_tables) {
            if(table.attr("key").equals("ps2_classic_launcher")) continue;
            Elements singleton = table.children();
            String title = "", icon = "", link = "", info = "";
            for (Element s : singleton) {
                if (s.attr("key").equals("title")) {
                    title = s.text();
                } else if (s.attr("key").equals("icon")) {
                    icon = s.text();
                } else if (s.attr("key").equals("module_action")) {
                    link = s.text().replace("127.0.0.1", Global.IP);
                } else if (s.attr("key").equals("info")) {
                    info = s.text();
                }
            }
            try {
                icon = downloadFile("http://" + Global.IP + ":80" + icon.replace(" ", "%20"), Environment.getExternalStorageDirectory().toString(), title + "icon.png").toString();
            }catch (Exception ex){
                ex.printStackTrace();
                icon = "";
            }
            ps2Games.add(new GameAdapter.PSGame(title, icon, link, info));
        }
        return ps2Games;
    }

    private static List<GameAdapter.PSGame> getPS2Games(Document document) throws Exception {
        Element ps2_games = document.getElementById("seg_wm_ps2_items");
        Elements ps2_tables = ps2_games.getElementsByTag("Table");
        List<GameAdapter.PSGame> ps2Games = new ArrayList<>();
        for (Element table : ps2_tables) {
            if(table.attr("key").equals("ps2_classic_launcher")) continue;
            Elements singleton = table.children();
            String title = "", icon = "", link = "", info = "";
            for (Element s : singleton) {
                if (s.attr("key").equals("title")) {
                    title = s.text();
                } else if (s.attr("key").equals("icon")) {
                    icon = s.text();
                } else if (s.attr("key").equals("module_action")) {
                    link = s.text().replace("127.0.0.1", Global.IP);
                } else if (s.attr("key").equals("info")) {
                    info = s.text();
                }
            }

            try {
                icon = downloadFile("http://" + Global.IP + ":80" + icon.replace(" ", "%20"), Environment.getExternalStorageDirectory().toString(), title + "icon.png").toString();
            }catch (Exception ex){
                ex.printStackTrace();
                icon = "";
            }
            ps2Games.add(new GameAdapter.PSGame(title, icon, link, info));
        }
        return ps2Games;
    }
    private static List<GameAdapter.PSGame> getPS3Games(Document document) throws Exception {
        Element ps3_game = document.getElementById("seg_wm_ps3_items");
        Elements ps3_tables = ps3_game.getElementsByTag("Table");
        List<GameAdapter.PSGame> ps3Games = new ArrayList<>();
        for (Element table : ps3_tables) {
            Elements singleton = table.children();
            String title = "", icon = "", link = "", info = "";
            for (Element s : singleton) {
                if (s.attr("key").equals("title")) {
                    title = s.text();
                } else if (s.attr("key").equals("icon")) {
                    icon = s.text();
                } else if (s.attr("key").equals("module_action")) {
                    link = s.text().replace("127.0.0.1", Global.IP);
                } else if (s.attr("key").equals("info")) {
                    info = s.text();
                }
            }
            try {
                icon = downloadFile("http://" + Global.IP + ":80" + icon.replace(" ", "%20"), Environment.getExternalStorageDirectory().toString(), title + "icon.png").toString();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            ps3Games.add(new GameAdapter.PSGame(title, icon, link, info));
        }
        return ps3Games;
    }

    public static String downloadFile(String link, String dest, String filename) throws Exception {
        File f = new File(dest + "/WebMan/");
        if (!f.exists()) {
            f.mkdirs();
        }
        if (new File(dest + "/WebMan/", filename).exists()) {
            Log.e("File Exist", "The File Exist");

            return (dest + "/WebMan/" + filename);
        }
        URL url = new URL(link);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.connect();
        FileOutputStream fis = new FileOutputStream(dest + "/WebMan/" + filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = httpURLConnection.getInputStream();
        byte[] buffer = new byte[1024];
        int i;
        while ((i = is.read(buffer)) != -1) {
            baos.write(buffer, 0, i);
        }
        is.close();
        baos.close();
        httpURLConnection.disconnect();
        fis.write(baos.toByteArray());
        fis.flush();
        fis.close();
        return dest + "/WebMan/" + filename;
    }


    public static String download(String ip) {
        try {
            Log.e("DOWNLOADING", ip);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(ip).openConnection();
            con.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder s = new StringBuilder();
            String sb = null;
            while ((sb = br.readLine()) != null) {
                s.append(sb);
            }
            br.close();
            con.disconnect();
            Log.e("CONTENT", s.toString());
            return s.toString();
        } catch (Exception e) {
            Log.e("NETWORK", ip);
            Log.e("NETWORK", ip, e);
            return null;
        }
    }

    public static InetAddress getWLANipAddress(String protocolVersion) throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            if (netint.isUp() && !netint.isLoopback() && !netint.isVirtual()) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (protocolVersion.equals("IPv4")) {
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress;
                        }
                    } else {
                        if (inetAddress instanceof Inet6Address) {
                            return inetAddress;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean exists(String URLName) throws IOException {
           HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder s = new StringBuilder();
            String sb = null;
            while ((sb = br.readLine()) != null) {
                s.append(sb);
            }
            br.close();
            con.disconnect();
            Log.e("CONTENT", s.toString());
            return (s.toString().toLowerCase().contains("ps3mapi") || s.toString().toLowerCase().contains("webman") || s.toString().toLowerCase().contains("dex")|| s.toString().toLowerCase().contains("d-rex")|| s.toString().toLowerCase().contains("cex") || s.toString().toLowerCase().contains("rebug") || s.toString().toLowerCase().contains("rsx"));

    }



    public static void printReachableHosts(InetAddress inetAddress, NetworkListener networkListener) throws SocketException {
        String ipAddress = inetAddress.toString();
        ipAddress = ipAddress.substring(1, ipAddress.lastIndexOf('.')) + ".";
        for (int i = 0; i < 256; i++) {
            String otherAddress = ipAddress + String.valueOf(i);
            try {
                if (InetAddress.getByName(otherAddress.toString()).isReachable(50)) {
                    System.out.println(otherAddress);
                    try {
                        if (exists("http://" + InetAddress.getByName(otherAddress.toString()).getHostName() + ":80/index.ps3")) {
                            networkListener.OnNetworkFound(InetAddress.getByName(otherAddress.toString()));
                            return;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        networkListener.OnNetworkFail("Could not locate your PS3. Please make sure you have Webman Mod Enabled, & that your Android is on the same Network.");
    }
}
