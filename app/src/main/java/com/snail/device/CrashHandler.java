package com.snail.device;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

/**
 * Author: snail
 * Data: 2017/8/3 下午3:25
 * Des:
 * version:
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Process.killProcess( Process.myPid());
        ActivityManager manager = (ActivityManager)
                CrashHandlerApplication.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == Process.myPid()) {
                if (!CrashHandlerApplication.getApplication().getPackageName().equals(processInfo.processName)) {
                    Process.killProcess( Process.myPid());
                }
                break;
            }
        }

    }
}
