package com.snail.device.jni;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.android.internal.telephony.IEmulatorCheck;
import com.snail.device.R;

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

    public static boolean isEmulator() {
        return detect();
    }

    public void throwNativeCrash(){

    }
}
