package com.zhihuitech.qualitymanagementsystem.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.victor.loading.rotate.RotateLoading;
import com.zhihuitech.qualitymanagementsystem.R;

import static android.R.attr.dial;

/**
 * Created by Administrator on 2016/7/16.
 */
public class CustomViewUtil {
    public static ProgressDialog dialog;

    public static void createToast(Context context, String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        TextView tvMessage = (TextView) view.findViewById(R.id.tv_toast_message);
        tvMessage.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public static void createProgressDialog(Context context, String message) {
        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);
        TextView tvMessage = (TextView)view.findViewById(R.id.tv_message);
        tvMessage.setText(message);
        RotateLoading rl = (RotateLoading) view.findViewById(R.id.rotate_loading);
        rl.start();
        dialog.setContentView(view);
    }

    public static void dismissDialog() {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
