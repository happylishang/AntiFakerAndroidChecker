package com.snail.device;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.IEmulatorCheck;
import com.snail.device.deviceid.androidid.IAndoidIdUtil;
import com.snail.device.deviceid.androidid.ISettingUtils;
import com.snail.device.deviceid.deviceid.DeviceIdUtil;
import com.snail.device.deviceid.deviceid.IPhoneSubInfoUtil;
import com.snail.device.deviceid.deviceid.ITelephonyUtil;
import com.snail.device.deviceid.emulator.EmuCheckUtil;
import com.snail.device.deviceid.macaddress.IWifiManagerUtil;
import com.snail.device.deviceid.macaddress.MacAddressUtils;
import com.snail.device.jni.EmulatorCheckService;
import com.snail.device.jni.EmulatorDetectUtil;
import com.snail.device.jni.PropertiesGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;


public class MainActivity extends AppCompatActivity {

    private Activity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        findViewById(R.id.btn_moni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, EmulatorCheckService.class);
                bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

            }
        });

        findViewById(R.id.btn_sycn_moni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView textView = (TextView) findViewById(R.id.btn_sycn_moni);
                textView.setText(" 是否模拟器 " + EmulatorDetectUtil.isEmulator());

            }
        });


        findViewById(R.id.btn_dna).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView textView = null;

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



                textView = (TextView) findViewById(R.id.tv_getdeviceid);
                // 不同的版本不一样，4.3之前ITelephony没有getDeviceId
                textView.setText("\n 最终方法获取IMEI  \n" + DeviceIdUtil.getDeviceId(mActivity)
                        + "\n最终方法获取MAC地址 \n" + MacAddressUtils.getMacAddress(mActivity)
                        + "\n最终方法获取AndroidID \n" + IAndoidIdUtil.getAndroidId(mActivity)
                        + "\n 是否模拟器  " + EmuCheckUtil.mayOnEmulator(mActivity)
                        + " \n\n可Hook系统API获取Deviceid\n" + ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()
                        + "\n 真实 反Hook Proxy代理获取Deviceid \n" + IPhoneSubInfoUtil.getDeviceIdLevel0(mActivity)
                        + "\n真实 反Hook Proxy代理获取Deviceid level1\n" + IPhoneSubInfoUtil.getDeviceIdLevel1(mActivity)
                        + "\n真实 反Hook Proxy代理获取Deviceid level2\n" + IPhoneSubInfoUtil.getDeviceIdLevel2(mActivity)
                        + "\n 真实 ITelephonyUtil反Hook 获取DeviceId\n" + ITelephonyUtil.getDeviceIdLevel0(mActivity)
                        + "\n 真实 ITelephonyUtil反Hook 获取DeviceId level1 \n" + ITelephonyUtil.getDeviceIdLevel1(mActivity)
                        + "\n 自定义ServiceManager获取getDeviceId level2 \n" + ITelephonyUtil.getDeviceIdLevel2(mActivity)
                        +"\n "+cpuInfo
                        +"\n "+ PropertiesGet.getString("ro.product.cpu.abi")
                );
                textView = (TextView) findViewById(R.id.tv_all);

                try {
                    Field seri = Build.class.getDeclaredField("SERIAL");
                    seri.setAccessible(true);
                    try {
                        seri.set(null, "" + System.currentTimeMillis());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

                textView.setText("\n系统API反射获取序列号\n" + SysAPIUtil.getSerialNumber(mActivity)
                        + "\n系统API反射获取序列号\n" + SysAPIUtil.getJavaSerialNumber(mActivity)
                        + "\n 直接通过 Build Serial " + Build.SERIAL
                        + "\n 直接native获取  Serial " + PropertiesGet.getString("ro.serialno")
                        + "\n 通过系统API获取MAC地址 \n" + SysAPIUtil.getMacAddress(mActivity)
                        + "\n Iwifmanager 获取mac level 0 \n" + IWifiManagerUtil.getMacAddress(mActivity)
                        + "\n 通过NetworkInterface获取MAC地址 \n" + MacAddressUtils.getMacAddressByWlan0(mActivity)
                        + "\n 系统API获取手机型号 （作假） \n" + SysAPIUtil.getPhoneManufacturer()
                        //Settings.Secure.ANDROID_ID Java类可以被HOOK 并且很简单
                        + "\n\n通过系统API获取ANDROID_ID (XPOSED可以HOOK) \n " + SysAPIUtil.getAndroidId(mActivity)
                        + "\n 反射获取系统 ANDROID_IDISettingUtils \n" + ISettingUtils.getAndroidProperty(mActivity, Settings.Secure.ANDROID_ID)
                        + "\n 反射获取系统 ANDROID_ID ISettingUtils level2 \n" + ISettingUtils.getAndroidPropertyLevel1(mActivity, Settings.Secure.ANDROID_ID)
                        + "\n native ro.product.manufacturer" + PropertiesGet.getString("ro.product.manufacturer")
                        + "\n native ro.product.model  " + PropertiesGet.getString("ro.product.model")
                        + "\n native ro.product.device " + PropertiesGet.getString("ro.product.device")
                        + "\n native ro.kernel.qemu " + PropertiesGet.getString("ro.kernel.qemu")
                        + "\n native ro.product.name" + PropertiesGet.getString("ro.product.name")


                );
            }
        });
    }

    private IEmulatorCheck mIEmulatorCheck;
    final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIEmulatorCheck = IEmulatorCheck.Stub.asInterface(service);
            if (mIEmulatorCheck != null) {
                try {
                    TextView textView = (TextView) findViewById(R.id.btn_moni);
                    textView.setText(" 是否模拟器 " + mIEmulatorCheck.isEmulator());
                    unbindService(this);
                    mIEmulatorCheck.kill();
                } catch (RemoteException e) {
                    Toast.makeText(MainActivity.this,"获取进程崩溃",Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

}