package com.snail.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.snail.antifake.deviceid.AndroidDeviceIMEIUtil;
import com.snail.antifake.deviceid.androidid.IAndroidIdUtil;
import com.snail.antifake.deviceid.androidid.ISettingUtils;
import com.snail.antifake.deviceid.deviceid.DeviceIdUtil;
import com.snail.antifake.deviceid.deviceid.IPhoneSubInfoUtil;
import com.snail.antifake.deviceid.deviceid.ITelephonyUtil;
import com.snail.antifake.deviceid.emulator.EmuCheckUtil;
import com.snail.antifake.deviceid.macaddress.IWifiManagerUtil;
import com.snail.antifake.deviceid.macaddress.MacAddressUtils;
import com.snail.antifake.jni.EmulatorDetectUtil;
import com.snail.antifake.jni.PropertiesGet;
import com.snail.device.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private Activity mActivity;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mActivity = this;
        /**
         *  子进程检测是否是模拟器，不影响UI线程
         */
        binding.btnAsyncSimu.setOnClickListener(v -> EmuCheckUtil.checkEmulatorFromCache(getApplicationContext(),

                new EmuCheckUtil.CheckEmulatorCallBack() {
                    @Override
                    public void onCheckSuccess(boolean isEmulator) {
                        TextView textView = (TextView) findViewById(R.id.btn_async_simu);
                        textView.setText("  内存异步非UI进程获取是否模拟器 " + isEmulator);
                    }

                    @Override
                    public void onCheckFaild() {
                        TextView textView = (TextView) findViewById(R.id.btn_async_simu);
                        textView.setText("  内存异步非UI进程获取是否模拟器 失败");
                    }
                }));
        /**
         *  UI进程检测，可能影响UI线程
         */
        binding.btnSycnSycSimu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 100; i++) {
                    TextView textView = (TextView) findViewById(R.id.btn_sycn_syc_simu);
                    textView.setText(" 内存同步是否模拟器 " + EmulatorDetectUtil.isEmulator(MainActivity.this));
                }

            }
        });
//        特征值判断是否模拟器
        binding.btnSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.btn_sample);
                textView.setText("特征信息判断是否模拟器 " + AndroidDeviceIMEIUtil.isRunOnEmulator(MainActivity.this));
            }
        });
//        综合判断一次
        binding.btnSycnInteger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) findViewById(R.id.btn_sycn_integer);
                textView.setText("综合判断是否模拟器 " + EmulatorDetectUtil.isEmulatorFromAll(MainActivity.this));
            }
        });

        //获取其他硬件信息
        binding.btnHwinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestGetInfo();
            }
        });

        requestGetInfo();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        renderHWInfo();
    }


    @SuppressLint("SetTextI18n")
    public void requestGetInfo() {

        // 不同的版本不一样，4.3之前ITelephony没有getDeviceId
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    0);
        } else {
            renderHWInfo();
        }

    }

    @SuppressLint("SetTextI18n")
    private void renderHWInfo() {
        String apideviceId = null;
        try {
            apideviceId = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        } catch (Exception ignored) {
        }

        binding.tvDeviceid.setText(
                " \n \n设备信息 " +
                        "  \n\n最终方法获取IMEI  : " + AndroidDeviceIMEIUtil.getDeviceId(mActivity) + " 是否有效 " + AndroidDeviceIMEIUtil.isValidIMEI(AndroidDeviceIMEIUtil.getDeviceId(mActivity))
                        + "\n最终方法获取MAC地址 : " + AndroidDeviceIMEIUtil.getMacAddress(mActivity) + " 是否有效 " + AndroidDeviceIMEIUtil.isValidAddress(MacAddressUtils.getMacAddress(mActivity))
                        + "\n最终方法获取AndroidID : " + AndroidDeviceIMEIUtil.getAndroidId(mActivity)
                        + "\n最终方法获取序列号 : " + AndroidDeviceIMEIUtil.getSerialno()
                        + "\n特征值检测是否模拟器  : " + EmuCheckUtil.mayOnEmulator(mActivity)
                        + " \n\nIMEI详细信息: "
                        + " \n\n可Hook系统API获取Deviceid: " + apideviceId
                        + "\nProxy代理获取Deviceid level0 : " + IPhoneSubInfoUtil.getDeviceIdLevel0(mActivity)
                        + "\nProxy代理获取Deviceid level1 :" + IPhoneSubInfoUtil.getDeviceIdLevel1(mActivity)
                        + "\nProxy代理获取Deviceid level2 :" + IPhoneSubInfoUtil.getDeviceIdLevel2(mActivity)
                        + "\nITelephonyUtil获取DeviceId level0: " + ITelephonyUtil.getDeviceIdLevel0(mActivity)
                        + "\nITelephonyUtil获取DeviceId level1 : " + ITelephonyUtil.getDeviceIdLevel1(mActivity)
                        + "\nITelephonyUtil获取DeviceId level2 :" + ITelephonyUtil.getDeviceIdLevel2(mActivity)
                     );

        binding.tvAndroididMacSerial.setText("\n\n序列号信息 ：" +
                "  \n\n系统API反射获取序列号 ： " + SysAPIUtil.getSerialNumber(mActivity)
                + "\n系统API反射获取序列号 ： " + SysAPIUtil.getJavaSerialNumber(mActivity)
                + "\n直接通过 Build Serial " + Build.SERIAL
                + "\n通过ADB Build Serial " + AndroidDeviceIMEIUtil.getSerialno()
                + "\n直接native获取  Serial " + PropertiesGet.getString("ro.serialno")
                + "\n\nMAC地址信息 ："
                + "\n\n通过系统API获取MAC地址  ： " + SysAPIUtil.getMacAddress(mActivity)
                + "\nIwifmanager 获取mac level 0  ： " + IWifiManagerUtil.getMacAddress(mActivity)
                + "\n通过NetworkInterface获取MAC地址  ： " + MacAddressUtils.getMacAddressByWlan0(mActivity)
                + "\n通过ADB获取MAC地址  ： " + MacAddressUtils.getMacInfoByAdb()

                + "\n\nAndroidID信息 ："
                + "\n\n通过系统API获取ANDROID_ID (XPOSED可以HOOK)  ： " + SysAPIUtil.getAndroidId(mActivity)
                + "\n反射获取系统 ANDROID_IDISettingUtils  ： " + ISettingUtils.getAndroidProperty(mActivity, Settings.Secure.ANDROID_ID)
                + "\n反射获取系统 ANDROID_ID ISettingUtils level2  ： " + ISettingUtils.getAndroidPropertyLevel1(mActivity, Settings.Secure.ANDROID_ID)
                + "\n\n其他手机型号信息 ："
                + "\n系统API获取手机型号 （作假）  ： " + SysAPIUtil.getPhoneManufacturer()
                + "\nnative ro.product.manufacturer" + PropertiesGet.getString("ro.product.manufacturer")
                + "\nnative ro.product.model  " + PropertiesGet.getString("ro.product.model")
                + "\nnative ro.product.device " + PropertiesGet.getString("ro.product.device")
                + "\nnative ro.product.name" + PropertiesGet.getString("ro.product.name")
                + "\n\n"
                + "\n系统架构  " + PropertiesGet.getString("ro.product.cpu.abi")
                + "\n获取链接的路由器地址 " + MacAddressUtils.getConnectedWifiMacAddress(getApplication())


        );
    }
}
