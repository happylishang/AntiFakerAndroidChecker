package com.snail.antifake.deviceid.emulator;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.snail.antifake.deviceid.AndroidDeviceIMEIUtil;
import com.snail.antifake.deviceid.ShellAdbUtils;
import com.snail.antifake.deviceid.deviceid.DeviceIdUtil;
import com.snail.antifake.deviceid.deviceid.IPhoneSubInfoUtil;
import com.snail.antifake.deviceid.deviceid.ITelephonyUtil;
import com.snail.antifake.jni.EmulatorCheckService;
import com.snail.antifake.jni.PropertiesGet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Author: hzlishang
 * Data: 2017/7/7 上午9:07
 * version:
 */

public class EmuCheckUtil {

    public static boolean mayOnEmulator(Context context) {

        return mayOnEmulatorViaQEMU(context)
                || isEmulatorViaBuild(context)
                || isEmulatorFromAbi()
                || isEmulatorFromCpu();

    }

    public static boolean checkPermissionGranted(Context context, String permission) {

        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                final PackageInfo info = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                int targetSdkVersion = info.applicationInfo.targetSdkVersion;
                if (targetSdkVersion >= Build.VERSION_CODES.M) {
                    result = context.checkSelfPermission(permission)
                            == PackageManager.PERMISSION_GRANTED;
                } else {
                    result = PermissionChecker.checkSelfPermission(context, permission)
                            == PermissionChecker.PERMISSION_GRANTED;
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static boolean isEmulatorViaBuild(Context context) {

        if (!TextUtils.isEmpty(PropertiesGet.getString("ro.product.model"))
                && PropertiesGet.getString("ro.product.model").toLowerCase().contains("sdk")) {
            return true;
        }

        /**
         * ro.product.manufacturer likes unknown
         */
        if (!TextUtils.isEmpty(PropertiesGet.getString("ro.product.manufacturer"))
                && PropertiesGet.getString("ro.product.manufacture").toLowerCase().contains("unknown")) {
            return true;
        }

        /**
         * ro.product.device likes generic
         */
        if (!TextUtils.isEmpty(PropertiesGet.getString("ro.product.device"))
                && PropertiesGet.getString("ro.product.device").toLowerCase().contains("generic")) {
            return true;
        }

        return false;
    }


    //  qemu模拟器特征
    public static boolean mayOnEmulatorViaQEMU(Context context) {
        String qemu = PropertiesGet.getString("ro.kernel.qemu");
        return "1".equals(qemu);
    }

    //  判断是否存在作假，如果TelephonyManager获取非空，但是底层获取为null，说明直接在上层Hook了
    public static boolean isFakeEmulatorFromIMEI(Context context) {

        String deviceId = null;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
        }

        String deviceId1 = IPhoneSubInfoUtil.getDeviceId(context);
        String deviceId2 = ITelephonyUtil.getDeviceId(context);
        return !TextUtils.isEmpty(deviceId)
                && (TextUtils.isEmpty(deviceId1)
                && TextUtils.isEmpty(deviceId2));
    }

    public static String getFinalIMEI(Context context) {
        return DeviceIdUtil.getDeviceId(context);
    }


    public static String getIMEIT(Context context) {
        return ITelephonyUtil.getDeviceId(context);
    }

    public static String getIMEIP(Context context) {
        return IPhoneSubInfoUtil.getDeviceId(context);
    }

    public static boolean hasQemuSocket() {

        File qemuSocket = new File("/dev/socket/qemud");
        return qemuSocket.exists();
    }

    public static boolean hasQemuPipe() {
        File qemuPipe = new File("/dev/socket/qemud");
        return qemuPipe.exists();
    }

    public static String getEmulatorQEMUKernel() {
        return PropertiesGet.getString("ro.kernel.qemu");
    }


    // 查杀比较严格，放在最后，直接pass x86
    private static boolean isEmulatorFromCpu() {
        ShellAdbUtils.CommandResult commandResult = ShellAdbUtils.execCommand("cat /proc/cpuinfo", false);
        String cpuInfo = commandResult == null ? "" : commandResult.successMsg;
        return !TextUtils.isEmpty(cpuInfo) && ((cpuInfo.contains("intel") || cpuInfo.contains("amd")));
    }


    private static boolean isEmulatorFromAbi() {

        String abi= AndroidDeviceIMEIUtil.getCpuAbi();

        return !TextUtils.isEmpty(abi) && abi.contains("x86");
    }

    public static String getCpuInfo() {
        ShellAdbUtils.CommandResult commandResult = ShellAdbUtils.execCommand("cat /proc/cpuinfo", false);
        return commandResult == null ? "" : commandResult.successMsg;
    }

    public static String getQEmuDriverFileString() {
        File driver_file = new File("/proc/tty/drivers");
        StringBuilder stringBuilder = new StringBuilder();
        if (driver_file.exists() && driver_file.canRead()) {
            try {
                char[] data = new char[1024];  //(int)driver_file.length()
                InputStream inStream = new FileInputStream(driver_file);
                Reader in = new InputStreamReader(inStream, "UTF-8");
                for (; ; ) {
                    int rsz = in.read(data, 0, data.length);
                    if (rsz < 0) {
                        break;
                    }
                    stringBuilder.append(data, 0, rsz);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }
        return "";
    }


    public interface CheckEmulatorCallBack {

        void onCheckSuccess(boolean isEmulator);

        void onCheckFaild();
    }

}
