package com.progiii.mailclientserver.client.utils;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GravatarRequests
{
    public static Image getProfilePicture(String id)
    {
        Image ret = null;
        try{
        String hash = MD5Util.md5Hex(id);
        URL url = new URL("https://www.gravatar.com/avatar/" + hash + "?d=monsterid");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int response = con.getResponseCode();
            if (response == 200)
            {
                InputStream stream = con.getInputStream();
                ret = new Image(stream);
            }
        }catch (Exception e){e.printStackTrace();}
        return ret;
    }
}
