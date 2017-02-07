package com.zhihuitech.qualitymanagementsystem;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.zhihuitech.qualitymanagementsystem.entity.PathUrl;
import com.zhihuitech.qualitymanagementsystem.entity.User;
import com.zhihuitech.qualitymanagementsystem.util.*;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseContext;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {
    private EditText etUsername;
    private EditText etPassword;
    private CheckBox cbRememberUsername;
    private Button btnLogin;

    private boolean isChecked;

    private String username;
    private String password;

    // 处理数据库所需的对象
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;
    // 处理数据库所需的对象
    private SharedPreferences pref = null;
    private final static String PREF_NAME = "quality_management";

    private MyApplication myApp;

    private final static int LOGIN = 1;
    private final static int GET_ALL_INFO = 2;
    private final static int DOWNLOAD_IMAGE = 3;
    private final static int GET_ACCEPT = 4;
    private final static int DOWNLOAD_IMAGE_ACCEPT = 5;

    private List<PathUrl> allPathUrls = new ArrayList<>();
    private String record_time = "0";
    private String actual_time = "0";


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN:
                    CustomViewUtil.dismissDialog();
                    parseLoginResult((String)msg.obj);
                    break;
                case GET_ALL_INFO:
                    parseGetAllInfoResult((String)msg.obj);
                    break;
                case DOWNLOAD_IMAGE:
                    CustomViewUtil.dismissDialog();
                    if(msg.obj.toString().equals("success")) {
                        new Thread() {
                            @Override
                            public void run() {
                                String result = DataProvider.getAccept(myApp.getUser().getId(), record_time, actual_time);
                                Message msg = handler.obtainMessage();
                                msg.what = GET_ACCEPT;
                                msg.obj = result;
                                handler.sendMessage(msg);
                            }
                        }.start();
                    } else {
                        CustomViewUtil.createToast(LoginActivity.this, "图片下载失败，请重试！");
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        try {
                            db.execSQL("delete from qu_project");
                            db.execSQL("delete from qu_area");
                            db.execSQL("delete from qu_classify");
                            db.execSQL("delete from qu_task");
                            db.execSQL("delete from qu_acceptance_standard");
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if(db != null) {
                                db.close();
                            }
                        }
                    }
                    break;
                case GET_ACCEPT:
                    parseGetAcceptResult((String)msg.obj);
                    break;
                case DOWNLOAD_IMAGE_ACCEPT:
                    CustomViewUtil.dismissDialog();
                    if(msg.obj.toString().equals("success")) {
                        Intent intent = new Intent(LoginActivity.this, ProjectListActivity.class);
                        startActivity(intent);
                    } else {
                        CustomViewUtil.createToast(LoginActivity.this, "图片下载失败，请重试！");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initData();
    }

    private void initData() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);

        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        username = pref.getString("username", "");
        isChecked = pref.getBoolean("isChecked", false);
        etUsername.setText(username);
        cbRememberUsername.setChecked(isChecked);
    }

    private void findViews() {
        etUsername = (EditText) findViewById(R.id.et_username_login);
        etPassword = (EditText) findViewById(R.id.et_password_login);
        cbRememberUsername = (CheckBox) findViewById(R.id.cb_remember_username_login);
        btnLogin = (Button) findViewById(R.id.btn_login_login);
    }

    private void addListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = etUsername.getText().toString();
                password = etPassword.getText().toString();
                if(username.equals("") || password.equals("")) {
                    CustomViewUtil.createToast(LoginActivity.this, "用户名和密码都不能为空！");
                } else {
                    CustomViewUtil.createProgressDialog(LoginActivity.this, "正在验证用户信息...");
                    // 如果网络连接正常，在线验证用户信息
                    if(HttpUtil.checkConnection(LoginActivity.this)) {
                        new Thread() {
                            @Override
                            public void run() {
                                String result = DataProvider.login(username, password);
                                Message msg = handler.obtainMessage();
                                msg.obj = result;
                                msg.what = LOGIN;
                                handler.sendMessage(msg);
                            }
                        }.start();
                    }
                    // 如果网络连接不可用，本地验证用户信息
                    else {
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        try {
                            Cursor cursor = db.rawQuery("select * from qu_user where username=? and password=?", new String[]{username, MD5.GetMD5Code(MD5.GetMD5Code(password))});
                            if(cursor.getCount() == 0) {
                                cursor = db.rawQuery("select * from qu_user where tel=? and password=?", new String[]{username, MD5.GetMD5Code(MD5.GetMD5Code(password))});
                            }
                            if(cursor.getCount() == 1) {
                                while (cursor.moveToNext()) {
                                    User user = new User();
                                    user.setId(cursor.getString(cursor.getColumnIndex("id")));
                                    user.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                                    user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                                    user.setTel(cursor.getString(cursor.getColumnIndex("tel")));
                                    user.setGroup_id(cursor.getString(cursor.getColumnIndex("group_id")));
                                    user.setProject_id(cursor.getString(cursor.getColumnIndex("project_id")));
                                    user.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
                                    myApp.setUser(user);
                                }
                                saveDataToPref();
                                CustomViewUtil.dismissDialog();
                                Intent intent = new Intent(LoginActivity.this, ProjectListActivity.class);
                                startActivity(intent);
                            } else {
                                CustomViewUtil.dismissDialog();
                                CustomViewUtil.createToast(LoginActivity.this, "用户名或密码错误！");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if(db != null) {
                                db.close();
                            }
                        }
                    }
                }
            }
        });
    }

    private void saveDataToPref() {
        SharedPreferences.Editor edit = pref.edit();
        if(cbRememberUsername.isChecked()) {
            edit.putString("username", username);
            edit.putBoolean("isChecked", true);
        } else {
            edit.putString("username", "");
            edit.putBoolean("isChecked", false);
        }
        edit.commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                HideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    // 判定是否需要隐藏
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top
                    && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    // 隐藏软键盘
    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void parseLoginResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createToast(LoginActivity.this, result);
            } else {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if (resultObject.has("status") && !resultObject.isNull("status")) {
                        if (resultObject.getInt("status") == -1) {
                            CustomViewUtil.createToast(LoginActivity.this, "该用户不存在！");
                            Cursor c = db.rawQuery("select * from qu_user where username=?", new String[]{username});
                            if (c.getCount() == 0) {
                                c = db.rawQuery("select * from qu_user where tel=?", new String[]{username});
                                if (c.getCount() == 1) {
                                    db.delete("qu_user", "tel=?", new String[]{username});
                                }
                            } else {
                                db.delete("qu_user", "username=?", new String[]{username});
                            }
                        } else if (resultObject.getInt("status") == 0) {
                            CustomViewUtil.createToast(LoginActivity.this, resultObject.getString("info"));
                        } else if (resultObject.getInt("status") == 1) {
                            if (resultObject.has("user") && !resultObject.isNull("user")) {
                                JSONObject userObject = resultObject.getJSONObject("user");
                                Cursor userCursor = db.rawQuery("select * from qu_user where id=?", new String[]{userObject.getString("id")});
                                ContentValues cv = new ContentValues();
                                cv.put("id", userObject.getString("id"));
                                cv.put("username", userObject.getString("username"));
                                cv.put("password", userObject.getString("password"));
                                cv.put("tel", userObject.getString("tel"));
                                cv.put("group_id", userObject.getString("group_id"));
                                cv.put("project_id", userObject.getString("project_id"));
                                cv.put("remark", userObject.getString("remark"));
                                cv.put("code", userObject.getString("code"));
                                if (userCursor.getCount() > 0) {
                                    db.update("qu_user", cv, "id=?", new String[]{userObject.getString("id")});
                                } else {
                                    db.insert("qu_user", null, cv);
                                }

                                User user = new User();
                                user.setId(userObject.getString("id"));
                                user.setUsername(userObject.getString("username"));
                                user.setPassword(userObject.getString("password"));
                                user.setTel(userObject.getString("tel"));
                                user.setGroup_id(userObject.getString("group_id"));
                                user.setProject_id(userObject.getString("project_id"));
                                user.setRemark(userObject.getString("remark"));
                                myApp.setUser(user);

                                saveDataToPref();
                                new Thread() {
                                    @Override
                                    public void run() {
                                        String result = DataProvider.getAllInfo();
                                        Message msg = handler.obtainMessage();
                                        msg.what = GET_ALL_INFO;
                                        msg.obj = result;
                                        handler.sendMessage(msg);
                                    }
                                }.start();


                                Cursor recordCursor = db.rawQuery("select * from qu_record where user_id=? order by id desc limit 0,1", new String[]{myApp.getUser().getId()});
                                String record_id = "";
                                while (recordCursor.moveToNext()) {
                                    record_time = recordCursor.getString(recordCursor.getColumnIndex("create_time"));
                                    record_id = recordCursor.getString(recordCursor.getColumnIndex("id"));
                                }
                                Cursor actualCursor = db.rawQuery("select * from qu_actual_situation where record_id=? order by id desc limit 0,1", new String[]{record_id});
                                while (actualCursor.moveToNext()) {
                                    actual_time = actualCursor.getString(actualCursor.getColumnIndex("create_time"));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(db != null) {
                        db.close();
                    }
                }
            }
        }
    }

    private void parseGetAllInfoResult(String result) {
            if(result != null && !result.equals("")) {
                if(result.contains("超时")) {
                    CustomViewUtil.createToast(LoginActivity.this, result);
                } else {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    try {
                        JSONObject resultObject = new JSONObject(result);
                        allPathUrls.clear();
                        if(resultObject.has("project") && !resultObject.isNull("project")) {
                            JSONObject projectObject = resultObject.getJSONObject("project");
                            if(projectObject.has("value") && !projectObject.isNull("value")) {
                                JSONArray valueArray = projectObject.getJSONArray("value");
                                for (int i = 0; i < valueArray.length(); i++) {
                                    JSONObject valueObject = valueArray.getJSONObject(i);
                                    if(valueObject.optInt("is_del") == 1) {
                                        db.delete("qu_project", "id=?", new String[]{valueObject.optString("id")});
                                        continue;
                                    }
                                    ContentValues cv = new ContentValues();
                                    cv.put("id", valueObject.optString("id"));
                                    cv.put("project_name", valueObject.optString("project_name"));
                                    cv.put("url", valueObject.optString("url"));
                                    cv.put("description", valueObject.optString("description"));
                                    cv.put("lasttime", valueObject.optString("lasttime"));
                                    cv.put("local_url", Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource" + valueObject.optString("local_url").substring(valueObject.optString("local_url").lastIndexOf("/")));
                                    Cursor c = db.rawQuery("select * from qu_project where id=?", new String[]{valueObject.optString("id")});
                                    if(c.getCount() == 0) {
                                        db.insert("qu_project", null, cv);
                                        if(!valueObject.optString("url").equals("")) {
                                            allPathUrls.add(new PathUrl(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource", valueObject.optString("url"), valueObject.optString("local_url")));
                                        }
                                    } else {
                                        while (c.moveToNext()) {
                                            // 如果记录更新过
                                            if(c.getLong(c.getColumnIndex("lasttime")) < valueObject.optLong("lasttime")) {
                                                if(!valueObject.optString("url").equals("") && !c.getString(c.getColumnIndex("url")).equals(valueObject.optString("url"))) {
                                                    allPathUrls.add(new PathUrl(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource", valueObject.optString("url"), valueObject.optString("local_url")));
                                                }
                                                db.update("qu_project", cv, "id=?", new String[]{valueObject.optString("id")} );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(resultObject.has("area") && !resultObject.isNull("area")) {
                            JSONObject areaObject = resultObject.getJSONObject("area");
                            if(areaObject.has("value") && !areaObject.isNull("value")) {
                                JSONArray valueArray = areaObject.getJSONArray("value");
                                for (int i = 0; i < valueArray.length(); i++) {
                                    JSONObject valueObject = valueArray.getJSONObject(i);
                                    if(valueObject.optInt("is_del") == 1) {
                                        db.delete("qu_area", "id=?", new String[]{valueObject.optString("id")});
                                        continue;
                                    }
                                    ContentValues cv = new ContentValues();
                                    cv.put("id", valueObject.optString("id"));
                                    cv.put("project_id", valueObject.optString("project_id"));
                                    cv.put("area_name", valueObject.optString("area_name"));
                                    cv.put("description", valueObject.optString("description"));
                                    cv.put("lasttime", valueObject.optString("lasttime"));
                                    Cursor c = db.rawQuery("select * from qu_area where id=?", new String[]{valueObject.optString("id")});
                                    if(c.getCount() == 0) {
                                        db.insert("qu_area", null, cv);
                                    } else {
                                        while (c.moveToNext()) {
                                            // 如果记录更新过
                                            if(c.getLong(c.getColumnIndex("lasttime")) < valueObject.optLong("lasttime")) {
                                                db.update("qu_area", cv, "id=?", new String[]{valueObject.optString("id")} );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(resultObject.has("classify") && !resultObject.isNull("classify")) {
                            JSONObject classifyObject = resultObject.getJSONObject("classify");
                            if(classifyObject.has("value") && !classifyObject.isNull("value")) {
                                JSONArray valueArray = classifyObject.getJSONArray("value");
                                for (int i = 0; i < valueArray.length(); i++) {
                                    JSONObject valueObject = valueArray.getJSONObject(i);
                                    if(valueObject.optInt("is_del") == 1) {
                                        db.delete("qu_classify", "id=?", new String[]{valueObject.optString("id")});
                                        continue;
                                    }
                                    ContentValues cv = new ContentValues();
                                    cv.put("id", valueObject.optString("id"));
                                    cv.put("project_id", valueObject.optString("project_id"));
                                    cv.put("area_id", valueObject.optString("area_id"));
                                    cv.put("classify_name", valueObject.optString("classify_name"));
                                    cv.put("description", valueObject.optString("description"));
                                    cv.put("lasttime", valueObject.optString("lasttime"));
                                    Cursor c = db.rawQuery("select * from qu_classify where id=?", new String[]{valueObject.optString("id")});
                                    if(c.getCount() == 0) {
                                        db.insert("qu_classify", null, cv);
                                    } else {
                                        while (c.moveToNext()) {
                                            // 如果记录更新过
                                            if(c.getLong(c.getColumnIndex("lasttime")) < valueObject.optLong("lasttime")) {
                                                db.update("qu_classify", cv, "id=?", new String[]{valueObject.optString("id")} );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(resultObject.has("task") && !resultObject.isNull("task")) {
                            JSONObject taskObject = resultObject.getJSONObject("task");
                            if(taskObject.has("value") && !taskObject.isNull("value")) {
                                JSONArray valueArray = taskObject.getJSONArray("value");
                                for (int i = 0; i < valueArray.length(); i++) {
                                    JSONObject valueObject = valueArray.getJSONObject(i);
                                    if(valueObject.optInt("is_del") == 1) {
                                        db.delete("qu_task", "id=?", new String[]{valueObject.optString("id")});
                                        continue;
                                    }
                                    ContentValues cv = new ContentValues();
                                    cv.put("id", valueObject.optString("id"));
                                    cv.put("project_id", valueObject.optString("project_id"));
                                    cv.put("area_id", valueObject.optString("area_id"));
                                    cv.put("classify_id", valueObject.optString("classify_id"));
                                    cv.put("task_name", valueObject.optString("task_name"));
                                    cv.put("url", valueObject.optString("url"));
                                    cv.put("description", valueObject.optString("description"));
                                    cv.put("lasttime", valueObject.optString("lasttime"));
                                    cv.put("local_url", Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource" + valueObject.optString("local_url").substring(valueObject.optString("local_url").lastIndexOf("/")));
                                    Cursor c = db.rawQuery("select * from qu_task where id=?", new String[]{valueObject.optString("id")});
                                    if(c.getCount() == 0) {
                                        db.insert("qu_task", null, cv);
                                        if(!valueObject.optString("url").equals("")) {
                                            allPathUrls.add(new PathUrl(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource", valueObject.optString("url"), valueObject.optString("local_url")));
                                        }
                                    } else {
                                        while (c.moveToNext()) {
                                            // 如果记录更新过
                                            if(c.getLong(c.getColumnIndex("lasttime")) < valueObject.optLong("lasttime")) {
                                                if(!valueObject.optString("url").equals("") && !c.getString(c.getColumnIndex("url")).equals(valueObject.optString("url"))) {
                                                    allPathUrls.add(new PathUrl(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource", valueObject.optString("url"), valueObject.optString("local_url")));
                                                }
                                                db.update("qu_task", cv, "id=?", new String[]{valueObject.optString("id")} );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(resultObject.has("acceptance") && !resultObject.isNull("acceptance")) {
                            JSONObject acceptanceObject = resultObject.getJSONObject("acceptance");
                            if(acceptanceObject.has("value") && !acceptanceObject.isNull("value")) {
                                JSONArray valueArray = acceptanceObject.getJSONArray("value");
                                for (int i = 0; i < valueArray.length(); i++) {
                                    JSONObject valueObject = valueArray.getJSONObject(i);
                                    if(valueObject.optInt("is_del") == 1) {
                                        db.delete("qu_acceptance_standard", "id=?", new String[]{valueObject.optString("id")});
                                        continue;
                                    }
                                    ContentValues cv = new ContentValues();
                                    cv.put("id", valueObject.optString("id"));
                                    cv.put("project_id", valueObject.optString("project_id"));
                                    cv.put("area_id", valueObject.optString("area_id"));
                                    cv.put("classify_id", valueObject.optString("classify_id"));
                                    cv.put("task_id", valueObject.optString("task_id"));
                                    cv.put("accept_name", valueObject.optString("accept_name"));
                                    cv.put("standard", valueObject.optString("standard"));
                                    cv.put("url", valueObject.optString("url"));
                                    cv.put("lasttime", valueObject.optString("lasttime"));
                                    cv.put("local_url", Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource" + valueObject.optString("local_url").substring(valueObject.optString("local_url").lastIndexOf("/")));
                                    Cursor c = db.rawQuery("select * from qu_acceptance_standard where id=?", new String[]{valueObject.optString("id")});
                                    if(c.getCount() == 0) {
                                        db.insert("qu_acceptance_standard", null, cv);
                                        if(!valueObject.optString("url").equals("")) {
                                            allPathUrls.add(new PathUrl(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource", valueObject.optString("url"), valueObject.optString("local_url")));
                                        }
                                    } else {
                                        while (c.moveToNext()) {
                                            // 如果记录更新过
                                            if(c.getLong(c.getColumnIndex("lasttime")) < valueObject.optLong("lasttime")) {
                                                if(!valueObject.optString("url").equals("") && !c.getString(c.getColumnIndex("url")).equals(valueObject.optString("url"))) {
                                                    allPathUrls.add(new PathUrl(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource", valueObject.optString("url"), valueObject.optString("local_url")));
                                                }
                                                db.update("qu_acceptance_standard", cv, "id=?", new String[]{valueObject.optString("id")} );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(db != null) {
                            db.close();
                        }
                    }
                    if(allPathUrls.size() > 0) {
                        CustomViewUtil.createProgressDialog(LoginActivity.this, "下载离线所需资源中，请稍后...");
                        ImageDownloader.getInstance().startDownload(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource", allPathUrls,
                                new ImageDownloader.DownloadStateListener() {

                                    @Override
                                    public void onFinish() {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                Message msg = handler.obtainMessage();
                                                msg.what = DOWNLOAD_IMAGE;
                                                msg.obj = "success";
                                                handler.sendMessage(msg);
                                            }
                                        }.start();
                                    }

                                    @Override
                                    public void onFailed() {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                Message msg = handler.obtainMessage();
                                                msg.what = DOWNLOAD_IMAGE;
                                                msg.obj = "fail";
                                                handler.sendMessage(msg);
                                            }
                                        }.start();
                                    }
                                }
                        );
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                String result = DataProvider.getAccept(myApp.getUser().getId(), record_time, actual_time);
                                Message msg = handler.obtainMessage();
                                msg.what = GET_ACCEPT;
                                msg.obj = result;
                                handler.sendMessage(msg);
                            }
                        }.start();
                    }
                }
            }
    }

    private void parseGetAcceptResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createToast(LoginActivity.this, result);
            } else {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("status") == 1) {
                        if(resultObject.has("record") && !resultObject.isNull("record")) {
                            JSONObject recordObject = resultObject.getJSONObject("record");
                            if(recordObject.getString("table_name").equals("Record")) {
                                if(recordObject.has("value") && !recordObject.isNull("value")) {
                                    JSONArray valueArray = recordObject.getJSONArray("value");
                                    for (int i = 0; i < valueArray.length(); i++) {
                                        JSONObject valueObject = valueArray.getJSONObject(i);
                                        ContentValues cv = new ContentValues();
                                        cv.put("project_id", valueObject.getString("project_id"));
                                        cv.put("area_id", valueObject.getString("area_id"));
                                        cv.put("classify_id", valueObject.getString("classify_id"));
                                        cv.put("task_id", valueObject.getString("task_id"));
                                        cv.put("create_time", valueObject.getString("create_time"));
                                        cv.put("user_id", valueObject.getString("user_id"));
                                        cv.put("submit", valueObject.getString("submit"));
                                        cv.put("store_path", valueObject.getString("store_path"));
                                        cv.put("backend_id", valueObject.getString("id"));
                                        db.insert("qu_record", null, cv);
                                    }
                                }
                            }
                        }
                        if(resultObject.has("group") && !resultObject.isNull("group")) {
                            JSONObject groupObject = resultObject.getJSONObject("group");
                            if(groupObject.getString("table_name").equals("ActualSituation")) {
                                if(groupObject.has("value") && !groupObject.isNull("value")) {
                                    JSONArray valueArray = groupObject.getJSONArray("value");
                                    List<PathUrl> urls = new ArrayList<>();
                                    for (int i = 0; i < valueArray.length(); i++) {
                                        JSONObject valueObject = valueArray.getJSONObject(i);
                                        Cursor recordCursor = db.rawQuery("select * from qu_record where backend_id=?", new String[]{valueObject.getString("record_id")});
                                        ContentValues cv = new ContentValues();
                                        while (recordCursor.moveToNext()) {
                                            cv.put("record_id", recordCursor.getString(recordCursor.getColumnIndex("id")));
                                            cv.put("description", valueObject.getString("description"));
                                            cv.put("url", valueObject.getString("url"));
                                            cv.put("score", valueObject.getString("score"));
                                            cv.put("submit", valueObject.getString("submit"));
                                            cv.put("create_time", valueObject.getString("create_time"));
                                            cv.put("local_url", valueObject.getString("local_url"));
                                            cv.put("backend_id", valueObject.getString("id"));
                                            cv.put("accept_id", valueObject.getString("accept_id"));
                                            db.insert("qu_actual_situation", null, cv);
                                            boolean exist = false;
                                            for (int j = 0; j < urls.size(); j++) {
                                                if(urls.get(j).equals(valueObject.getString("url"))) {
                                                    exist = true;
                                                }
                                            }
                                            if(!exist) {
                                                urls.add(new PathUrl(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/AcceptanceRecord", valueObject.optString("url"), valueObject.optString("local_url")));
                                            }
                                        }
                                    }
                                    for (int i = 0; i < urls.size(); i++) {
                                        System.out.println(urls.get(i));
                                    }
                                    if(urls.size() > 0) {
                                        CustomViewUtil.createProgressDialog(LoginActivity.this, "从后台同步数据中...");
                                        ImageDownloader.getInstance().startDownload(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/AcceptanceRecord", urls,
                                                new ImageDownloader.DownloadStateListener() {

                                                    @Override
                                                    public void onFinish() {
                                                        new Thread() {
                                                            @Override
                                                            public void run() {
                                                                Message msg = handler.obtainMessage();
                                                                msg.what = DOWNLOAD_IMAGE_ACCEPT;
                                                                msg.obj = "success";
                                                                handler.sendMessage(msg);
                                                            }
                                                        }.start();
                                                    }

                                                    @Override
                                                    public void onFailed() {
                                                        new Thread() {
                                                            @Override
                                                            public void run() {
                                                                Message msg = handler.obtainMessage();
                                                                msg.what = DOWNLOAD_IMAGE_ACCEPT;
                                                                msg.obj = "fail";
                                                                handler.sendMessage(msg);
                                                            }
                                                        }.start();
                                                    }
                                                }
                                        );
                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, ProjectListActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        }
                    } else {
                        Intent intent = new Intent(LoginActivity.this, ProjectListActivity.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(db != null) {
                        db.close();
                    }
                }
            }
        }
    }

}
