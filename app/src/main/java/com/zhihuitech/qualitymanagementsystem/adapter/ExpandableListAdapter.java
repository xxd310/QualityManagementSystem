package com.zhihuitech.qualitymanagementsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zhihuitech.qualitymanagementsystem.R;
import com.zhihuitech.qualitymanagementsystem.entity.Area;
import com.zhihuitech.qualitymanagementsystem.entity.Classify;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/12/26.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private ArrayList<Area> groupList;
    private ArrayList<ArrayList<Classify>> childList;
    private Context context;
    private int mGroupPosition = -1;
    private int mChildPosition = -1;

    public ExpandableListAdapter(ArrayList<Area> groupList, ArrayList<ArrayList<Classify>> childList, Context context) {
        super();
        this.groupList = groupList;
        this.childList = childList;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.expandable_list_group_item, null);
        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rl_group_item_expandable_list_group_item);
        TextView tv = (TextView) view.findViewById(R.id.tv_area_name_expandable_list_group_item);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_arrow_expandable_list_group_item);
        tv.setText(groupList.get(groupPosition).getName());
        iv.setImageResource(isExpanded ? R.drawable.arrow_down : R.drawable.arrow_right);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.expandable_list_child_item, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_classify_name_expandable_list_child_item);
        tv.setText(childList.get(groupPosition).get(childPosition).getName());
        tv.setBackgroundResource((groupPosition == mGroupPosition && childPosition == mChildPosition) ? R.drawable.sidebar_bg_selected : R.drawable.sidebar_bg);
        tv.setTextColor((groupPosition == mGroupPosition && childPosition == mChildPosition) ? 0xFFFFFFFF : 0xFF494D59);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    public void setmGroupPosition(int mGroupPosition) {
        this.mGroupPosition = mGroupPosition;
    }

    public void setmChildPosition(int mChildPosition) {
        this.mChildPosition = mChildPosition;
    }
}
