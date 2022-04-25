package com.snail.antifake.jni;

import android.content.Context;

import androidx.annotation.Keep;

import com.snail.antifake.deviceid.AndroidDeviceIMEIUtil;

/**
 * Author: snail
 * Data: 2017/7/20 下午4:46
 * Des:
 * version:
 */
@Keep
public class EmulatorDetectUtil {

    static {
        System.loadLibrary("emulator_check");
    }

    @Keep
    native  public static  boolean detectS();
    /**
     * 同时考虑特征值跟cache
     */
    public static boolean isEmulator(Context context) {
        return  detectS();
    }
    /**
     * 只考虑cache，Android R之后，模拟器机制有变化，检测会有问题
     */
    public static boolean isEmulatorFromAll(Context context) {
        return AndroidDeviceIMEIUtil.isRunOnEmulator(context) || detectS();
    }
}
