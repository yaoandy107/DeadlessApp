package me.yaoandy107.deadlessapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import me.yaoandy107.deadlessapp.KeepLiveUtils;
import me.yaoandy107.deadlessapp.StrongService;

/**
 * Created by yaoweichen on 2017/10/21.
 */

public class KeepLiveService extends Service {

    private final MyHandler handler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<KeepLiveService> reference;

        MyHandler(KeepLiveService service) {
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            KeepLiveService service = reference.get();
            if (service != null) {
                switch (msg.what) {
                    case 1:
                        service.startOnlineService();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onCreate() {
        Log.e("test", "KeepLiveService");
        Toast.makeText(KeepLiveService.this, "Service2 啟動中", Toast.LENGTH_SHORT).show();
        startOnlineService();
        /*
          此線程用於監聽 Service2 的狀態
         */
        new Thread() {
            public void run() {
                while (true) {
                    boolean isRun = KeepLiveUtils.isServiceWork(KeepLiveService.this,"me.yaoandy107.deadlessapp.service.OnLineService");
                    if (!isRun) {
                        Message msg = Message.obtain();
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
    public IBinder onBind(Intent intent) {
        return (IBinder) startS1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 判斷 Service1 是否還在運行，如果不是則啟動 Service1
     */
    private void startOnlineService() {
        boolean isRun = KeepLiveUtils.isServiceWork(KeepLiveService.this,
                "me.yaoandy107.deadlessapp.service.OnLineService");
        if (!isRun) {
            try {
                startS1.StartService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 使用 aidl 啟動 Service1
     */

    private StrongService startS1 = new StrongService.Stub() {

        @Override
        public void StartService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), OnLineService.class);
            getBaseContext().startService(i);
        }

        @Override
        public void stopService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), OnLineService.class);
            getBaseContext().stopService(i);
        }
    };

    @Override
    public void onTrimMemory(int level) {
        startOnlineService();
    }
}
