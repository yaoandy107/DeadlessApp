package me.yaoandy107.deadlessapp.service;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by yaoweichen on 2017/10/21.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("test", "onStartJob");
        startService();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e("test","onStopJob");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("test","onStartCommand");
        try {
            int id = 1;
            JobInfo.Builder builder = new JobInfo.Builder(id,
                    new ComponentName(getPackageName(), MyJobService.class.getName() ));
            builder.setPeriodic(60*1000);  // 間格 1 分鐘調用 onStartJob
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // 有網路的時候喚醒
            JobScheduler jobScheduler = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                int ret = jobScheduler.schedule(builder.build());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        startService();
        return super.onStartCommand(intent, flags, startId);
    }

public void startService(){
        Log.e("test", "MyJobService");
        startService(new Intent(this, OnLineService.class));
        startService(new Intent(this, KeepLiveService.class));
        Toast.makeText(this, "進程啟動", Toast.LENGTH_SHORT).show();
    }
}
