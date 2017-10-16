package com.snail.antifake.deviceid.deviceid;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.snail.antifake.deviceid.BinderUtil;
import java.lang.reflect.Method;

/**
 * Author: hzlishang
 * Data: 17-7-6 下午6:38
 * Des: 新版本的可能需要ITelephony
 * version:
 */

public class ITelephonyUtil {


    public static String getDeviceId(Context context){

        String deviceId;
        if(!TextUtils.isEmpty(deviceId=getDeviceIdLevel2(context))
                ||!TextUtils.isEmpty(deviceId=getDeviceIdLevel1(context))
                ||!TextUtils.isEmpty(deviceId=getDeviceIdLevel0(context))){
            return deviceId;
        }

        return deviceId;
    }

    public static String getDeviceIdLevel0(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        try {
            Method method = TelephonyManager.class.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            Object binderProxy = method.invoke(telephonyManager);
            Class proxyClass = binderProxy.getClass();
            Method asBinder = proxyClass.getDeclaredMethod("asBinder");
            asBinder.setAccessible(true);
            try {
                Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId", String.class);
                if (getDeviceId != null) {
                    return (String) getDeviceId.invoke(binderProxy, context.getPackageName());
                }
            } catch (Exception e) {

            }
            Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId");
            if (getDeviceId != null) {
                return (String) getDeviceId.invoke(binderProxy);
            }
        } catch (Exception e) {
            //();
        }
        return "";
    }

    public static String getDeviceIdLevel1(Context context) {
        try {
            Class ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            IBinder binder = (IBinder) getService.invoke(null, Context.TELEPHONY_SERVICE);
            Class Stub = Class.forName("com.android.internal.telephony.ITelephony$Stub");
            Method asInterface = Stub.getDeclaredMethod("asInterface", IBinder.class);
            asInterface.setAccessible(true);
            Object binderProxy = asInterface.invoke(null, binder);
            try {
                Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId", String.class);
                if (getDeviceId != null) {
                    return (String) getDeviceId.invoke(binderProxy, context.getPackageName());
                }
            } catch (Exception e) {

            }
            Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId");
            if (getDeviceId != null) {
                return (String) getDeviceId.invoke(binderProxy);
            }
        } catch (Exception e) {
            //();
        }
        return "";
    }

    //风险  如果底层ROM改了ServiceManager，那就可能有问题，但是概率很小
    public static String getDeviceIdLevel2(Context context) {

        String deviceId = "";
        try {
            Class ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            IBinder binder = (IBinder) getService.invoke(null, Context.TELEPHONY_SERVICE);
            Class Stub = Class.forName("com.android.internal.telephony.ITelephony$Stub");
            Method asInterface = Stub.getDeclaredMethod("asInterface", IBinder.class);
            asInterface.setAccessible(true);
            Object binderProxy = asInterface.invoke(null, binder);
            try {
                Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId", String.class);
                if (getDeviceId != null) {
                    deviceId = binderGetHardwareInfo(context.getPackageName(),
                            binder, BinderUtil.getInterfaceDescriptor(binderProxy),
                            BinderUtil.getTransactionId(binderProxy, "TRANSACTION_getDeviceId"));
                }
            } catch (Exception e) {
            }
            Method getDeviceId = binderProxy.getClass().getDeclaredMethod("getDeviceId");
            if (getDeviceId != null) {
                deviceId = binderGetHardwareInfo("",
                        binder, BinderUtil.getInterfaceDescriptor(binderProxy),
                        BinderUtil.getTransactionId(binderProxy, "TRANSACTION_getDeviceId"));
            }
        } catch (Exception e) {
            //();
        }
        return deviceId;
    }

    private static String binderGetHardwareInfo(String callingPackage,
                                                IBinder remote,
                                                String DESCRIPTOR,
                                                int tid) throws RemoteException {

        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        String _result;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            if (!TextUtils.isEmpty(callingPackage)) {
                _data.writeString(callingPackage);
            }
            remote.transact(tid, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }


}
