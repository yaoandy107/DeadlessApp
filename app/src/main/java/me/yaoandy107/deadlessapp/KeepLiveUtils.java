package me.yaoandy107.deadlessapp;

import android.app.ActivityManager;
import android.content.Context;


import java.util.List;

/**
 * Created by yaoweichen on 2017/10/21.
 */

public class KeepLiveUtils {

    // 判斷服務是否在運行
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = null;
        if (myAM != null) {
            myList = myAM.getRunningServices(100);
        }
        if (myList != null && myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < (myList != null ? myList.size() : 0); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
