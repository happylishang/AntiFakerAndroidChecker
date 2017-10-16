package com.snail.antifake.deviceid;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;

/**
 * Author: snail
 * Data: 2017/8/3 下午3:25
 * Des:
 * version:
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Application mApplication;
    public CrashHandler(Application application){
        mApplication=application;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Process.killProcess( Process.myPid());
        ActivityManager manager = (ActivityManager)
                mApplication.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == Process.myPid()) {
                if (!mApplication.getPackageName().equals(processInfo.processName)) {
                    Process.killProcess( Process.myPid());
                }
                break;
            }
        }

    }
}
