package com.snail.labaffinity;

import android.content.ContentResolver;
import android.net.wifi.WifiInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class MyXposedModule implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        Log.e("snail", "" + lpparam.packageName);

        findAndHookMethod(Settings.Secure.class.getName(), lpparam.classLoader, "getStringForUser",
                ContentResolver.class, String.class, int.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        super.afterHookedMethod(param);
                        String pa = (String) param.args[1];
                        Log.e("snail", "ANDROID_ID getStringForUser " + pa + " originnal " + param.getResult());
                        if (pa.equals(Settings.Secure.ANDROID_ID)) {

                            param.setResult("" + System.currentTimeMillis());
                        }

                    }

                });

        findAndHookMethod(Settings.Secure.class.getName(),
                lpparam.classLoader, "getString",
                ContentResolver.class, String.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.e("snail", "ANDROID_ID before called");
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.e("snail", "ANDROID_ID after called");
                        super.afterHookedMethod(param);
                        String pa = (String) param.args[1];
                        if (pa.equals(Settings.Secure.ANDROID_ID)) {
                            param.setResult("" + System.currentTimeMillis());
                        }
                    }

                });


        findAndHookMethod("android.provider.Settings$NameValueCache",
                lpparam.classLoader, "getStringForUser",
                ContentResolver.class, String.class, int.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.e("snail", "ANDROID_ID before called");
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.e("snail", "ANDROID_ID after called");
                        super.afterHookedMethod(param);
                        String pa = (String) param.args[1];
                        if (pa.equals(Settings.Secure.ANDROID_ID)) {
                            param.setResult("" + System.currentTimeMillis());
                        }
                    }

                });




        findAndHookMethod(NetworkInterface.class.getName(), lpparam.classLoader, "getHardwareAddress",
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e("snail", "before  getHardwareAddress called");
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e("snail", "after getHardwareAddress  called");
                        super.afterHookedMethod(param);
                        Random random = new Random();
                        byte[] bytes = new byte[]{1, 2, 3, 4, 6};
                        random.nextBytes(bytes);
                        param.setResult(bytes);
                    }

                });

        findAndHookMethod(WifiInfo.class.getName(), lpparam.classLoader, "getMacAddress", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // TODO 自动生成的方法存根
                super.afterHookedMethod(param);

                param.setResult("" + System.currentTimeMillis());
            }
        });

        findAndHookMethod("android.os.SystemProperties", lpparam.classLoader, "get", String.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // TODO 自动生成的方法存根
                super.afterHookedMethod(param);

                String pa = (String) param.args[0];

                if (pa.equals("ro.serialno")) {

                    param.setResult("" + System.currentTimeMillis());
                }
            }
        });

        try {
            findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getDeviceId",
                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // TODO 自动生成的方法存根
                            super.afterHookedMethod(param);

                            param.setResult("" + System.currentTimeMillis());
                            Log.e("snail", "after TelephonyManager called");
                        }
                    });
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Error e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            findAndHookMethod("com.android.internal.telephony.IPhoneSubInfo$Stub$Proxy", lpparam.classLoader,
                    "getDeviceId", new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // TODO 自动生成的方法存根
                            super.afterHookedMethod(param);
                            param.setResult("" + System.currentTimeMillis());
                            Log.e("snail", "after IPhoneSubInfo$Proxy called");
                        }
                    });

        } catch (Exception e) {
            // TODO: handle exception
        } catch (Error e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            findAndHookMethod("com.android.internal.telephony.IPhoneSubInfo$Stub$Proxy", lpparam.classLoader,
                    "getDeviceId", String.class, new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // TODO 自动生成的方法存根
                            super.afterHookedMethod(param);
                            param.setResult("" + System.currentTimeMillis());
                            Log.e("snail", "after IPhoneSubInfo$Proxy called");
                        }
                    });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Error e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            findAndHookMethod("com.android.internal.telephony.ITelephony$Stub$Proxy", lpparam.classLoader,
                    "getDeviceId", new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // TODO 自动生成的方法存根
                            super.afterHookedMethod(param);

                            param.setResult("" + System.currentTimeMillis());
                            Log.e("snail", "after ITelephony$Proxy called");
                        }
                    });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Error e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            findAndHookMethod("com.android.internal.telephony.ITelephony$Stub$Proxy", lpparam.classLoader,
                    "getDeviceId", String.class, new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // TODO 自动生成的方法存根
                            super.afterHookedMethod(param);

                            param.setResult("" + System.currentTimeMillis());
                            Log.e("snail", "after ITelephony$Proxy called");
                        }
                    });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Error e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
