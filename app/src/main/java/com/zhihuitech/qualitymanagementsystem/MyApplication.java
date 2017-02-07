package com.zhihuitech.qualitymanagementsystem;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.zhihuitech.qualitymanagementsystem.entity.User;
import org.json.JSONArray;

/**
 * Created by Administrator on 2016/12/30.
 */
public class MyApplication extends Application {
    private User user;
    private JSONArray dataArray;

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "pzglxt";

    public void saveDataToPref() {
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("data", dataArray.toString());
        edit.commit();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JSONArray getDataArray() {
        return dataArray;
    }

    public void setDataArray(JSONArray dataArray) {
        this.dataArray = dataArray;
    }
}
