package com.snail.labaffinity.app;

import android.app.Application;

import com.snail.labaffinity.utils.LogUtils;

import cn.campusapp.router.Router;

/**
 * Author: hzlishang
 * Data: 16/10/11 下午12:44
 * Des:
 * version:
 */
public class LabApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        Router.initBrowserRouter(this);
        Router.initActivityRouter(getApplicationContext());
        LogUtils.v("LabApplication create");
    }

    private static Application sApplication;

    public static Application getContext() {
        return sApplication;
    }
}
