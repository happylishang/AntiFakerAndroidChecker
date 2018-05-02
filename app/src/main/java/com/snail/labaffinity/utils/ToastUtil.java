package com.snail.labaffinity.utils;

import android.widget.Toast;

/**
 * Author: hzlishang
 * Data: 16/10/11 下午12:47
 * Des:
 * version:
 */
public class ToastUtil {

    public static void show(String msg) {
        Toast.makeText(AppProfile.getAppContext(), msg, Toast.LENGTH_SHORT).show();
    }
}

