package com.snail.labaffinity.utils;

import android.app.Application;

import com.snail.labaffinity.app.LabApplication;

/**
 * Author: hzlishang
 * Data: 16/10/11 下午12:45
 * Des:
 * version:
 */
public class AppProfile {

    public static Application getAppContext() {
        return LabApplication.getContext();
    }
}
