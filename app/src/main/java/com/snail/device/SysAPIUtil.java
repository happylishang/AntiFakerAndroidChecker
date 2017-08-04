package com.snail.device;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Author: hzlishang
 * Data: 17-7-11 上午10:07
 * Des:
 * version:
 */

public class SysAPIUtil {




    public static String getMacAddress(Context context) {
        String macAddress = null;
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //先尝试获取一次，如果不为空，则不用打开wifi了
            WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
            if (null != info) {
                macAddress = info.getMacAddress();
            }
            if (!TextUtils.isEmpty(macAddress)) {
                return macAddress;
            }
            //为空，则打开wifi
            if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                //必须先打开，才能获取到MAC地址
                wifiManager.setWifiEnabled(true);
                wifiManager.setWifiEnabled(false);
            }
            info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
            if (null != info) {
                macAddress = info.getMacAddress();
            }
        } catch (Exception e) {
        }

        return macAddress;
    }

    /**
     * 手机厂商
     *
     * @return
     */
    public static String getPhoneManufacturer() {
        return android.os.Build.MANUFACTURER;
    }






    private String getMmei(Context context) {
        DeviceUuidFactory deviceUuidFactory = new DeviceUuidFactory(context);
        return deviceUuidFactory.getDeviceUuid().toString();
    }

    //    方案不行
    class DeviceUuidFactory {
        protected static final String PREFS_FILE = "device_id.xml";
        protected static final String PREFS_DEVICE_ID = "device_id";
        protected UUID uuid;

        public DeviceUuidFactory(Context context) {
            if (uuid == null) {
                synchronized (DeviceUuidFactory.class) {
                    if (uuid == null) {

                        {
                            final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            try {
//                                刷机
//                               都可以被hook
                                if (!"9774d56d682e549c".equals(androidId)) {
                                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                                } else {
                                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                                }
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                            // Write the value out to the prefs file
                        }
                    }
                }
            }
        }

        public UUID getDeviceUuid() {
            return uuid;
        }
    }


    public static String getAndroidId(Context context)   {

       return  Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);


    }


    public static String getSerialNumber(Context context) {
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");
            Method method = SystemProperties.getDeclaredMethod("native_get", String.class);
            method.setAccessible(true);
            return (String) method.invoke(SystemProperties, "ro.serialno");
        } catch (Exception e) {
            return null;
        }
    }

    public static String getJavaSerialNumber(Context context) {
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");
            Method method = SystemProperties.getDeclaredMethod("get", String.class);
            method.setAccessible(true);
            return (String) method.invoke(SystemProperties, "ro.serialno");
        } catch (Exception e) {
            return null;
        }
    }
}
