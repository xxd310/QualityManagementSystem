package com.zhihuitech.qualitymanagementsystem.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zhihuitech.qualitymanagementsystem.R;
import com.zhihuitech.qualitymanagementsystem.entity.Record;

import java.util.List;

/**
 * Created by Administrator on 2016/12/19.
 */
public class RecordListAdapter extends BaseAdapter {
    private Context context;
    private List<Record> list;

    public RecordListAdapter(Context context, List<Record> list) {
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
            convertView = mInflater.inflate(R.layout.record_list_item, null);
            viewHolder.ll = (LinearLayout) convertView.findViewById(R.id.ll_record_list_item);
            viewHolder.iv = (ImageView) convertView.findViewById(R.id.iv_clock_record_list_item);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_name_record_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Record record = (Record) getItem(position);
        viewHolder.ll.setBackgroundResource(record.isSelected() ? R.drawable.sidebar_bg_selected : R.drawable.sidebar_bg);
        viewHolder.tv.setText(record.getFormat_time());
        viewHolder.tv.setTextColor(record.isSelected() ? Color.WHITE : 0xFF494D59);
        viewHolder.iv.setVisibility(record.getSubmit().equals("1") ? View.VISIBLE : View.INVISIBLE);
        return convertView;
    }

    static class ViewHolder {
        LinearLayout ll;
        ImageView iv;
        TextView tv;
    }
}
