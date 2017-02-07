package com.zhihuitech.qualitymanagementsystem;

import android.app.*;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.*;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.zhihuitech.qualitymanagementsystem.adapter.ProjectListAdapter;
import com.zhihuitech.qualitymanagementsystem.entity.*;
import com.zhihuitech.qualitymanagementsystem.util.CustomViewUtil;
import com.zhihuitech.qualitymanagementsystem.util.DataProvider;
import com.zhihuitech.qualitymanagementsystem.util.HttpUtil;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseContext;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseHelper;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ProjectListActivity extends Activity {
    private TextView tvUsername;
    private LinearLayout llProjectList;
    private TextView tvNoProject;

    private ViewPager viewPager;
    private List<View> viewList =  new ArrayList<View>();
    // 总页数
    private int pageCount;
    // 底部小横线
    private ViewGroup lineGroup;
    private ImageView[] lines;

    private List<Project> allProjectList;
    private List<List<Project>> projectList = new ArrayList<>();

    // 处理数据库所需的对象
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private MyApplication myApp;

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "pzglxt";
    private String uploadData = "";
    private JSONArray uploadDataArray = null;
    private List<ID_URL> idURLList = new ArrayList<>();
    private int uploadImgIndex = 0;

    private final static int IMG_YUN = 1;
    private final static int SYNC_DATA = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IMG_YUN:
                    parseImgYunResult((String)msg.obj);
                    break;
                case SYNC_DATA:
                    CustomViewUtil.dismissDialog();
                    parseSyncDataResult((String)msg.obj);
                    break;
            }
        }
    };

    private void parseSyncDataResult(String result) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            if(result != null && !result.equals("")) {
                if(result.contains("超时")) {
                    CustomViewUtil.createToast(ProjectListActivity.this, result);
                } else {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.has("status")) {
                        if(resultObject.getInt("status") == 1) {
                            sendNotification("数据同步成功！");
                            if(resultObject.has("res") && !resultObject.isNull("res")) {
                                JSONArray resArray = resultObject.getJSONArray("res");
                                for (int i = 0; i < resArray.length(); i++) {
                                    JSONObject obj = resArray.getJSONObject(i);
                                    JSONObject valueObject = obj.getJSONObject("value");
                                    ContentValues cv = new ContentValues();
                                    cv.put("backend_id", valueObject.getString("id"));
                                    if(obj.getString("table_name").equals("Record")) {
                                        db.update("qu_record", cv, "id=?", new String[]{valueObject.getString("pad_id")});
                                    } else if(obj.getString("table_name").equals("ActualSituation")) {
                                        db.update("qu_actual_situation", cv, "id=?", new String[]{valueObject.getString("pad_id")});
                                    }
                                }
                            }
                        } else {
                            restoreInfoToPref();
                            sendNotification("数据同步失败！");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(db != null) {
                db.close();
            }
        }
    }

    private void parseImgYunResult(String result) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            if (result != null && !result.equals("")) {
                if (result.contains("超时")) {
                    CustomViewUtil.dismissDialog();
                    CustomViewUtil.createToast(ProjectListActivity.this, result);
                    restoreInfoToPref();
                } else {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("status") == 1) {
                        for (int i = 0; i < uploadDataArray.length(); i++) {
                            JSONObject obj = uploadDataArray.getJSONObject(i);
                            if(obj.getString("table_name").equals("ActualSituation")) {
                                JSONObject dataObject = obj.getJSONObject("data");
                                if(dataObject.getInt("pad_id") == idURLList.get(uploadImgIndex).getId()) {
                                    dataObject.put("url", resultObject.getString("imgurlYun"));
                                    ContentValues cv = new ContentValues();
                                    cv.put("url", resultObject.getString("imgurlYun"));
                                    db.update("qu_actual_situation", cv, "id = ?", new String[]{dataObject.getString("pad_id")});
                                    uploadImgIndex++;
                                    break;
                                }
                            }
                        }
                        if(uploadImgIndex <= idURLList.size() - 1) {
                            new Thread() {
                                @Override
                                public void run() {
                                    String result = DataProvider.imgYun(idURLList.get(uploadImgIndex).getUrl());
                                    Message msg = handler.obtainMessage();
                                    msg.obj = result;
                                    msg.what = IMG_YUN;
                                    handler.sendMessage(msg);
                                }
                            }.start();
                        } else if(uploadImgIndex > idURLList.size() - 1) {
                            new Thread() {
                                @Override
                                public void run() {
                                    String result = DataProvider.syncData(uploadDataArray.toString());
                                    Message msg = handler.obtainMessage();
                                    msg.obj = result;
                                    msg.what = SYNC_DATA;
                                    handler.sendMessage(msg);
                                }
                            }.start();
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(db != null) {
                db.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_list);

        myApp = (MyApplication) getApplication();
        findViews();
        initData();
        getInfoFromPref();
        EventBus.getDefault().register(this);
        setSyncAlarmManager();

        CustomViewUtil.createToast(ProjectListActivity.this, "登录成功");
    }

    private void getInfoFromPref() {
        try {
            if(HttpUtil.checkConnection(ProjectListActivity.this)) {
                pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                // 获取需要同步的数据
                uploadData = pref.getString("data", "[]");
                uploadDataArray = new JSONArray(uploadData);
                System.out.println("uploadData=" + uploadData);
                syncData();
                // 重置data
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("data", "[]");
                edit.commit();
                JSONArray dataArray = new JSONArray("[]");
                myApp.setDataArray(dataArray);
            } else {
                pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                // 获取需要同步的数据
                uploadData = pref.getString("data", "[]");
                JSONArray dataArray = new JSONArray(uploadData);
                myApp.setDataArray(dataArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String result) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Notification notify = new Notification.Builder(this)
                .setSmallIcon(R.drawable.logo) // 设置状态栏中的小图片，尺寸一般建议在24×24， 这里也可以设置大图标
                .setTicker("品质管理系统提示")// 设置显示的提示文字
                        .setContentTitle("提示")// 设置显示的标题
                        .setContentText(result)// 消息的详细内容
                        .setContentIntent(pendingIntent) // 关联PendingIntent
                        .getNotification(); // 需要注意build()是在API level16及之后增加的，在API11中可以使用getNotification()来代替
        notify.defaults = Notification.DEFAULT_SOUND;
        notify.audioStreamType = AudioManager.ADJUST_LOWER;
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notify);
    }

    private void restoreInfoToPref() {
        try {
            JSONArray uploadDataArray = new JSONArray(uploadData);
            JSONArray tempArray = new JSONArray("[]");
            for(int i = 0; i < myApp.getDataArray().length(); i++) {
                tempArray.put(myApp.getDataArray().getJSONObject(i));
            }
            myApp.setDataArray(new JSONArray("[]"));
            for(int i = 0; i < uploadDataArray.length(); i++) {
                myApp.getDataArray().put(uploadDataArray.getJSONObject(i));
            }
            for(int i = 0; i < tempArray.length(); i++) {
                myApp.getDataArray().put(tempArray.getJSONObject(i));
            }
            myApp.saveDataToPref();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void syncData() {
        if(uploadDataArray.length() == 0) {
            return;
        } else {
            try {
                idURLList.clear();
                for (int i = 0; i < uploadDataArray.length(); i++) {
                    JSONObject obj = uploadDataArray.getJSONObject(i);
                    if(obj.getString("table_name").equals("ActualSituation")) {
                        JSONObject dataObject = obj.getJSONObject("data");
                        idURLList.add(new ID_URL(dataObject.getInt("pad_id"), dataObject.getString("local_url")));
                    }
                }
                CustomViewUtil.createProgressDialog(ProjectListActivity.this, "正在同步本地数据到云端，请稍后...");
                new Thread() {
                    @Override
                    public void run() {
                        String result = DataProvider.imgYun(idURLList.get(uploadImgIndex).getUrl());
                        Message msg = handler.obtainMessage();
                        msg.obj = result;
                        msg.what = IMG_YUN;
                        handler.sendMessage(msg);
                    }
                }.start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        new Thread() {
//            @Override
//            public void run() {
//                String result = DataProvider.syncData(uploadData);
//                Message msg = handler.obtainMessage();
//                msg.obj = result;
//                msg.what = SYNC_DATA;
//                handler.sendMessage(msg);
//            }
//        }.start();
    }

    private void setSyncAlarmManager() {
        Intent intent = new Intent(ProjectListActivity.this, SyncService.class);
        PendingIntent sender = PendingIntent.getService(ProjectListActivity.this, 0, intent, 0);
        // 进行闹铃注册
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000L * 60 * 60 , sender);
    }

    @Subscribe
    public void onEventMainThread(SyncEvent event) {
        System.out.println("onEventMainThread.开始同步");
        // 如果网络已连接
        if(HttpUtil.checkConnection(ProjectListActivity.this)) {
//            syncData();
        }
    }

    private void initData() {
        tvUsername.setText(myApp.getUser() == null ? "未知" : myApp.getUser().getUsername());
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from qu_project", null);
        if(cursor.getCount() == 0) {
            llProjectList.setVisibility(View.GONE);
            tvNoProject.setVisibility(View.VISIBLE);
        } else {
            llProjectList.setVisibility(View.VISIBLE);
            tvNoProject.setVisibility(View.GONE);
            allProjectList = new ArrayList<>();
            while (cursor.moveToNext()) {
                Project project = new Project();
                project.setId(cursor.getString(cursor.getColumnIndex("id")));
                project.setName(cursor.getString(cursor.getColumnIndex("project_name")));
                project.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                project.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                project.setLocal_url(cursor.getString(cursor.getColumnIndex("local_url")));
                allProjectList.add(project);
            }
            calculatePageCount();
            showContent();
        }
    }

    private void calculatePageCount() {
        if(allProjectList.size() <= 12) {
            pageCount = 1;
            List<Project> showList = new ArrayList<>();
            showList.addAll(allProjectList);
            projectList.add(showList);
        } else {
            if(allProjectList.size() % 12 == 0) {
                pageCount = allProjectList.size() / 12;
                List<Project> showList = null;
                for(int i = 0; i < pageCount; i++) {
                    showList = new ArrayList<>();
                    for(int j = 0; j < 12; j++) {
                        showList.add(allProjectList.get(j + 12 * i));
                    }
                    projectList.add(showList);
                }
            } else {
                pageCount = allProjectList.size() / 12 + 1;
                List<Project> showList = null;
                for(int i = 0; i < pageCount - 1; i++) {
                    showList = new ArrayList<>();
                    for(int j = 0; j < 12; j++) {
                        showList.add(allProjectList.get(j + 12 * i));
                    }
                    projectList.add(showList);
                }
                showList = new ArrayList<>();
                for(int i = allProjectList.size() - allProjectList.size() % 12; i < allProjectList.size(); i++) {
                    showList.add(allProjectList.get(i));
                }
                projectList.add(showList);
            }
        }
    }

    private void showContent() {
        if(pageCount > 1) {
            initLines();
            lineGroup.setVisibility(View.VISIBLE);
        } else {
            lineGroup.setVisibility(View.GONE);
        }
        for(int i = 0; i < pageCount; i++) {
            final int index = i;
            View view = getLayoutInflater().inflate(R.layout.project_list_item, null);
            GridView gv = (GridView) view.findViewById(R.id.gv_project_list_item);
            ProjectListAdapter adapter = new ProjectListAdapter(ProjectListActivity.this, projectList.get(i));
            gv.setAdapter(adapter);
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(ProjectListActivity.this, ChooseTaskActivity.class);
                    intent.putExtra("project", projectList.get(index).get(position));
                    startActivity(intent);
                }
            });
            viewList.add(view);
        }
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    class MyPagerAdapter extends PagerAdapter {
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
        Object object) {
            container.removeView(viewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewList.get(position));
            return viewList.get(position);
        }
    }

    private void findViews() {
        tvUsername = (TextView) findViewById(R.id.tv_username_project_list);
        llProjectList = (LinearLayout) findViewById(R.id.ll_project_list_project_list);
        tvNoProject = (TextView) findViewById(R.id.tv_no_project_tip_project_list);
        viewPager = (ViewPager) findViewById(R.id.vp_project_list);
        lineGroup =(ViewGroup) findViewById(R.id.ll_line_project_list);
    }

    private void initLines() {
        lines = new ImageView[pageCount];
        for (int i = 0; i < pageCount; i++) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams;
            layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(100, 5));
            layoutParams.leftMargin = 20;
            layoutParams.rightMargin = 20;
            imageView.setLayoutParams(layoutParams);
            lines[i] = imageView;
            if (i == 0) {
                lines[i].setBackgroundResource(R.drawable.project_dot_selected);
            } else {
                lines[i].setBackgroundResource(R.drawable.project_dot_unselected);
            }
            lineGroup.addView(lines[i]);
        }
    }

    /**
     * ViewPager的监听器
     */
    class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            for(int i = 0; i < lines.length; i++){
                if(i == position){
                    lines[i].setBackgroundResource(R.drawable.project_dot_selected);
                }else{
                    lines[i].setBackgroundResource(R.drawable.project_dot_unselected);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    class ID_URL {
        private int id;
        private String url;

        public ID_URL(int id, String url) {
            this.id = id;
            this.url = url;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
