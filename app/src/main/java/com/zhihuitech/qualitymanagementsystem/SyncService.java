package com.zhihuitech.qualitymanagementsystem;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.zhihuitech.qualitymanagementsystem.entity.SyncEvent;
import org.greenrobot.eventbus.EventBus;

public class SyncService extends Service {
    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EventBus.getDefault().post(new SyncEvent("开始同步"));
        return super.onStartCommand(intent, flags, startId);
    }
}
