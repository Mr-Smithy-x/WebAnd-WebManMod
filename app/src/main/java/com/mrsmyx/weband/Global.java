package com.mrsmyx.weband;

import android.os.Environment;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Charlton on 9/5/2015.
 */
public class Global {
    public static String IP = null;
    public static boolean CONNECTED = false;
    public static boolean ATTEMPTING = false;

    public static boolean Protected = false;

    public static void setPass(String key) throws IOException {
        String base64 = Base64.encodeToString(key.getBytes(),Base64.NO_PADDING);
        FileOutputStream fileOutputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory().toString()+"/WKEY.wbmn"));
        fileOutputStream.write(base64.getBytes(),0,base64.length());
        fileOutputStream.flush();
        fileOutputStream.close();
    }
    public static String getPass() throws IOException {
        File f = new File(Environment.getExternalStorageDirectory().toString()+"/WKEY.wbmn");
        if(f.exists()) {
            FileInputStream fileInputStream = new FileInputStream(f);
            byte[] buffer = new byte[(int) f.length()];
            fileInputStream.read(buffer, 0, (int) f.length());
            fileInputStream.close();
            return new String(buffer).toString();
        }

        return null;
    }


}
