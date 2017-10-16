package com.snail.antifake.deviceid.androidid;

import android.content.ContentResolver;
import android.content.Context;
import android.os.IBinder;
import android.os.Process;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * Author: hzlishang
 * Data: 17-7-13 上午10:47
 * Des:
 * version:
 */

public class ISettingUtils {

    public static String getAndroidProperty(Context context, String name) {
        try {
            Class mUserHandle = Class.forName("android.os.UserHandle");
            Method getUserId = mUserHandle.getDeclaredMethod("getUserId", int.class);
            getUserId.setAccessible(true);
            int uid = (int) getUserId.invoke(null, Process.myUid());
            Class mSecure = Class.forName("android.provider.Settings$Secure");
            Method getString = mSecure.getDeclaredMethod("getStringForUser", ContentResolver.class, String.class, int.class);
            getString.setAccessible(true);
            return (String) getString.invoke(null, context.getContentResolver(), name, uid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    //    getStringForUser 是一个隐藏方法，按理说 第三方不能随意修改，因为版本不一致，但是仍然可以修改，虽然可能导致ROM不稳定，但是定制ROM或者做插件的也许不关心
    public static String getAndroidPropertyLevel1(Context context, String name) {

        ContentResolver resolver = context.getContentResolver();
        try {
            Class mUserHandle = Class.forName("android.os.UserHandle");
            Method getUserId = mUserHandle.getDeclaredMethod("getUserId", int.class);
            getUserId.setAccessible(true);
            int uid = (int) getUserId.invoke(null, Process.myUid());

            HashSet<String> MOVED_TO_SECURE = new HashSet<>();
            HashSet<String> MOVED_TO_LOCK_SETTINGS = new HashSet<>();
            HashSet<String> MOVED_TO_GLOBAL = new HashSet<>();
            try {
                Class Global = Class.forName("android.provider.Settings$Global");
                Field field = Global.getDeclaredField("MOVED_TO_SECURE");
                field.setAccessible(true);
                MOVED_TO_SECURE = (HashSet<String>) field.get(Global);
            } catch (Exception e) {
            }
            try {
                Class Secure = Class.forName("android.provider.Settings$Secure");
                Field field = Secure.getDeclaredField("MOVED_TO_LOCK_SETTINGS");
                field.setAccessible(true);
                MOVED_TO_LOCK_SETTINGS = (HashSet<String>) field.get(Secure);
            } catch (Exception e) {
            }
            try {
                Class Secure = Class.forName("android.provider.Settings$Secure");
                Field field = Secure.getDeclaredField("MOVED_TO_GLOBAL");
                field.setAccessible(true);
                MOVED_TO_GLOBAL = (HashSet<String>) field.get(Secure);
            } catch (Exception e) {

            }

            if (MOVED_TO_SECURE.contains(name)) {

            } else if (MOVED_TO_GLOBAL.contains(name)) {
                Class mSecure = Class.forName("android.provider.Global");
                Method getStringForUser = mSecure.getDeclaredMethod("getStringForUser", ContentResolver.class, String.class, int.class);
                getStringForUser.setAccessible(true);
                return (String) getStringForUser.invoke(null, resolver, name, uid);
            } else if ((MOVED_TO_LOCK_SETTINGS.contains(name))) {
                Class ServiceManager = Class.forName("android.os.ServiceManager");
                Method getService = ServiceManager.getDeclaredMethod("getService");
                getService.setAccessible(true);
                IBinder binder = (IBinder) getService.invoke(null, "lock_settings");
                Class Stub = Class.forName("com.android.internal.widget.ILockSettings$Stub");
                Method asInterface = Stub.getDeclaredMethod("asInterface", IBinder.class);
                asInterface.setAccessible(true);
                Object binderProxy = asInterface.invoke(null, binder);
                boolean sIsSystemProcess = Process.myUid() == Process.SYSTEM_UID;
                if (MOVED_TO_LOCK_SETTINGS.contains(name)) {
                    if (binderProxy != null && !sIsSystemProcess) {
                        Class proxy = binderProxy.getClass();
                        Method getString = proxy.getDeclaredMethod("getString", String.class, String.class, int.class);
                        return (String) getString.invoke(name, "0", uid);
                    }
                }
            }
            Class Secure = Class.forName("android.provider.Settings$Secure");
            Field field = Secure.getDeclaredField("sNameValueCache");
            field.setAccessible(true);
            Object sNameValueCache = field.get(null);
            Class NameValueCache = sNameValueCache.getClass();
            Method getStringForUser = NameValueCache.getDeclaredMethod("getStringForUser", ContentResolver.class, String.class, int.class);
            return (String) getStringForUser.invoke(sNameValueCache, resolver, name, uid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

}
