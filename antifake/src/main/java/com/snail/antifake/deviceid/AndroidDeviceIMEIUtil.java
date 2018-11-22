package com.snail.antifake.deviceid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.snail.antifake.deviceid.androidid.IAndroidIdUtil;
import com.snail.antifake.deviceid.deviceid.DeviceIdUtil;
import com.snail.antifake.deviceid.emulator.EmuCheckUtil;
import com.snail.antifake.deviceid.macaddress.MacAddressUtils;
import com.snail.antifake.jni.PropertiesGet;


/**
 * Author: hzlishang
 * Data: 17-7-26 上午11:02
 * Des: java与C ，代理与服务都能被hook，目前还没找不能被篡改的方法，只能将篡改的成本提高
 * version:
 */


public class AndroidDeviceIMEIUtil {

    public static boolean isRunOnEmulator(Context context) {
        return EmuCheckUtil.mayOnEmulator(context);

    }

    public static String getDeviceId(Context context) {
        return DeviceIdUtil.getDeviceId(context);

    }

    public static String getAndroidId(Context context) {

        return IAndroidIdUtil.getAndroidId(context);
    }

    public static String getMacAddress(Context context) {

        return MacAddressUtils.getMacAddress(context);
    }


    //@Deprecated
    // IMPORTANT: This field should be initialized via a function call to
    // prevent its value being inlined in the app during compilation because
    // we will later set it to the value based on the app's target SDK.
    //  public static final String SERIAL = getString("no.such.thing");


    //    序列号	 重新烧录flash
    @SuppressLint("MissingPermission")
    public static String getSerialno() {
        String serialno = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serialno = Build.getSerial();
            } else {
                serialno = PropertiesGet.getString("ro.serialno");
                if (TextUtils.isEmpty(serialno)) {
                    serialno = Build.SERIAL;
                }
            }
        } catch (Exception e) {
        }
        return serialno;
    }


    public static String getManufacturer() {
        return PropertiesGet.getString("ro.product.manufacturer");
    }


    public static String getBrand() {
        return PropertiesGet.getString("ro.product.brand");
    }

    public static String getModel() {
        return PropertiesGet.getString("ro.product.model");
    }


    public static String getCpuAbi() {
        return PropertiesGet.getString("ro.product.cpu.abi");
    }


    public static String getDevice() {
        return PropertiesGet.getString("ro.product.device");
    }


    /**
     * The name of the underlying board, like "goldfish".
     */
    public static String getBoard() {
        return PropertiesGet.getString("ro.product.board");
    }


    /**
     * The name of the hardware (from the kernel command line or /proc).
     */
    public static String getHardware() {
        return PropertiesGet.getString("ro.hardware");
    }

    public static String getBootloader() {
        return PropertiesGet.getString("ro.bootloader");
    }

    //    //TODO 17-7-31 by lishang : 暂时这么获取，不太重要
    public static String getIMSI(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        return telephonyManager.getSubscriberId();
    }


    public static BatteryChangeReceiver sBatteryChangeReceiver;

    public static void registerBatteryChangeListener(Context context) {

        if (sBatteryChangeReceiver == null) {
            sBatteryChangeReceiver = new BatteryChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(sBatteryChangeReceiver, filter);
        }
    }

    public static void unRegisterBatteryChangeListener(Context context) {
        if (sBatteryChangeReceiver == null) {
            context.unregisterReceiver(sBatteryChangeReceiver);
            sBatteryChangeReceiver = null;
        }
    }

    public static boolean isCharging() {

        return !(sBatteryChangeReceiver == null || sBatteryChangeReceiver.isCharging());
    }

    public static int getCurrentBatteryLevel() {

        return sBatteryChangeReceiver != null ? sBatteryChangeReceiver.getCurrentLevel() : -1;
    }

    public static void getMac(IpScanner.OnScanListener listener) {
        IpScanner ipScanner = new IpScanner();
        ipScanner.startScan(listener);
    }
}
