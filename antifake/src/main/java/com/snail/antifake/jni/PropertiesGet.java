package com.snail.antifake.jni;

/**
 * Author: snail
 * Data: 2017/7/20 上午9:11
 * Des:
 * version:
 */

public class PropertiesGet {

    static{
        System.loadLibrary("property_get");
    }

    private static native String native_get(String key);
    private static native String native_get(String key,String def);
    public static String getString(String key){
        return native_get(key);
    }
    public static String getString(String key,String def){
        return native_get(key,def);
    }

}
