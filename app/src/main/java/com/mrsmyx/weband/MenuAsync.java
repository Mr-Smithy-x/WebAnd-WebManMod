package com.mrsmyx.weband;

import android.os.AsyncTask;
import android.util.Log;

import com.mrsmyx.weband.struct.PS3Info;
import com.mrsmyx.weband.utils.Network;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Created by Charlton on 9/7/2015.
 */
public class MenuAsync extends AsyncTask<PS3Info, Integer, PS3Info> {
    MenuAsyncListener menuAsyncListener;

    public interface MenuAsyncListener {
        public void OnFinished(PS3Info info);
        public void OnFail();
    }

    public MenuAsync(MenuAsyncListener menuAsyncListener) {
        this.menuAsyncListener = menuAsyncListener;
    }

    @Override
    protected PS3Info doInBackground(PS3Info... params) {
        if (Global.CONNECTED == true) {
            try {
                String document = Network.download("http://" + Global.IP + ":80/cpursx.ps3");
                Document d = Jsoup.parse(document);
                params[0].setUP_T(d.getElementsByTag("label").last().nextSibling().toString());
                Elements docs = d.getElementsByClass("s");
                for (Element element : docs) {
                    switch (element.attr("href")) {
                        case "/cpursx.ps3?mode":
                            if (element.textNodes().size() > 0) {
                                params[0].setFAN(element.textNodes().get(0).text());
                            }
                            break;
                        case "/cpursx.ps3?up":
                            if (element.textNodes().size() > 0) {
                                params[0].setCPU_C(element.textNodes().get(0).text());
                                params[0].setRSX_C(element.textNodes().get(1).text());
                            }
                            break;
                        case "/cpursx.ps3?dn":
                            if (element.textNodes().size() > 0) {
                                params[0].setCPU(element.textNodes().get(0).text());
                                params[0].setRSX(element.textNodes().get(1).text());
                            }
                            break;
                        case "/games.ps3":
                            if (element.textNodes().size() > 0) {
                                params[0].setMEM(element.textNodes().get(0).text());
                                params[0].setHDD(element.textNodes().get(1).text());
                            }
                            break;
                        case "/setup.ps3":
                            if (element.textNodes().size() > 0) {
                                params[0].setFIRM(element.textNodes().get(0).text());
                                params[0].setPSID(element.textNodes().get(1).text());
                                params[0].setIDPSEID0(element.textNodes().get(2).text());
                                params[0].setIDPSLV2(element.textNodes().get(3).text());
                                params[0].setMACAddr(element.textNodes().get(4).text());
                            }
                            break;
                    }

                }
                return params[0];
            } catch (Exception ex) {
                return null;
            }
        }else {
            return null;
        }
    }


    @Override
    protected void onPostExecute(PS3Info ps3Info) {
        super.onPostExecute(ps3Info);
        if (ps3Info != null) menuAsyncListener.OnFinished(ps3Info);
        else menuAsyncListener.OnFail();
    }
}

