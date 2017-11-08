package me.yaoandy107.deadlessapp.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import java.lang.ref.WeakReference;

import me.yaoandy107.deadlessapp.KeepLiveUtils;
import me.yaoandy107.deadlessapp.MainActivity;
import me.yaoandy107.deadlessapp.R;
import me.yaoandy107.deadlessapp.StrongService;
import me.yaoandy107.deadlessapp.receiver.TickAlarmReceiver;

/**
 * Created by yaoweichen on 2017/10/21.
 */

public class OnLineService extends Service {

    protected PendingIntent tickPendIntent;
    WakeLock wakeLock;
    public static OnLineService mOnLineService;


    @Override
    public void onCreate() {
        super.onCreate();
        this.setTickAlarm();
        Log.e("test", "OnLineService");
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OnLineService");
        }

        notifyRunning();
        mOnLineService = this;

        Toast.makeText(OnLineService.this, "Service1 啟動中", Toast.LENGTH_SHORT).show();
        startKeepLiveService();
        /*
         * 此線程用於監聽 Service2 狀態
         */
        new Thread() {
            public void run() {
                while (true) {
                    boolean isRun = KeepLiveUtils.isServiceWork(OnLineService.this, "service.keppliveservice.service.KeepLiveService");
                    if (!isRun) {
                        android.os.Message msg = android.os.Message.obtain();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.start();
    }

    @Override
    public int onStartCommand(Intent param, int flags, int startId) {
        if (param == null) {
            return START_STICKY;
        }
        String cmd = param.getStringExtra("CMD");
        if (cmd == null) {
            cmd = "";
        }
        if (cmd.equals("TICK")) {
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(10*60*1000L /*10 minutes*/);
            }
        }
        return START_STICKY;
    }

    protected void tryReleaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    protected void setTickAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TickAlarmReceiver.class);
        int requestCode = 0;
        tickPendIntent = PendingIntent.getBroadcast(this,
                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long triggerAtTime = System.currentTimeMillis();
        int interval = 300 * 1000;
        if (alarmMgr != null) {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
        }
    }

    protected void notifyRunning() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "")
                .setContentTitle("正在運行")
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setTicker("正在運行中")
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.notify(0, notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelNotifyRunning();
        this.tryReleaseWakeLock();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<OnLineService> reference;

        MyHandler(OnLineService service) {
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            OnLineService service = reference.get();
            switch (msg.what) {
                case 1:
                    service.startKeepLiveService();
                    break;

                default:
                    break;
            }
        }
    }

    private MyHandler handler = new MyHandler(this);

    protected void cancelNotifyRunning() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(0);
    }

    /**
     * 使用 aidl 啟動 Service2
     */
    private StrongService startS2 = new StrongService.Stub() {

        @Override
        public void stopService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), KeepLiveService.class);
            getBaseContext().stopService(i);
        }

        @Override
        public void StartService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), KeepLiveService.class);
            getBaseContext().startService(i);
        }
    };

    /**
     * 系統回收記憶體時，會調用 onTrimMemory，覆寫 onTrimMemory，當系統清理記憶體時重新啟動 Service2
     */
    @Override
    public void onTrimMemory(int level) {
        startKeepLiveService();
    }

    /**
     * 判斷 Service 是否在運行，如果不是則啟動 Service2
     */
    private void startKeepLiveService() {
        boolean isRun = KeepLiveUtils.isServiceWork(OnLineService.this,
                "me.yaoandy107.deadlessapp.service.KeepLiveReceivers");
        if (!isRun) {
            try {
                startS2.StartService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) startS2;
    }
}
