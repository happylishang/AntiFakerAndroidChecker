package com.snail.antifake.deviceid;

import android.os.RemoteException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: hzlishang
 * Data: 17-7-13 下午1:29
 * Des:
 * version:
 */

public class BinderUtil {



    //根据方法名，反射获得方法transactionId
    public static int getTransactionId(Object proxy,
                                        String name) throws RemoteException, NoSuchFieldException, IllegalAccessException {
        int transactionId = 0;
        Class outclass = proxy.getClass().getEnclosingClass();
        Field idField = outclass.getDeclaredField(name);
        idField.setAccessible(true);
        transactionId = (int) idField.get(proxy);
        return transactionId;
    }

    //根据方法名，反射获得方法transactionId
    public static String getInterfaceDescriptor(Object proxy) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getInterfaceDescriptor = proxy.getClass().getDeclaredMethod("getInterfaceDescriptor");
        return (String) getInterfaceDescriptor.invoke(proxy);
    }

}
