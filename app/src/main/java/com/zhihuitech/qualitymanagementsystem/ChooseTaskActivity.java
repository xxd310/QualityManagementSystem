package com.zhihuitech.qualitymanagementsystem;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.zhihuitech.qualitymanagementsystem.adapter.ExpandableListAdapter;
import com.zhihuitech.qualitymanagementsystem.adapter.TaskListAdapter;
import com.zhihuitech.qualitymanagementsystem.entity.Area;
import com.zhihuitech.qualitymanagementsystem.entity.Classify;
import com.zhihuitech.qualitymanagementsystem.entity.Project;
import com.zhihuitech.qualitymanagementsystem.entity.Task;
import com.zhihuitech.qualitymanagementsystem.util.CustomViewUtil;
import com.zhihuitech.qualitymanagementsystem.util.DateUtil;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseContext;
import com.zhihuitech.qualitymanagementsystem.util.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ChooseTaskActivity extends Activity {
    private TextView tvProjectName;
    private TextView tvUsername;
    private TextView tvNoTask;
    private LinearLayout llTaskList;
    private ViewPager viewPager;
    private ExpandableListView elv;
    private EditText etSearchCondition;
    private ImageView ivClearContent;

    private ExpandableListAdapter adapter;

    private List<View> viewList =  new ArrayList<>();
    // 总页数
    private int pageCount;
    // 底部小圆点
    private ViewGroup lineGroup;
    private ImageView[] lines;
    private int currentIndex = 0;

    private Project project;
    private Area area;
    private Classify classify;

    private ArrayList<Area> groupList = new ArrayList<>();
    private ArrayList<ArrayList<Classify>> childList = new ArrayList<>();
    private List<Task> allStoreTaskList = new ArrayList<>();
    private List<Task> allTaskList = new ArrayList<>();
    private List<List<Task>> taskList = new ArrayList<>();
    private List<TaskListAdapter> adapterList = new ArrayList<>();

    // 处理数据库所需的对象
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private MyApplication myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_task);

        myApp = (MyApplication) getApplication();
        findViews();
        initData();
        addListeners();
    }

    private void addListeners() {
        tvProjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                adapter.setmGroupPosition(groupPosition);
                adapter.setmChildPosition(childPosition);
                adapter.notifyDataSetChanged();
                try {
                    allStoreTaskList.clear();
                    allTaskList.clear();
                    taskList.clear();
                    adapterList.clear();
                    etSearchCondition.setText("");
                    db = dbHelper.getReadableDatabase();
                    area = groupList.get(groupPosition);
                    classify = childList.get(groupPosition).get(childPosition);
                    Cursor taskCursor = db.rawQuery("select * from qu_task where project_id=? and area_id=? and classify_id=?", new String[]{project.getId(), area.getId(), classify.getId()});
                    while (taskCursor.moveToNext()) {
                        Task task = new Task();
                        task.setId(taskCursor.getString(taskCursor.getColumnIndex("id")));
                        task.setName(taskCursor.getString(taskCursor.getColumnIndex("task_name")));
                        task.setProject_id(taskCursor.getString(taskCursor.getColumnIndex("project_id")));
                        task.setArea_id(taskCursor.getString(taskCursor.getColumnIndex("area_id")));
                        task.setClassify_id(taskCursor.getString(taskCursor.getColumnIndex("classify_id")));
                        task.setUrl(taskCursor.getString(taskCursor.getColumnIndex("url")));
                        task.setDescription(taskCursor.getString(taskCursor.getColumnIndex("description")));
                        task.setLocal_url(taskCursor.getString(taskCursor.getColumnIndex("local_url")));
                        task.setFinish("0");
                        Cursor recordCursor = db.rawQuery("select * from qu_record where user_id=? and project_id=? and area_id=? and classify_id=? and task_id=? and create_time>? and create_time<?", new String[]{myApp.getUser().getId(), project.getId(), area.getId(), classify.getId(), taskCursor.getString(taskCursor.getColumnIndex("id")), DateUtil.getStartTime() + "", DateUtil.getEndTime() + ""});
                        while (recordCursor.moveToNext()) {
                            if(recordCursor.getString(recordCursor.getColumnIndex("submit")).equals("1")) {
                                task.setFinish("1");
                            }
                        }
                        allStoreTaskList.add(task);
                    }
                    if(allStoreTaskList.size() == 0) {
                        tvNoTask.setVisibility(View.VISIBLE);
                        llTaskList.setVisibility(View.GONE);
                        return true;
                    }
                    allTaskList.addAll(allStoreTaskList);
                    tvNoTask.setVisibility(View.GONE);
                    llTaskList.setVisibility(View.VISIBLE);
                    calculatePageCount();
                    showContent();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(db != null) {
                        db.close();
                    }
                }
                return true;
            }
        });
        ivClearContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearchCondition.setText("");
            }
        });

        etSearchCondition.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String condition = editable.toString();
                System.out.println("condition=" + condition);
                allTaskList.clear();
                taskList.clear();
                adapterList.clear();
                for (int i = 0; i < allStoreTaskList.size(); i++) {
                    if(allStoreTaskList.get(i).getName().contains(condition)) {
                        allTaskList.add(allStoreTaskList.get(i));
                    }
                }
                if(allTaskList.size() == 0) {
                    tvNoTask.setVisibility(View.VISIBLE);
                    llTaskList.setVisibility(View.GONE);
                    return;
                }
                tvNoTask.setVisibility(View.GONE);
                llTaskList.setVisibility(View.VISIBLE);
                calculatePageCount();
                showContent();
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        tvUsername.setText(myApp.getUser() == null ? "未知" : myApp.getUser().getUsername());
        project = intent.hasExtra("project") ? (Project) intent.getSerializableExtra("project") : null;
        if(project == null) {
            return;
        }
        tvProjectName.setText(project == null ? "未知项目" : project.getName());
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
        try {
            db = dbHelper.getReadableDatabase();
            Cursor areaCursor = db.rawQuery("select * from qu_area where project_id=?", new String[]{project.getId()});
            while(areaCursor.moveToNext()) {
                Area area = new Area();
                area.setId(areaCursor.getString(areaCursor.getColumnIndex("id")));
                area.setName(areaCursor.getString(areaCursor.getColumnIndex("area_name")));
                area.setProject_id(areaCursor.getString(areaCursor.getColumnIndex("project_id")));
                area.setDescription(areaCursor.getString(areaCursor.getColumnIndex("description")));
                groupList.add(area);
            }
            System.out.println("groupList.size=" + groupList.size());
            for (int i = 0; i < groupList.size(); i++) {
                ArrayList<Classify> list = new ArrayList<>();
                Cursor classifyCursor = db.rawQuery("select * from qu_classify where project_id=? and area_id=?", new String[]{project.getId(), groupList.get(i).getId()});
                while (classifyCursor.moveToNext()) {
                    Classify classify = new Classify();
                    classify.setId(classifyCursor.getString(classifyCursor.getColumnIndex("id")));
                    classify.setName(classifyCursor.getString(classifyCursor.getColumnIndex("classify_name")));
                    classify.setProject_id(classifyCursor.getString(classifyCursor.getColumnIndex("project_id")));
                    classify.setArea_id(classifyCursor.getString(classifyCursor.getColumnIndex("area_id")));
                    classify.setDescription(classifyCursor.getString(classifyCursor.getColumnIndex("description")));
                    list.add(classify);
                }
                childList.add(list);
            }
            adapter = new ExpandableListAdapter(groupList, childList, ChooseTaskActivity.this);
            elv.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(db != null) {
                db.close();
            }
        }
    }

    private void findViews() {
        elv = (ExpandableListView) findViewById(R.id.elv_area_classify_choose_task);
        tvProjectName = (TextView) findViewById(R.id.tv_project_name_choose_task);
        tvUsername = (TextView) findViewById(R.id.tv_username_choose_task);
        viewPager = (ViewPager) findViewById(R.id.vp_choose_task);
        lineGroup =(ViewGroup) findViewById(R.id.ll_line_choose_task);
        tvNoTask = (TextView) findViewById(R.id.tv_no_task_tip_choose_task);
        llTaskList = (LinearLayout) findViewById(R.id.ll_task_list_choose_task);
        etSearchCondition = (EditText) findViewById(R.id.et_search_condition_choose_task);
        ivClearContent = (ImageView) findViewById(R.id.iv_clear_content_choose_task);
    }

    private void calculatePageCount() {
        if(allTaskList.size() <= 6) {
            pageCount = 1;
            List<Task> showList = new ArrayList<>();
            showList.addAll(allTaskList);
            taskList.add(showList);
        } else {
            if(allTaskList.size() % 6 == 0) {
                pageCount = allTaskList.size() / 6;
                List<Task> showList = null;
                for(int i = 0; i < pageCount; i++) {
                    showList = new ArrayList<>();
                    for(int j = 0; j < 6; j++) {
                        showList.add(allTaskList.get(j + 6 * i));
                    }
                    taskList.add(showList);
                }
            } else {
                pageCount = allTaskList.size() / 6 + 1;
                List<Task> showList = null;
                for(int i = 0; i < pageCount - 1; i++) {
                    showList = new ArrayList<>();
                    for(int j = 0; j < 6; j++) {
                        showList.add(allTaskList.get(j + 6 * i));
                    }
                    taskList.add(showList);
                }
                showList = new ArrayList<>();
                for(int i = allTaskList.size() - allTaskList.size() % 6; i < allTaskList.size(); i++) {
                    showList.add(allTaskList.get(i));
                }
                taskList.add(showList);
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
        viewList.clear();
        for(int i = 0; i < pageCount; i++) {
            final int index = i;
            View view = getLayoutInflater().inflate(R.layout.task_list_item, null);
            GridView gv = (GridView) view.findViewById(R.id.gv_task_list_item);
            TaskListAdapter adapter = new TaskListAdapter(ChooseTaskActivity.this, taskList.get(i));
            gv.setAdapter(adapter);
            adapterList.add(adapter);
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    Cursor c = db.rawQuery("select * from qu_acceptance_standard where project_id=? and area_id=? and classify_id=? and task_id=?", new String[]{project.getId(), area.getId(), classify.getId(), taskList.get(index).get(position).getId()});
                    if(c.getCount() == 0) {
                        CustomViewUtil.createToast(ChooseTaskActivity.this, "该任务暂无验收内容！");
                        return;
                    }
                    Intent intent = new Intent(ChooseTaskActivity.this, TaskAcceptanceActivity.class);
                    intent.putExtra("project", project);
                    intent.putExtra("area", area);
                    intent.putExtra("classify", classify);
                    intent.putExtra("task", taskList.get(index).get(position));
                    startActivityForResult(intent, 111);
                }
            });
            viewList.add(view);
        }
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 111:
                if(resultCode == RESULT_OK) {
                    adapterList.get(currentIndex).notifyDataSetChanged();
                    Task t = (Task) data.getSerializableExtra("task");
                    for (int i = 0; i < taskList.get(currentIndex).size(); i++) {
                        if(taskList.get(currentIndex).get(i).getId().equals(t.getId())) {
                            taskList.get(currentIndex).set(i, t);
                            break;
                        }
                    }
                    adapterList.get(currentIndex).notifyDataSetChanged();
                }
                break;
        }
    }

    private void initLines() {
        lineGroup.removeAllViews();
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
                lines[i].setBackgroundResource(R.drawable.viewpager_selected);
            } else {
                lines[i].setBackgroundResource(R.drawable.viewpager_unselected);
            }
            lineGroup.addView(lines[i]);
        }
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
            currentIndex = position;
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
            for(int i = 0; i < lines.length; i++){
                if(i == position){
                    lines[i].setBackgroundResource(R.drawable.viewpager_selected);
                }else{
                    lines[i].setBackgroundResource(R.drawable.viewpager_unselected);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
