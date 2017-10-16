package com.snail.antifake.deviceid.emulator;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.snail.antifake.deviceid.ShellAdbUtils;
import com.snail.antifake.deviceid.deviceid.DeviceIdUtil;
import com.snail.antifake.deviceid.deviceid.IPhoneSubInfoUtil;
import com.snail.antifake.deviceid.deviceid.ITelephonyUtil;
import com.snail.antifake.jni.PropertiesGet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Author: hzlishang
 * Data: 2017/7/7 上午9:07
 * Des:模拟器判断
 * version:
 */

public class EmuCheckUtil {

    public static boolean mayOnEmulator(Context context) {

        return mayOnEmulatorViaQEMU(context)
                || isEmulatorViaBuild(context)
                || isEmulatorFromCpu()
                || isFakeEmulatorFromIMEI(context)
                || isEmulatorFromDeviceId(context);

    }

    //只有获取的IMEI全部为000000才是模拟器，但是>6.0的某些国产rom 在未授权的情况下返回的是00000000000000，所以这个判断 只限定到<23，甚至这个条件将来会放弃
    public static boolean isEmulatorFromDeviceId(Context context) {
        return isAllZero(DeviceIdUtil.getDeviceId(context))
                && EmuCheckUtil.checkPermissionGranted(context, "android.permission.READ_PHONE_STATE");
    }

    private static boolean isAllZero(@NonNull String content) {
        if (TextUtils.isEmpty(content))//获取不到不能作为参考，可能因为权限拿不到
            return false;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) != '0') {
                return false;
            }
        }
        return true;
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

    // 查杀比较严格，放在最后，直接pass x86
    public static boolean isEmulatorFromCpu() {

        String cpuInfo = ShellAdbUtils.execCommand("cat /proc/cpuinfo",false).successMsg;
        return TextUtils.isEmpty(cpuInfo) || ((cpuInfo.contains("intel") || cpuInfo.contains("amd")));
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


    // 根据Qemu的一些特征信息判断
    public static boolean isEmulatorFromQemuFeatures(Context context) {

        return checkPipes()
                || checkQEmuDriverFile();
    }

    public static String[] known_pipes = {
            "/dev/socket/qemud",
            "/dev/qemu_pipe"
    };

    public static String[] known_qemu_drivers = {
            "goldfish"
    };

    //检测“/dev/socket/qemud”，“/dev/qemu_pipe”这两个通道

    public static boolean checkPipes() {
        for (int i = 0; i < known_pipes.length; i++) {
            String pipes = known_pipes[i];
            File qemu_socket = new File(pipes);
            if (qemu_socket.exists()) {
                return true;
            }
        }
        return false;
    }

    // 读取文件内容，然后检查已知QEmu的驱动程序的列表
    public static boolean checkQEmuDriverFile() {
        File driver_file = new File("/proc/tty/drivers");
        if (driver_file.exists() && driver_file.canRead()) {
            byte[] data = new byte[1024];  //(int)driver_file.length()
            try {
                InputStream inStream = new FileInputStream(driver_file);
                inStream.read(data);
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String driver_data = new String(data);
            for (String known_qemu_driver : known_qemu_drivers) {
                if (driver_data.indexOf(known_qemu_driver) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

}
