package com.snail.antifake.deviceid.androidid;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * androidid获取
 */

public class IAndroidIdUtil {

    public static String getAndroidId(Context context) {
        String androidId;
        if (!TextUtils.isEmpty(androidId = ISettingUtils.getAndroidPropertyLevel1(context, Settings.Secure.ANDROID_ID))
                || !TextUtils.isEmpty(androidId = ISettingUtils.getAndroidProperty(context, Settings.Secure.ANDROID_ID))) {
            return androidId;
        }
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
