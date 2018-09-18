package com.snail.antifake.deviceid.macaddress;

import android.app.Application;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.snail.antifake.deviceid.ShellAdbUtils;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * Author: snail
 * Data: 2017/7/12 上午9:54
 * Des:mac地址获取
 * version:
 */

public class MacAddressUtils {


    public static String getMacAddress(Context context) {

        String macAddress;
        if (!TextUtils.isEmpty(macAddress = getMacInfoByAdb())) {
            return macAddress;
        }

        if (!TextUtils.isEmpty(macAddress = getMacAddressByWlan0(context))) {
            return macAddress;
        }
        if (!TextUtils.isEmpty(macAddress = IWifiManagerUtil.getMacAddress(context))) {
            return macAddress;
        }
        return "";
    }

    public static String getMacInfoByAdb() {
        ShellAdbUtils.CommandResult commandResult = ShellAdbUtils.execCommand("cat /sys/class/net/wlan0/address", false);
        return commandResult.successMsg;
    }

    private static String getProp(Context context, String property) {
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");
            Method method = SystemProperties.getDeclaredMethod("native_get", String.class);
            Object[] params = new Object[1];
            params[0] = property;
            method.setAccessible(true);
            return (String) method.invoke(SystemProperties, params);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMacAddressByWlan0(Context context) {

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface iF = interfaces.nextElement();

            byte[] addr = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                try {
                    addr = iF.getHardwareAddress();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            if ((!iF.getDisplayName().equals(getProp(context, "wifi.interface"))) || addr == null || addr.length == 0) {
                continue;
            }

            StringBuilder buf = new StringBuilder();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            String mac = buf.toString();
            return mac;
        }
        return "";
    }

    public static String getConnectedWifiMacAddress(Application context) {
        String connectedWifiMacAddress = null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> wifiList;

        if (wifiManager != null) {
            wifiList = wifiManager.getScanResults();
            WifiInfo info = wifiManager.getConnectionInfo();
            if (wifiList != null && info != null) {
                for (int i = 0; i < wifiList.size(); i++) {
                    ScanResult result = wifiList.get(i);
                    if (!TextUtils.isEmpty(info.getBSSID()) && info.getBSSID().equals(result.BSSID)) {
                        connectedWifiMacAddress = result.BSSID;
                    }
                }
            }
        }
        return connectedWifiMacAddress;
    }
}
