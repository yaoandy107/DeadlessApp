package me.yaoandy107.deadlessapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import me.yaoandy107.deadlessapp.receiver.KeepLiveReceivers;
import me.yaoandy107.deadlessapp.service.KeepLiveService;
import me.yaoandy107.deadlessapp.service.MyJobService;
import me.yaoandy107.deadlessapp.service.OnLineService;

/**
 * Created by yaoweichen on 2017/10/21.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent startSrv = new Intent(this, OnLineService.class);
        startService(startSrv);
        Intent i2 = new Intent(this, KeepLiveService.class);
        startService(i2);
        Log.i("test","version"+ Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT>=21){
            Intent i3 = new Intent(this, MyJobService.class);
            startService(i3);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerReceiver(this);
        Toast.makeText(getApplication(), "關掉", Toast.LENGTH_LONG);
    }

    // 註冊鎖屏廣播
    public void registerReceiver(Context context){
        IntentFilter filter=new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        context.getApplicationContext().registerReceiver(new KeepLiveReceivers(), filter);
    }

}
