package me.yaoandy107.deadlessapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import me.yaoandy107.deadlessapp.service.OnLineService;

/**
 * Created by yaoweichen on 2017/10/21.
 */

public class TickAlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("test", "收到鬧鐘廣播");
        Intent startSrv = new Intent(context, OnLineService.class);
        startSrv.putExtra("CMD", "TICK");
        context.startService(startSrv);
    }

}