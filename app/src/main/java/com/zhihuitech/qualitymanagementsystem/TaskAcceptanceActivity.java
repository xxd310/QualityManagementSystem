package com.zhihuitech.qualitymanagementsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.zhihuitech.qualitymanagementsystem.adapter.RecordListAdapter;
import com.zhihuitech.qualitymanagementsystem.entity.*;
import com.zhihuitech.qualitymanagementsystem.util.CustomViewUtil;
import com.zhihuitech.qualitymanagementsystem.util.PinchImageView;
import com.zhihuitech.qualitymanagementsystem.util.XCRoundRectImageView;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseContext;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class TaskAcceptanceActivity extends Activity {
    private TextView tvUsername;
    private TextView tvProjectName;
    private TextView tvPath;
    private ViewPager viewPager;
    private List<View> viewList =  new ArrayList<>();
    private List<ActualAcceptanceSituation> actualAcceptanceSituationList = new ArrayList<>();

    // 底部小圆点
    private ViewGroup dotGroup;
    private ImageView[] dots;
    private List<AcceptanceStandard> acceptStandardList = new ArrayList<>();
    private AcceptanceStandard currentAcceptanceStandard;
    private int currentAcceptanceStandardIndex = -1;

    private ListView lvRecord;
    private List<Record> recordList = new ArrayList<>();
    private RecordListAdapter recordAdapter;
    private Record currentRecord;
    private int currentRecordIndex = -1;

    private ImageView ivAdd;
    private TextView tvStandardContent;
    private TextView tvAcceptName;

    private LinearLayout llTakePhoto;
    private ImageView ivActualPhoto;
    private ImageView ivTakePhoto;
    private ImageView ivPlay;

    private LinearLayout llAccept;
    private EditText etAcceptanceInstruction;
    private Button btnSubmit;

    private RatingBar rbScore;
    private LinearLayout llAcceptResult;
    private TextView tvAcceptanceInstruction;
    private TextView tvAccepter;
    private TextView tvScore;

    private File imageFile;
    private String imageFilePath;
    private File videoFile;

    // 处理数据库所需的对象
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private Project project;
    private Area area;
    private Classify classify;
    private Task task;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private MyApplication myApp;
    private int screenWidth;
    private int screenHeight;
    private long lastAddTime = 0;
    private String uuid;

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "pzglxt";

    private JSONArray tempArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_acceptance);

        myApp = (MyApplication) getApplication();
        try {
            tempArray = new JSONArray("[]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        getMyUUID();
        getScreenInfo();
        findViews();
        addListeners();
        initData();
    }

    private void getScreenInfo() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
    }

    private void getMyUUID(){
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(this.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        uuid = deviceUuid.toString();
        System.out.println("UUID=" + uuid);
    }

    private void initData() {
        tvUsername.setText(myApp.getUser() == null ? "未知" : myApp.getUser().getUsername());
        Intent intent = getIntent();
        project = intent.hasExtra("project") ? (Project) intent.getSerializableExtra("project") : null;
        area = intent.hasExtra("area") ? (Area) intent.getSerializableExtra("area") : null;
        classify = intent.hasExtra("classify") ? (Classify) intent.getSerializableExtra("classify") : null;
        task = intent.hasExtra("task") ? (Task) intent.getSerializableExtra("task") : null;
        if(project == null || area == null || classify == null || task == null) {
            return;
        }
        tvProjectName.setText(project.getName());
        tvPath.setText("位置：" + area.getName() + "-" + classify.getName() + "-" + task.getName());

        recordAdapter = new RecordListAdapter(TaskAcceptanceActivity.this, recordList);
        lvRecord.setAdapter(recordAdapter);

        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // 删除之前还未提交成功的记录
            Cursor cc = db.rawQuery("select * from qu_record where submit=0", null);
            while (cc.moveToNext()) {
                db.delete("qu_actual_situation", "record_id=?", new String[]{cc.getString(cc.getColumnIndex("id"))});
            }
            db.delete("qu_record", "submit=0", null);

            // 获取验收标准
            Cursor acceptanceStandardCursor = db.rawQuery("select * from qu_acceptance_standard where project_id=? and area_id=? and classify_id=? and task_id=?", new String[]{project.getId(), area.getId(), classify.getId(), task.getId()});
            while (acceptanceStandardCursor.moveToNext()) {
                AcceptanceStandard as = new AcceptanceStandard();
                as.setId(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("id")));
                as.setProject_id(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("project_id")));
                as.setArea_id(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("area_id")));
                as.setClassify_id(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("classify_id")));
                as.setTask_id(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("task_id")));
                as.setStandard(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("standard")));
                as.setAccept_name(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("accept_name")));
                as.setUrl(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("url")));
                as.setLocal_url(acceptanceStandardCursor.getString(acceptanceStandardCursor.getColumnIndex("local_url")));
                acceptStandardList.add(as);
            }
            // 获取记录信息
            Cursor recordCursor = db.rawQuery("select r.*,u.username from qu_record r LEFT OUTER JOIN qu_user u on r.user_id=u.id where r.project_id=? and r.area_id=? and r.classify_id=? and r.task_id=? order by r.create_time desc", new String[]{project.getId(), area.getId(), classify.getId(), task.getId()});
            if(recordCursor.getCount() > 0) {
                while (recordCursor.moveToNext()) {
                    Record record = new Record();
                    record.setId(recordCursor.getString(recordCursor.getColumnIndex("id")));
                    record.setProject_id(recordCursor.getString(recordCursor.getColumnIndex("project_id")));
                    record.setArea_id(recordCursor.getString(recordCursor.getColumnIndex("area_id")));
                    record.setClassify_id(recordCursor.getString(recordCursor.getColumnIndex("classify_id")));
                    record.setTask_id(recordCursor.getString(recordCursor.getColumnIndex("task_id")));
                    record.setCreate_time(recordCursor.getString(recordCursor.getColumnIndex("create_time")));
                    record.setFormat_time(sdf.format(new Date(recordCursor.getLong(recordCursor.getColumnIndex("create_time")) * 1000)));
                    record.setSelected(false);
                    record.setDirectoryPath(recordCursor.getString(recordCursor.getColumnIndex("store_path")));
                    record.setSubmit(recordCursor.getString(recordCursor.getColumnIndex("submit")));
                    record.setUsername(recordCursor.getString(recordCursor.getColumnIndex("username")));
                    recordList.add(record);
                }
                recordList.get(0).setSelected(true);
                currentRecord = recordList.get(0);
                recordAdapter.notifyDataSetChanged();
            }
            // 如果当前没有验收记录，新建空的验收内容
            if(currentRecord == null) {
                for (int i = 0; i < acceptStandardList.size(); i++) {
                    ActualAcceptanceSituation aas = new ActualAcceptanceSituation();
                    aas.setRecord_id("");
                    aas.setDescription("");
                    aas.setScore("");
                    aas.setSubmit("0");
                    aas.setUrl("");
                    aas.setCreate_time(System.currentTimeMillis() / 1000 + "");
                    aas.setLocal_url("");
                    aas.setAccept_id(acceptStandardList.get(i).getId());
                    actualAcceptanceSituationList.add(aas);
                }
            } else {
                // 根据验收记录的id获取实际验收内容
                Cursor actualAcceptanceSituationCursor = db.rawQuery("select * from qu_actual_situation where record_id=?", new String[]{currentRecord.getId()});
                if(actualAcceptanceSituationCursor.getCount() > 0) {
                    while (actualAcceptanceSituationCursor.moveToNext()) {
                        ActualAcceptanceSituation aas = new ActualAcceptanceSituation();
                        aas.setId(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("id")));
                        aas.setRecord_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("record_id")));
                        aas.setDescription(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("description")));
                        aas.setScore(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("score")));
                        aas.setSubmit(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("submit")));
                        aas.setUrl(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("url")));
                        aas.setCreate_time(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("create_time")));
                        aas.setLocal_url(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("local_url")));
                        aas.setAccept_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("accept_id")));
                        actualAcceptanceSituationList.add(aas);
                    }
                    if(actualAcceptanceSituationList.get(0).getLocal_url().equals("")) {
                        llTakePhoto.setVisibility(View.VISIBLE);
                        ivActualPhoto.setVisibility(View.GONE);
                    } else {
                        llTakePhoto.setVisibility(View.GONE);
                        ivActualPhoto.setVisibility(View.VISIBLE);
                        if(actualAcceptanceSituationList.get(0).getLocal_url().endsWith(".mp4")) {
                            ivPlay.setVisibility(View.VISIBLE);
                            ivActualPhoto.setImageBitmap(getVideoThumbnail(actualAcceptanceSituationList.get(0).getLocal_url()));
                            ivPlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(TaskAcceptanceActivity.this, PlayVideoActivity.class);
                                    intent.putExtra("url", actualAcceptanceSituationList.get(0).getLocal_url());
                                    startActivity(intent);
                                }
                            });
                        } else {
                            ivPlay.setVisibility(View.GONE);
                            Glide.with(this).load(new File(actualAcceptanceSituationList.get(0).getLocal_url())).dontAnimate().into(ivActualPhoto);
                            ivActualPhoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showLocalBigPicDialog(actualAcceptanceSituationList.get(0).getLocal_url());
                                }
                            });
                        }
                    }
                    llAccept.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.GONE : View.VISIBLE);
                    llAcceptResult.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.VISIBLE : View.GONE);
                    tvAcceptanceInstruction.setText(actualAcceptanceSituationList.get(0).getDescription());
                    tvAccepter.setText(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? currentRecord.getUsername() : "");
                    tvScore.setText("(" + actualAcceptanceSituationList.get(0).getScore() + "分)");
                }
            }
            etAcceptanceInstruction.setEnabled(currentRecord == null ? false : true);
//            rbScore.setEnabled(currentRecord == null ? false : true);
//            btnSubmit.setEnabled(currentRecord == null ? false : true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        initDots();
        for(int i = 0; i < acceptStandardList.size(); i++) {
            final int index = i;
            XCRoundRectImageView iv = new XCRoundRectImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            iv.setLayoutParams(layoutParams);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource" + acceptStandardList.get(i).getLocal_url().substring(acceptStandardList.get(i).getLocal_url().lastIndexOf("/")));
            if(bm == null) {
                iv.setImageResource(R.drawable.logo);
            } else {
                iv.setImageBitmap(bm);
            }
            viewList.add(iv);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showBigPicDialog(acceptStandardList.get(index).getLocal_url());
                }
            });
        }
        currentAcceptanceStandard = acceptStandardList.get(0);
        currentAcceptanceStandardIndex = 0;
        tvStandardContent.setText(currentAcceptanceStandard.getStandard());
        tvAcceptName.setText("(" + currentAcceptanceStandard.getAccept_name() + ")");
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    private void showImageVideoSelectDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TaskAcceptanceActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.image_video_select_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int) (0.3 * screenWidth), (int) (0.15 * screenHeight));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        TextView tvTakePhoto = (TextView) view.findViewById(R.id.tv_take_photo_image_video_select_dialog);
        TextView tvTakeVideo = (TextView) view.findViewById(R.id.tv_take_video_image_video_select_dialog);
        tvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                takePhoto();
            }
        });
        tvTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                takeVideo();
            }
        });
    }

    private void showLocalBigPicDialog(String url) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TaskAcceptanceActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.big_pic_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int) (0.8 * screenWidth), (int) (0.8 * screenHeight));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        PinchImageView piv = (PinchImageView) view.findViewById(R.id.iv_big_pic_dialog);
        Glide.with(this).load(new File(url)).dontAnimate().into(piv);
        piv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void showBigPicDialog(String url) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TaskAcceptanceActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.big_pic_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int) (0.8 * screenWidth), (int) (0.8 * screenHeight));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        PinchImageView piv = (PinchImageView) view.findViewById(R.id.iv_big_pic_dialog);
//        Glide.with(this).load(new File(url)).dontAnimate().into(piv);
        Glide.with(this).load(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/Resource" + url.substring(url.lastIndexOf("/")))).dontAnimate().into(piv);
        piv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void addListeners() {
        tvProjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recordList.size() > 0) {
                    for (int i = 0; i < actualAcceptanceSituationList.size(); i++) {
                        if(actualAcceptanceSituationList.get(i).getSubmit().equals("0")) {
                            CustomViewUtil.createToast(TaskAcceptanceActivity.this, "尚有未提交的验收内容！");
                            return;
                        }
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("task", task);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        lvRecord.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Record deleteRecord = recordList.get(i);
//                showDeleteDialog(deleteRecord, i);
                return true;
            }
        });
        lvRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // 如果点击的是当前所选项，不处理
                if(recordList.get(position).isSelected()) {
                    return;
                }
                for (int i = 0; i < actualAcceptanceSituationList.size(); i++) {
                    if(actualAcceptanceSituationList.get(i).getSubmit().equals("0")) {
                        CustomViewUtil.createToast(TaskAcceptanceActivity.this, "尚有未提交的验收内容！");
                        return;
                    }
                }
                currentAcceptanceStandardIndex = 0;
                viewPager.setCurrentItem(currentAcceptanceStandardIndex);
                for (int i = 0; i < recordList.size(); i++) {
                    if(i == position) {
                        recordList.get(i).setSelected(true);
                    } else {
                        recordList.get(i).setSelected(false);
                    }
                }
                recordAdapter.notifyDataSetChanged();
                currentRecordIndex = position;
                currentRecord = recordList.get(position);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor actualAcceptanceSituationCursor = db.rawQuery("select * from qu_actual_situation where record_id=?", new String[]{currentRecord.getId()});
                actualAcceptanceSituationList.clear();
                if(actualAcceptanceSituationCursor.getCount() > 0) {
                    while (actualAcceptanceSituationCursor.moveToNext()) {
                        ActualAcceptanceSituation aas = new ActualAcceptanceSituation();
                        aas.setId(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("id")));
                        aas.setRecord_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("record_id")));
                        aas.setDescription(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("description")));
                        aas.setScore(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("score")));
                        aas.setSubmit(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("submit")));
                        aas.setUrl(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("url")));
                        aas.setCreate_time(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("create_time")));
                        aas.setLocal_url(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("local_url")));
                        aas.setAccept_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("accept_id")));
                        actualAcceptanceSituationList.add(aas);
                    }
                    if(actualAcceptanceSituationList.get(0).getLocal_url().equals("")) {
                        llTakePhoto.setVisibility(View.VISIBLE);
                        ivActualPhoto.setVisibility(View.GONE);
                    } else {
                        llTakePhoto.setVisibility(View.GONE);
                        ivActualPhoto.setVisibility(View.VISIBLE);
                        if(actualAcceptanceSituationList.get(0).getLocal_url().endsWith(".mp4")) {
                            ivPlay.setVisibility(View.VISIBLE);
                            ivActualPhoto.setImageBitmap(getVideoThumbnail(actualAcceptanceSituationList.get(0).getLocal_url()));
                            ivPlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(TaskAcceptanceActivity.this, PlayVideoActivity.class);
                                    intent.putExtra("url", actualAcceptanceSituationList.get(0).getLocal_url());
                                    startActivity(intent);
                                }
                            });
                        } else {
                            ivPlay.setVisibility(View.GONE);
                            Glide.with(TaskAcceptanceActivity.this).load(new File(actualAcceptanceSituationList.get(0).getLocal_url())).dontAnimate().into(ivActualPhoto);
                            ivActualPhoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showLocalBigPicDialog(actualAcceptanceSituationList.get(0).getLocal_url());
                                }
                            });
                        }
                    }
                    llAccept.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.GONE : View.VISIBLE);
                    llAcceptResult.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.VISIBLE : View.GONE);
                    tvAcceptanceInstruction.setText(actualAcceptanceSituationList.get(0).getDescription());
                    tvAccepter.setText(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? currentRecord.getUsername() : "");
                    tvScore.setText("(" + actualAcceptanceSituationList.get(0).getScore() + "分)");
                }
            }
        });
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(task.getFinish().equals("1")) {
//                    CustomViewUtil.createToast(TaskAcceptanceActivity.this, "您已完成今天的验收任务，无需再次新建！");
//                    return;
//                }
                if(System.currentTimeMillis() - lastAddTime < 1000) {
                    CustomViewUtil.createToast(TaskAcceptanceActivity.this, "操作太快！");
                    return;
                }
                if(recordList.size() > 0) {
                    for (int i = 0; i < actualAcceptanceSituationList.size(); i++) {
                        if(actualAcceptanceSituationList.get(i).getSubmit().equals("0")) {
                            CustomViewUtil.createToast(TaskAcceptanceActivity.this, "尚有未提交的验收内容！");
                            return;
                        }
                    }
                }
                try {
                    tempArray = new JSONArray("[]");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                lastAddTime = System.currentTimeMillis();
                currentAcceptanceStandardIndex = 0;
                viewPager.setCurrentItem(currentAcceptanceStandardIndex);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                try {
                    ContentValues cv = new ContentValues();
                    cv.put("project_id", project.getId());
                    cv.put("area_id", area.getId());
                    cv.put("classify_id", classify.getId());
                    cv.put("task_id", task.getId());
                    cv.put("create_time", System.currentTimeMillis() / 1000);
                    cv.put("user_id", myApp.getUser().getId());
                    cv.put("submit", 0);
                    cv.put("store_path", Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/AcceptanceRecord");
                    db.insert("qu_record", null, cv);

                    Cursor recordCursor = db.rawQuery("select r.*,u.username from qu_record r LEFT OUTER JOIN qu_user u on r.user_id=u.id order by r.id desc limit 0,1", null);
                    currentRecord = new Record();
                    currentRecordIndex = 0;
                    while (recordCursor.moveToNext()) {
                        currentRecord.setId(recordCursor.getString(recordCursor.getColumnIndex("id")));
                        currentRecord.setProject_id(recordCursor.getString(recordCursor.getColumnIndex("project_id")));
                        currentRecord.setArea_id(recordCursor.getString(recordCursor.getColumnIndex("area_id")));
                        currentRecord.setClassify_id(recordCursor.getString(recordCursor.getColumnIndex("classify_id")));
                        currentRecord.setTask_id(recordCursor.getString(recordCursor.getColumnIndex("task_id")));
                        currentRecord.setCreate_time(recordCursor.getString(recordCursor.getColumnIndex("create_time")));
                        currentRecord.setFormat_time(sdf.format(new Date(recordCursor.getLong(recordCursor.getColumnIndex("create_time")) * 1000)));
                        currentRecord.setDirectoryPath(recordCursor.getString(recordCursor.getColumnIndex("store_path")));
                        currentRecord.setSelected(true);
                        currentRecord.setSubmit("0");
                        currentRecord.setUsername(recordCursor.getString(recordCursor.getColumnIndex("username")));

                        // 把数据存储到需要同步的对象中
                        JSONObject itemObject = new JSONObject();
                        itemObject.put("pad_id", recordCursor.getString(recordCursor.getColumnIndex("id")));
                        itemObject.put("project_id", recordCursor.getString(recordCursor.getColumnIndex("project_id")));
                        itemObject.put("area_id", recordCursor.getString(recordCursor.getColumnIndex("area_id")));
                        itemObject.put("classify_id", recordCursor.getString(recordCursor.getColumnIndex("classify_id")));
                        itemObject.put("task_id", recordCursor.getString(recordCursor.getColumnIndex("task_id")));
                        itemObject.put("create_time", recordCursor.getString(recordCursor.getColumnIndex("create_time")));
                        itemObject.put("user_id", recordCursor.getString(recordCursor.getColumnIndex("user_id")));
                        itemObject.put("store_path", recordCursor.getString(recordCursor.getColumnIndex("store_path")));
                        itemObject.put("submit", recordCursor.getString(recordCursor.getColumnIndex("submit")));
                        itemObject.put("device_id", uuid);
                        JSONObject obj = new JSONObject();
                        obj.put("operation", "insert");
                        obj.put("table_name", "Record");
                        obj.put("data", itemObject);
                        tempArray.put(obj);
//                        myApp.getDataArray().put(obj);
//                        myApp.saveDataToPref();
                    }
                    for (int i = 0; i < recordList.size(); i++) {
                        recordList.get(i).setSelected(false);
                    }
                    recordList.add(0, currentRecord);
                    recordAdapter.notifyDataSetChanged();
                    File file = new File(currentRecord.getDirectoryPath());
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    for (int i = 0; i < acceptStandardList.size(); i++) {
                        ContentValues ccv = new ContentValues();
                        ccv.put("record_id", currentRecord.getId());
                        ccv.put("description", "");
                        ccv.put("url", "");
                        ccv.put("score", "");
                        ccv.put("submit", 0);
                        ccv.put("create_time", System.currentTimeMillis() / 1000);
                        ccv.put("local_url", "");
                        ccv.put("accept_id", acceptStandardList.get(i).getId());
                        db.insert("qu_actual_situation", null, ccv);
                    }
                    Cursor actualAcceptanceSituationCursor = db.rawQuery("select * from qu_actual_situation where record_id=?", new String[]{currentRecord.getId()});
                    actualAcceptanceSituationList.clear();
                    if(actualAcceptanceSituationCursor.getCount() > 0) {
                        while (actualAcceptanceSituationCursor.moveToNext()) {
                            ActualAcceptanceSituation aas = new ActualAcceptanceSituation();
                            aas.setId(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("id")));
                            aas.setRecord_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("record_id")));
                            aas.setDescription(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("description")));
                            aas.setScore(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("score")));
                            aas.setSubmit(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("submit")));
                            aas.setUrl(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("url")));
                            aas.setCreate_time(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("create_time")));
                            aas.setLocal_url(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("local_url")));
                            aas.setAccept_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("accept_id")));
                            actualAcceptanceSituationList.add(aas);

                            // 把数据存储到需要同步的对象中
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("pad_id", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("id")));
                            itemObject.put("record_id", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("record_id")));
                            itemObject.put("description", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("description")));
                            itemObject.put("url", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("url")));
                            itemObject.put("score", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("score")));
                            itemObject.put("submit", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("submit")));
                            itemObject.put("create_time", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("create_time")));
                            itemObject.put("local_url", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("local_url")));
                            itemObject.put("accept_id", actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("accept_id")));
                            itemObject.put("user_id", myApp.getUser().getId());
                            itemObject.put("device_id", uuid);
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "insert");
                            obj.put("table_name", "ActualSituation");
                            obj.put("data", itemObject);
                            tempArray.put(obj);
//                            myApp.getDataArray().put(obj);
//                            myApp.saveDataToPref();
                        }
                    }
                    llTakePhoto.setVisibility(View.VISIBLE);
                    ivActualPhoto.setVisibility(View.GONE);
                    etAcceptanceInstruction.setText("");
                    rbScore.setRating(0);
                    llAccept.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.GONE : View.VISIBLE);
                    llAcceptResult.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.VISIBLE : View.GONE);
                    etAcceptanceInstruction.setEnabled(currentRecord == null ? false : true);
//                    rbScore.setEnabled(currentRecord == null ? false : true);
//                    btnSubmit.setEnabled(currentRecord == null ? false : true);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                }

            }
        });
        ivTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentRecord != null) {
//                    showImageVideoSelectDialog();
                    takePhoto();
                } else {
                    CustomViewUtil.createToast(TaskAcceptanceActivity.this, "请先点击左下角按钮新建验收记录！");
                }
            }
        });
        rbScore.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

            }
        });
        ivActualPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recordList.size() == 0) {
                    CustomViewUtil.createToast(TaskAcceptanceActivity.this, "请先点击左下角按钮新建验收记录！");
                    return;
                }
                // 如果用户尚未拍照，提示用户
                if(!ivActualPhoto.isShown()) {
                    CustomViewUtil.createToast(TaskAcceptanceActivity.this, "请先拍照！");
                    return;
                }
                // 如果用户尚未输入验收说明，提示用户
                if(etAcceptanceInstruction.getText().toString().equals("")) {
                    CustomViewUtil.createToast(TaskAcceptanceActivity.this, "请输入验收说明！");
                    return;
                }
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                try {
                    ContentValues cv = new ContentValues();
                    cv.put("description", etAcceptanceInstruction.getText().toString());
                    cv.put("local_url", actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getLocal_url());
                    cv.put("score", rbScore.getRating());
                    cv.put("create_time", System.currentTimeMillis() / 1000);
                    cv.put("submit", 1);
                    long count = db.update("qu_actual_situation", cv, "id=?", new String[]{actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getId()});
                    if(count > 0) {
                        Cursor acceptanceSituationCursor = db.rawQuery("select * from qu_actual_situation where id=?", new String[]{actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getId()});
                        while (acceptanceSituationCursor.moveToNext()) {
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setId(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("id")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setRecord_id(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("record_id")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setDescription(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("description")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setScore(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("score")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setUrl(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("url")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setSubmit(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("submit")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setCreate_time(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("create_time")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setLocal_url(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("local_url")));
                            actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setAccept_id(acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("accept_id")));

                            for(int i = 0; i < tempArray.length(); i++) {
                                JSONObject obj = tempArray.getJSONObject(i);
                                if(obj.getString("table_name").equals("ActualSituation")) {
                                    JSONObject dataObj = obj.getJSONObject("data");
                                    if(dataObj.getInt("pad_id") == acceptanceSituationCursor.getInt(acceptanceSituationCursor.getColumnIndex("id"))) {
                                        dataObj.put("description", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("description")));
                                        dataObj.put("local_url", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("local_url")));
                                        dataObj.put("score", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("score")));
                                        dataObj.put("submit", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("submit")));
                                        dataObj.put("create_time", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("create_time")));
                                        break;
                                    }
                                }
                            }

//                            JSONArray dataArray = myApp.getDataArray();
//                            for(int i = 0; i < dataArray.length(); i++) {
//                                JSONObject obj = dataArray.getJSONObject(i);
//                                if(obj.getString("table_name").equals("ActualSituation")) {
//                                    JSONObject dataObj = obj.getJSONObject("data");
//                                    if(dataObj.getInt("pad_id") == acceptanceSituationCursor.getInt(acceptanceSituationCursor.getColumnIndex("id"))) {
//                                        dataObj.put("description", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("description")));
//                                        dataObj.put("url", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("url")));
//                                        dataObj.put("score", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("score")));
//                                        dataObj.put("submit", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("submit")));
//                                    }
//                                }
//                            }
//                            myApp.saveDataToPref();

                            // 把数据存储到需要同步的对象中
//                            JSONObject itemObject = new JSONObject();
//                            itemObject.put("pad_id", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("id")));
//                            itemObject.put("description", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("description")));
//                            itemObject.put("url", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("url")));
//                            itemObject.put("score", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("score")));
//                            itemObject.put("submit", acceptanceSituationCursor.getString(acceptanceSituationCursor.getColumnIndex("submit")));
//                            itemObject.put("device_id", uuid);
//                            JSONObject obj = new JSONObject();
//                            obj.put("operation", "update");
//                            obj.put("table_name", "ActualSituation");
//                            obj.put("data", itemObject);
//                            myApp.getDataArray().put(obj);
//                            myApp.saveDataToPref();
                        }
                        llAccept.setVisibility(View.GONE);
                        llAcceptResult.setVisibility(View.VISIBLE);
                        tvAccepter.setText(myApp.getUser() == null ? "未知" : myApp.getUser().getUsername());
                        tvScore.setText("(" + actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getScore() + "分)");
                        tvAcceptanceInstruction.setText(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getDescription());
                        boolean allSubmit = true;
                        for (int i = 0; i < actualAcceptanceSituationList.size(); i++) {
                            if(actualAcceptanceSituationList.get(i).getSubmit().equals("0")) {
                                allSubmit = false;
                                break;
                            }
                        }
                        if(allSubmit) {
                            long currentTime = System.currentTimeMillis() / 1000;
                            ContentValues rcv = new ContentValues();
                            rcv.put("submit", "1");
                            rcv.put("create_time", currentTime);
                            long countRecord = db.update("qu_record", rcv, "id=?", new String[]{currentRecord.getId()});
                            if(countRecord == 1) {
                                task.setFinish("1");
                                currentRecord.setSubmit("1");
                                currentRecord.setCreate_time(currentTime + "");
                                recordAdapter.notifyDataSetChanged();
                                for(int i = 0; i < tempArray.length(); i++) {
                                    JSONObject obj = tempArray.getJSONObject(i);
                                    if(obj.getString("table_name").equals("Record")) {
                                        JSONObject dataObj = obj.getJSONObject("data");
                                        if(dataObj.getString("pad_id").equals(currentRecord.getId())) {
                                            dataObj.put("submit", "1");
                                            dataObj.put("create_time", currentTime + "");
                                            break;
                                        }
                                    }
                                }

                                try {
                                    JSONArray dataArray = myApp.getDataArray();
                                    for (int i = 0; i < tempArray.length(); i++) {
                                        dataArray.put(tempArray.getJSONObject(i));
                                    }
                                    myApp.saveDataToPref();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                // 把数据存储到需要同步的对象中
//                                JSONArray dataArray = myApp.getDataArray();
//                                for(int i = 0; i < dataArray.length(); i++) {
//                                    JSONObject obj = dataArray.getJSONObject(i);
//                                    if(obj.getString("table_name").equals("Record")) {
//                                        JSONObject dataObj = obj.getJSONObject("data");
//                                        if(dataObj.getString("pad_id").equals(currentRecord.getId())) {
//                                            dataObj.put("submit", "1");
//                                        }
//                                    }
//                                }
//                                myApp.saveDataToPref();

//                                JSONObject itemObject = new JSONObject();
//                                itemObject.put("pad_id", currentRecord.getId());
//                                itemObject.put("submit", "1");
//                                itemObject.put("device_id", uuid);
//                                JSONObject obj = new JSONObject();
//                                obj.put("operation", "update");
//                                obj.put("table_name", "Record");
//                                obj.put("data", itemObject);
//                                myApp.getDataArray().put(obj);
//                                myApp.saveDataToPref();
                            }
                        }
                    } else {
                        CustomViewUtil.createToast(TaskAcceptanceActivity.this, "提交验收结果失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                }
            }
        });
    }

    private void takeVideo() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        Calendar c = Calendar.getInstance();
        String fileName = format.format(c.getTime());
        videoFile = new File(currentRecord.getDirectoryPath() + "/" + currentAcceptanceStandard.getId() + "_" + fileName + ".mp4");
        if (!videoFile.getParentFile().exists()) {
            videoFile.getParentFile().mkdirs();
        }
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, 222);
    }

    private void showDeleteDialog(final Record deleteRecord, final int index) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TaskAcceptanceActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_confirm_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int) (0.33 * screenWidth), (int) (0.24 * screenWidth));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Button btnConfirm = (Button) view.findViewById(R.id.btn_confirm_delete_confirm_dialog);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_delete_confirm_dialog);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                deleteDataFromDB(deleteRecord, index);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void deleteDataFromDB(Record record, int index) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // 删除记录表中的数据
            JSONObject itemObject = new JSONObject();
            itemObject.put("id", record.getId());
            JSONObject obj = new JSONObject();
            obj.put("operation", "delete");
            obj.put("table_name", "Record");
            obj.put("data", itemObject);
            myApp.getDataArray().put(obj);
            myApp.saveDataToPref();
            db.delete("qu_record", "id=?", new String[]{record.getId()});
            // 删除实际情况表中的数据
            Cursor actualSituationCursor = db.rawQuery("select * from qu_actual_situation where record_id=?", new String[]{record.getId()});
            while(actualSituationCursor.moveToNext()) {
                JSONObject item = new JSONObject();
                item.put("id", actualSituationCursor.getString(actualSituationCursor.getColumnIndex("id")));
                JSONObject itemObj = new JSONObject();
                itemObj.put("operation", "delete");
                itemObj.put("table_name", "ActualSituation");
                itemObj.put("data", item);
                myApp.getDataArray().put(itemObj);
                myApp.saveDataToPref();
            }
            db.delete("qu_actual_situation", "record_id=?", new String[]{record.getId()});
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deletePhotoFile(record.getDirectoryPath());
        recordList.remove(record);
        // 如果删除掉记录之后列表为空，重置右侧验收内容
        if(recordList.size() == 0) {
            currentRecordIndex = -1;
            currentRecord = null;
            currentAcceptanceStandardIndex = 0;
            viewPager.setCurrentItem(currentAcceptanceStandardIndex);
            actualAcceptanceSituationList.clear();
            for (int i = 0; i < acceptStandardList.size(); i++) {
                ActualAcceptanceSituation aas = new ActualAcceptanceSituation();
                aas.setRecord_id("");
                aas.setDescription("");
                aas.setScore("");
                aas.setSubmit("0");
                aas.setUrl("");
                aas.setLocal_url("");
                aas.setAccept_id(acceptStandardList.get(i).getId());
                actualAcceptanceSituationList.add(aas);
            }
            llTakePhoto.setVisibility(View.VISIBLE);
            ivActualPhoto.setVisibility(View.GONE);
            llAccept.setVisibility(View.VISIBLE);
            llAcceptResult.setVisibility(View.GONE);
            etAcceptanceInstruction.setText("");
            rbScore.setRating(0);
            etAcceptanceInstruction.setEnabled(false);
            rbScore.setEnabled(false);
            btnSubmit.setEnabled(false);
        } else {
            // 如果删除的是当前所选项,则设置选中项为第一项
            if(currentRecordIndex == index) {
                currentRecordIndex = 0;
                currentRecord = recordList.get(0);
                Cursor actualAcceptanceSituationCursor = db.rawQuery("select * from qu_actual_situation where record_id=?", new String[]{currentRecord.getId()});
                actualAcceptanceSituationList.clear();
                if(actualAcceptanceSituationCursor.getCount() > 0) {
                    while (actualAcceptanceSituationCursor.moveToNext()) {
                        ActualAcceptanceSituation aas = new ActualAcceptanceSituation();
                        aas.setId(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("id")));
                        aas.setRecord_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("record_id")));
                        aas.setDescription(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("description")));
                        aas.setScore(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("score")));
                        aas.setSubmit(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("submit")));
                        aas.setUrl(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("url")));
                        aas.setCreate_time(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("create_time")));
                        aas.setLocal_url(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("local_url")));
                        aas.setAccept_id(actualAcceptanceSituationCursor.getString(actualAcceptanceSituationCursor.getColumnIndex("accept_id")));
                        actualAcceptanceSituationList.add(aas);
                    }
                }
                if(actualAcceptanceSituationList.get(0).getLocal_url().equals("")) {
                    llTakePhoto.setVisibility(View.VISIBLE);
                    ivActualPhoto.setVisibility(View.GONE);
                } else {
                    llTakePhoto.setVisibility(View.GONE);
                    ivActualPhoto.setVisibility(View.VISIBLE);
                    Glide.with(this).load(new File(actualAcceptanceSituationList.get(0).getLocal_url())).dontAnimate().into(ivActualPhoto);
                    ivActualPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showLocalBigPicDialog(actualAcceptanceSituationList.get(0).getLocal_url());
                        }
                    });
                }
                llAccept.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.GONE : View.VISIBLE);
                llAcceptResult.setVisibility(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? View.VISIBLE : View.GONE);
                tvAcceptanceInstruction.setText(actualAcceptanceSituationList.get(0).getDescription());
                tvAccepter.setText(actualAcceptanceSituationList.get(0).getSubmit().equals("1") ? currentRecord.getUsername() : "");
                tvScore.setText("(" + actualAcceptanceSituationList.get(0).getScore() + "分)");
                etAcceptanceInstruction.setEnabled(currentRecord == null ? false : true);
                rbScore.setEnabled(currentRecord == null ? false : true);
                btnSubmit.setEnabled(currentRecord == null ? false : true);
                for (int i = 0; i < recordList.size(); i++) {
                    recordList.get(i).setSelected(i == 0 ? true : false);
                }
            }
        }
        recordAdapter.notifyDataSetChanged();
    }

    private void deletePhotoFile(String directoryPath) {
        File file = new File(directoryPath);
        if(file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                File[] files = file.listFiles();
                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        deletePhotoFile(files[i].getAbsolutePath());
                    }
                }
                file.delete();
            }
        }
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

    private void takePhoto() {
        // 获取当前时间，进一步转化为字符串
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        Calendar c = Calendar.getInstance();
        String fileName = format.format(c.getTime());
//        imageFile = new File(currentRecord.getDirectoryPath() + "/" + currentAcceptanceStandard.getId() + "_" + fileName + ".png");
        imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QualityManagement/AcceptanceRecord/" + fileName + ".png";
        imageFile = new File(imageFilePath);
        if (!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdirs();
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(imageFile));
        startActivityForResult(intent, 111);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("imageFilePath", imageFilePath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageFilePath = savedInstanceState.getString("imageFilePath");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            if(recordList.size() > 0) {
                for (int i = 0; i < actualAcceptanceSituationList.size(); i++) {
                    if(actualAcceptanceSituationList.get(i).getSubmit().equals("0")) {
                        CustomViewUtil.createToast(TaskAcceptanceActivity.this, "尚有未提交的验收内容！");
                        return true;
                    }
                }
            }
            Intent intent = new Intent();
            intent.putExtra("task", task);
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 111:  // 拍照
                llTakePhoto.setVisibility(View.GONE);
                ivActualPhoto.setVisibility(View.VISIBLE);
                ivPlay.setVisibility(View.GONE);
//                saveFile(imageFile.getAbsolutePath(), BitmapFactory.decodeFile(imageFile
//                        .getAbsolutePath()));
//                Glide.with(this).load(imageFile).dontAnimate().into(ivActualPhoto);
//                actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setLocal_url(imageFile.getAbsolutePath());
                saveFile(imageFilePath, BitmapFactory.decodeFile(imageFilePath));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Glide.with(this).load(new File(imageFilePath)).dontAnimate().into(ivActualPhoto);
                actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setLocal_url(imageFilePath);
                ivActualPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showLocalBigPicDialog(imageFilePath);
                    }
                });
                break;
            case 222:
                llTakePhoto.setVisibility(View.GONE);
                ivActualPhoto.setVisibility(View.VISIBLE);
                ivPlay.setVisibility(View.VISIBLE);
                Bitmap bitmap = getVideoThumbnail(videoFile.getAbsolutePath());
//                Glide.with(this).load(bitmap).dontAnimate().into(ivActualPhoto);
                ivActualPhoto.setImageBitmap(bitmap);
                actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).setLocal_url(videoFile.getAbsolutePath());
                break;
        }
    }

    private void saveFile(String fileName, Bitmap bitmap) {
        try {
            File dirFile = new File(fileName);
            //检测图片是否存在
            if(dirFile.exists()){
                dirFile.delete();  //删除原图片
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imageFile));
            //100表示不进行压缩，80表示压缩率为20%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getVideoThumbnail(String filePath) {
        // MediaMetadataRetriever is available on API Level 8
        // but is hidden until API Level 10
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName("android.media.MediaMetadataRetriever");
            instance = clazz.newInstance();
            Method method = clazz.getMethod("setDataSource", String.class);
            method.invoke(instance, filePath);
            // The method name changes between API Level 9 and 10.
            if (Build.VERSION.SDK_INT <= 9) {
                return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
            } else {
                byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap != null) return bitmap;
                }
                return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
            }
        } catch (Exception ex) {
            // Assume this is a corrupt video file
        } finally {
            try {
                if (instance != null) {
                    clazz.getMethod("release").invoke(instance);
                }
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    private void findViews() {
        tvUsername = (TextView) findViewById(R.id.tv_username_task_acceptance);
        tvProjectName = (TextView) findViewById(R.id.tv_project_name_task_acceptance);
        tvPath = (TextView) findViewById(R.id.tv_path_task_acceptance);
        tvAcceptName = (TextView) findViewById(R.id.tv_accept_name_task_acceptance);
        lvRecord = (ListView) findViewById(R.id.lv_record_list_task_acceptance);
        ivAdd = (ImageView) findViewById(R.id.iv_add_acceptance_task_acceptance);
        viewPager = (ViewPager) findViewById(R.id.vp_standard_pic_task_acceptance);
        dotGroup =(ViewGroup) findViewById(R.id.ll_dot_task_acceptance);
        llTakePhoto = (LinearLayout) findViewById(R.id.ll_take_photo_task_acceptance);
        ivTakePhoto = (ImageView) findViewById(R.id.iv_take_photo_task_acceptance);
        ivActualPhoto = (ImageView) findViewById(R.id.iv_actual_pic_task_acceptance);
        ivPlay = (ImageView) findViewById(R.id.iv_play_task_acceptance);
        tvStandardContent = (TextView) findViewById(R.id.tv_standard_task_acceptance);
        llAccept = (LinearLayout) findViewById(R.id.ll_accept_task_acceptance);
        etAcceptanceInstruction = (EditText) findViewById(R.id.et_accept_instruction_task_acceptance);
        rbScore = (RatingBar) findViewById(R.id.rb_score_task_acceptance);
        btnSubmit = (Button) findViewById(R.id.btn_submit_task_acceptance);
        llAcceptResult = (LinearLayout) findViewById(R.id.ll_accept_result_task_acceptance);
        tvAcceptanceInstruction = (TextView) findViewById(R.id.tv_accept_instruction_task_acceptance);
        tvAccepter = (TextView) findViewById(R.id.tv_accepter_task_acceptance);
        tvScore = (TextView) findViewById(R.id.tv_score_task_acceptance);
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
    };

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
            for(int i = 0; i < dots.length; i++){
                if(i == position){
                    dots[i].setBackgroundResource(R.drawable.dot_selected);
                    currentAcceptanceStandardIndex = position;
                    currentAcceptanceStandard = acceptStandardList.get(currentAcceptanceStandardIndex);
                    tvAcceptName.setText("(" + currentAcceptanceStandard.getAccept_name() + ")");
                    tvStandardContent.setText(acceptStandardList.get(currentAcceptanceStandardIndex).getStandard());
                    etAcceptanceInstruction.setText("");
                    if(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getLocal_url().equals("")) {
                        llTakePhoto.setVisibility(View.VISIBLE);
                        ivActualPhoto.setVisibility(View.GONE);
                    } else {
                        llTakePhoto.setVisibility(View.GONE);
                        ivActualPhoto.setVisibility(View.VISIBLE);
                        if(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getLocal_url().endsWith(".mp4")) {
                            ivPlay.setVisibility(View.VISIBLE);
                            ivActualPhoto.setImageBitmap(getVideoThumbnail(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getLocal_url()));
                            ivPlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(TaskAcceptanceActivity.this, PlayVideoActivity.class);
                                    intent.putExtra("url", actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getLocal_url());
                                    startActivity(intent);
                                }
                            });
                        } else {
                            ivPlay.setVisibility(View.GONE);
                            Glide.with(TaskAcceptanceActivity.this).load(new File(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getLocal_url())).dontAnimate().into(ivActualPhoto);
                            ivActualPhoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showLocalBigPicDialog(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getLocal_url());
                                }
                            });
                        }
                    }
                    if(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getSubmit().equals("1")) {
                        llAccept.setVisibility(View.GONE);
                        llAcceptResult.setVisibility(View.VISIBLE);
                        tvAcceptanceInstruction.setText(actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getDescription());
                        tvScore.setText("(" + actualAcceptanceSituationList.get(currentAcceptanceStandardIndex).getScore() + "分)");
                    } else {
                        llAccept.setVisibility(View.VISIBLE);
                        llAcceptResult.setVisibility(View.GONE);
                        rbScore.setRating(0);
                        etAcceptanceInstruction.setText("");
                    }
                } else {
                    dots[i].setBackgroundResource(R.drawable.dot_unselected);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    private void initDots() {
        dots = new ImageView[acceptStandardList.size()];
        for (int i = 0; i < acceptStandardList.size(); i++) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(20, 20));
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            imageView.setLayoutParams(layoutParams);
            dots[i] = imageView;
            if (i == 0) {
                dots[i].setBackgroundResource(R.drawable.dot_selected);
            } else {
                dots[i].setBackgroundResource(R.drawable.dot_unselected);
            }
            dotGroup.addView(dots[i]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("TaskAcceptanceActivity.onDestroy");
        if(currentRecord != null) {
            if(currentRecord.getSubmit().equals("0")) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long recordCount = db.delete("qu_record", "id=?", new String[]{currentRecord.getId()});
                long acCount = db.delete("qu_actual_situation", "record_id=?", new String[]{currentRecord.getId()});
                System.out.println("recordCount=" + recordCount + ",acCount=" + acCount);
                db.close();
            } else {
//                try {
//                    JSONArray dataArray = myApp.getDataArray();
//                    for (int i = 0; i < tempArray.length(); i++) {
//                        dataArray.put(tempArray.getJSONObject(i));
//                    }
//                    myApp.saveDataToPref();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }
}
