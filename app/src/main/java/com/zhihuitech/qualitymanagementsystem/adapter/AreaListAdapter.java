package com.zhihuitech.qualitymanagementsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhihuitech.qualitymanagementsystem.R;
import com.zhihuitech.qualitymanagementsystem.entity.Area;

import java.util.List;

/**
 * Created by Administrator on 2016/12/19.
 */
public class AreaListAdapter extends BaseAdapter {
    private Context context;
    private List<Area> list;

    public AreaListAdapter(Context context, List<Area> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.area_list_item, null);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_name_area_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Area area = (Area) getItem(position);
        viewHolder.tv.setText(area.getName());
        return convertView;
    }

    static class ViewHolder {
        TextView tv;
    }
}
