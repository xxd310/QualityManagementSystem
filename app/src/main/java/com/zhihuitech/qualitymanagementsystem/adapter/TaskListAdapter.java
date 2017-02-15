package com.zhihuitech.qualitymanagementsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhihuitech.qualitymanagementsystem.R;
import com.zhihuitech.qualitymanagementsystem.entity.Task;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2016/12/19.
 */
public class TaskListAdapter extends BaseAdapter {
    private Context context;
    private List<Task> list;

    public TaskListAdapter(Context context, List<Task> list) {
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
            convertView = mInflater.inflate(R.layout.task_gridview_item, null);
            viewHolder.iv = (ImageView) convertView
                    .findViewById(R.id.iv_task_pic_task_gridview_item);
            viewHolder.ivFinish = (ImageView) convertView.findViewById(R.id.iv_finish_task_gridview_item);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_task_name_task_gridview_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Task task = (Task) getItem(position);
        viewHolder.tv.setText(task.getName());
        System.out.println("adapter=" + task.getLocal_url());
//        viewHolder.iv.setImageBitmap(BitmapFactory.decodeFile(task.getLocal_url()));
        Glide.with(context).load(new File(task.getLocal_url())).diskCacheStrategy(DiskCacheStrategy.NONE).dontAnimate().error(R.drawable.logo).into(viewHolder.iv);
        viewHolder.ivFinish.setVisibility(task.getFinish().equals("1") ? View.VISIBLE : View.GONE);
        return convertView;
    }

    static class ViewHolder {
        ImageView iv;
        ImageView ivFinish;
        TextView tv;
    }
}
