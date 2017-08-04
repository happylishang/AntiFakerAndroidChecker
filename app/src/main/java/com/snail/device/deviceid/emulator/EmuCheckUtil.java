package com.snail.device.deviceid.emulator;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.snail.device.deviceid.deviceid.DeviceIdUtil;
import com.snail.device.deviceid.deviceid.IPhoneSubInfoUtil;
import com.snail.device.deviceid.deviceid.ITelephonyUtil;
import com.snail.device.jni.PropertiesGet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Author: hzlishang
 * Data: 2017/7/7 上午9:07
 * Des:模拟器判断
 * version:
 */

public class EmuCheckUtil {

    public static boolean mayOnEmulator(Context context) {

        mayOnEmulatorViaQEMU(context);
        DeviceIdUtil.isEmulatorFromDeviceId(context);
        isFakeEmulatorFromIMEI(context);
        mayOnEmulatorViaBuild(context);
        isEmulatorFromCpu();
        isEmulatorFromQemuFeatures(context);

        return mayOnEmulatorViaQEMU(context)
                || DeviceIdUtil.isEmulatorFromDeviceId(context)
                || isFakeEmulatorFromIMEI(context)
                || mayOnEmulatorViaBuild(context)
                || isEmulatorFromCpu()
                || isEmulatorFromQemuFeatures(context);


    }

    private static final boolean mayOnEmulatorViaBuild(Context context) {

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


    private static final boolean mayOnEmulatorViaQEMU(Context context) {
        String qemu = PropertiesGet.getString("ro.kernel.qemu");
        return "1".equals(qemu);
    }

    // 查杀比较严格
    private static boolean isEmulatorFromCpu() {

        String cpuInfo = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            StringBuffer sb = new StringBuffer();
            String readLine = "";
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine);
            }
            responseReader.close();
            cpuInfo = sb.toString().toLowerCase();
        } catch (IOException ex) {
        }
        return TextUtils.isEmpty(cpuInfo) || ((cpuInfo.contains("intel") || cpuInfo.contains("amd")));
    }


    //  判断是否存在作假，如果TelephonyManager获取非空，但是
    private static boolean isFakeEmulatorFromIMEI(Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        String deviceId1 = IPhoneSubInfoUtil.getDeviceId(context);
        String deviceId2 = ITelephonyUtil.getDeviceId(context);
        return !TextUtils.isEmpty(deviceId)
                && (TextUtils.isEmpty(deviceId1)
                && TextUtils.isEmpty(deviceId2));
    }

    private static boolean isEmulatorFromQemuFeatures(Context context) {
        return checkPipes()
                || checkQEmuDriverFile()
                || CheckEmulatorFiles()
                || CheckPhoneNumber(context);
    }

    private static String[] known_pipes = {
            "/dev/socket/qemud",
            "/dev/qemu_pipe"
    };

    private static String[] known_qemu_drivers = {
            "goldfish"
    };

    private static String[] known_files = {
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
    };

    private static String[] known_numbers = {"15555215554", "15555215556",
            "15555215558", "15555215560", "15555215562", "15555215564",
            "15555215566", "15555215568", "15555215570", "15555215572",
            "15555215574", "15555215576", "15555215578", "15555215580",
            "15555215582", "15555215584",};

    private static boolean checkPipes() {
        for (int i = 0; i < known_pipes.length; i++) {
            String pipes = known_pipes[i];
            File qemu_socket = new File(pipes);
            if (qemu_socket.exists()) {
                Log.v("Result:", "Find pipes!");
                return true;
            }
        }
        Log.i("Result:", "Not Find pipes!");
        return false;
    }

    private static boolean checkQEmuDriverFile() {
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
                    Log.i("Result:", "Find know_qemu_drivers!");
                    return true;
                }
            }
        }
        Log.i("Result:", "Not Find known_qemu_drivers!");
        return false;
    }

    //检测模拟器上特有的几个文件
    public static boolean CheckEmulatorFiles() {
        for (int i = 0; i < known_files.length; i++) {
            String file_name = known_files[i];
            File qemu_file = new File(file_name);
            if (qemu_file.exists()) {
                return true;
            }
        }
        return false;
    }

    // 检测模拟器默认的电话号码
    private static boolean CheckPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        String phonenumber = telephonyManager.getLine1Number();

        for (String number : known_numbers) {
            if (number.equalsIgnoreCase(phonenumber)) {
                return true;
            }
        }
        return false;
    }

}
