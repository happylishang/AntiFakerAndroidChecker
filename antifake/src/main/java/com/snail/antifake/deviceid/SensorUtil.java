package com.snail.antifake.deviceid;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Author: snail
 * Data: 2017/7/26 下午2:41
 * Des:
 * version:
 */

  class SensorUtil {
    public static String getSensorListInfo(Context context){
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder strLog = new StringBuilder();

        for (Sensor item : sensors) {
            strLog.append(" type = " + item.getType() );
            strLog.append(" name = " + item.getName());
        }
        return strLog.toString();
    }
}
