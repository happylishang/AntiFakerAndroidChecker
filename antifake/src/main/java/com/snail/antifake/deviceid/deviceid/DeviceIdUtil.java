package com.snail.antifake.deviceid.deviceid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * deviceid的获取
 */

public class DeviceIdUtil {

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {

        String deviceId;

        //先看底下
        if (!TextUtils.isEmpty(deviceId = ITelephonyUtil.getDeviceIdLevel2(context))
                || !TextUtils.isEmpty(deviceId = IPhoneSubInfoUtil.getDeviceIdLevel2(context))) {
            return deviceId;
        }
        //再看中部
        if (!TextUtils.isEmpty(deviceId = ITelephonyUtil.getDeviceIdLevel1(context))
                || !TextUtils.isEmpty(deviceId = IPhoneSubInfoUtil.getDeviceIdLevel1(context))) {
            return deviceId;
        }
        //再看上部
        if (!TextUtils.isEmpty(deviceId = IPhoneSubInfoUtil.getDeviceIdLevel0(context))
                || !TextUtils.isEmpty(deviceId = ITelephonyUtil.getDeviceIdLevel0(context))) {
            return deviceId;
        }
        try {
            TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
            deviceId =telephonyManager.getDeviceId();
        }catch (Exception ignore){
        }
        return deviceId;
    }



    //  模拟器存在权限吗？
    public static boolean isEmulatorFromDeviceId(Context context) {
        return isAllZero(getDeviceId(context));
    }

    private static boolean isAllZero(@NonNull String content) {
        if (TextUtils.isEmpty(content))
            return false;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }
}
