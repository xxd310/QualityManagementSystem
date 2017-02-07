package com.zhihuitech.qualitymanagementsystem.util;

import java.util.Calendar;

/**
 * Created by Administrator on 2017/1/19.
 */
public class DateUtil {
    public static long getStartTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis() / 1000;
    }

    public static long getEndTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTimeInMillis() / 1000;
    }
}
