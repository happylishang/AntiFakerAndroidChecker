// ITelephony.aidl
package com.android.internal.telephony;

// Declare any non-default types here with import statements
import android.net.wifi.WifiInfo;
interface ITelephony {

    String getDeviceId(String callingPackage);

    String getString(in String key, in String defaultValue, in int userId);

//    WifiInfo getConnectionInfo();
}