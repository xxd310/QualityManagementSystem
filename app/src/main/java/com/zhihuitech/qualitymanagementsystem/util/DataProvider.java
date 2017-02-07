package com.zhihuitech.qualitymanagementsystem.util;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/19.
 */
public class DataProvider {
    public final static String ROOT_URL = "http://quality.jinlanzuan.com";

    public static String login(String username, String password) {
        NameValuePair usernamePair = new BasicNameValuePair("username", username);
        NameValuePair passwordPair = new BasicNameValuePair("password", password);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(usernamePair);
        pairs.add(passwordPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php/Api/App/userLogin", pairs);
        System.out.println("userLogin.result=" + result);
        return result;
    }

    public static String getAllInfo() {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php/Api/App/getAllInfo", pairs);
        System.out.println("getAllInfo.result=" + result);
        return result;
    }

    public static String getAccept(String uid, String record_time, String actual_time) {
        NameValuePair uidPair = new BasicNameValuePair("uid", uid);
        NameValuePair recordTimePair = new BasicNameValuePair("record_time", record_time);
        NameValuePair actualTimePair = new BasicNameValuePair("actual_time", actual_time);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(uidPair);
        pairs.add(recordTimePair);
        pairs.add(actualTimePair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php/Api/App/getAccept", pairs);
        System.out.println("getAccept.result=" + result);
        return result;
    }

    public static String syncData(String data) {
        System.out.println("syncData.data=" + data);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        NameValuePair dataPair = new BasicNameValuePair("data", data);
        pairs.add(dataPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php/Api/App/syncData", pairs);
        System.out.println("syncData.result=" + result);
        return result;
    }

    public static String imgYun(String path) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        NameValuePair imgUrlPair = new BasicNameValuePair("imgurl", ImageUtils.encode(path));
        pairs.add(imgUrlPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php/Api/App/imgYun", pairs);
        System.out.println("imgYun.result=" + result);
        return result;
    }

}
