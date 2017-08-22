package com.snail.device;

import android.app.Application;
import android.app.Service;
import android.view.WindowManager;

/**
 * Author: snail
 * Data: 2017/8/3 下午3:26
 * Des:
 * version:
 */

public class CrashHandlerApplication extends Application {

    private static  Application sApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        sApplication =this;
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());


        WindowManager windowManager= (WindowManager) getSystemService(Service.WINDOW_SERVICE);
        windowManager.addView();

    }

    public static Application getApplication(){
        return sApplication;
    }
}
