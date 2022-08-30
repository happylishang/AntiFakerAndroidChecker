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
    native public static boolean detectS();

    /**
     * 同时考虑特征值跟cache
     */
    public static boolean isEmulator(Context context) {
        return detectS();
    }

    /**
     * 只考虑cache，Android R之后，模拟器机制有变化，检测会有问题
     */
    public static boolean isEmulatorFromAll(Context context) {
        return AndroidDeviceIMEIUtil.isRunOnEmulator(context) || detectS();
    }

    public static int getSystemArch() {
        String cpuAbi=  PropertiesGet.getString("ro.product.cpu.abi");
        if("armeabi-v7a".equals(cpuAbi))
            return Arch.ARM32;
        if("arm64-v8a".equals(cpuAbi))
            return Arch.ARM64;
        if("x86".equals(cpuAbi)  )
            return Arch.X86;
        if(  "x86_64".equals(cpuAbi))
            return Arch.X86_64;
        return Arch.ARM64;
    }

    public interface Arch {
        int X86 = 0;
        int X86_64 = 1;
        int ARM32 = 2;
        int ARM64 = 3;
    }
}