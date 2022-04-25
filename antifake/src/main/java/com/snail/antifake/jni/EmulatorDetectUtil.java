package com.snail.antifake.jni;

import android.content.Context;

import com.snail.antifake.deviceid.AndroidDeviceIMEIUtil;

/**
 * Author: snail
 * Data: 2017/7/20 下午4:46
 * Des:
 * version:
 */

public class EmulatorDetectUtil {

    static {
        System.loadLibrary("emulator_check");
    }

    public static native boolean detect();

    /**
     * 同时考虑特征值跟cache
     */
    public static boolean isEmulator(Context context) {
        return AndroidDeviceIMEIUtil.isRunOnEmulator(context) || detect();
    }
    /**
     * 只考虑cache，Android R之后，模拟器机制有变化，检测会有问题
     */
    public static boolean isEmulatorFromArch() {
        return detect();
    }

    public void throwNativeCrash() {

    }
}
