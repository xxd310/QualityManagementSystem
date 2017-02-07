package com.zhihuitech.qualitymanagementsystem.util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2016/10/31.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance=null;
    private static final int version = 1;
    private Context mContext;
    public static final String DB_PATH = "schema";
    public static final String DB_NAME = "quality_management.db";
    public static final int DB_VERSION = 1;
    public static int oldVersion = -1;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, version);
        this.mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        if(instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeAssetsSQL(db, "quality_management.sql");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //数据库不升级
        if (newVersion <= oldVersion) {
            return;
        }
        this.oldVersion = oldVersion;
        int changeCnt = newVersion - oldVersion;
        for (int i = 0; i < changeCnt; i++) {
            // 依次执行updatei_i+1文件,由1更新到2 [1-2]，2更新到3 [2-3]
            String schemaName = "update" + (oldVersion + i) + "_" + (oldVersion + i + 1) + ".sql";
            executeAssetsSQL(db, schemaName);
        }
    }

    private void executeAssetsSQL(SQLiteDatabase db, String schemaName) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(mContext.getAssets()
                    .open(DB_PATH + "/" + schemaName)));
            System.out.println("路径:" + DB_PATH + "/" + schemaName);
            String line;
            String buffer = "";
            while ((line = in.readLine()) != null) {
                buffer += line;
                if (line.trim().endsWith(";")) {
                    db.execSQL(buffer.replace(";", ""));
                    buffer = "";
                }
            }
        } catch (IOException e) {
            Log.e("db-error", e.toString());
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                Log.e("db-error", e.toString());
            }
        }
    }
}