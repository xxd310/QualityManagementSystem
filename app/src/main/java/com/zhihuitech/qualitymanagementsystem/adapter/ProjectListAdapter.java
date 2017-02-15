package com.zhihuitech.qualitymanagementsystem.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhihuitech.qualitymanagementsystem.R;
import com.zhihuitech.qualitymanagementsystem.entity.Project;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2016/12/19.
 */
public class ProjectListAdapter extends BaseAdapter {
    private Context context;
    private List<Project> list;

    public ProjectListAdapter(Context context, List<Project> list) {
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
            convertView = mInflater.inflate(R.layout.project_gridview_item, null);
            viewHolder.iv = (ImageView) convertView
                    .findViewById(R.id.iv_project_pic_gridview_item);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_project_name_gridview_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Project project = (Project) getItem(position);
        viewHolder.tv.setText(project.getName());
//        viewHolder.iv.setImageBitmap(BitmapFactory.decodeFile(project.getUrl()));
//        Glide.with(context).load(new File(project.getUrl())).dontAnimate().into(viewHolder.iv);
        System.out.println("path=" + project.getLocal_url());
        Glide.with(context).load(new File(project.getLocal_url())).diskCacheStrategy(DiskCacheStrategy.NONE).dontAnimate().error(R.drawable.logo).into(viewHolder.iv);
//        viewHolder.iv.setImageBitmap(BitmapFactory.decodeFile(project.getLocal_url()));
        return convertView;
    }

    static class ViewHolder {
        ImageView iv;
        TextView tv;
    }
}
