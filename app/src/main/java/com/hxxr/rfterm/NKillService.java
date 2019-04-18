package com.hxxr.rfterm;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

// This class allows you to run code when the application closes
public class NKillService extends Service {

    // This code runs when the application closes
    @Override
    public void onCreate() {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(12345679);
    }



    public class NKillBinder extends Binder {
        public final Service service;
        public NKillBinder(Service service) {
            this.service = service;
        }
    }
    private NotificationManager manager;
    private final IBinder mBinder = new NKillBinder(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return Service.START_STICKY; }
}
