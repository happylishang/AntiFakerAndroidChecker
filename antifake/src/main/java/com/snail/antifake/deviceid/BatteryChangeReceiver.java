package com.snail.antifake.deviceid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * Author: hzlishang
 * Data: 17-8-18 下午1:54
 * Des:
 * version:
 */

public class BatteryChangeReceiver extends BroadcastReceiver {

    private boolean mIsCharging;
    private int mCurrentLevel;

    // 只有未充电，并且电量经常不变才看成模拟器，这个方法很容易造假，参考意义不大
    @Override
    public void onReceive(Context context, Intent intent) {


        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        mCurrentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

        switch (status) {

            case BatteryManager.BATTERY_STATUS_CHARGING:
                // 电池满不作为参考，看做充电中
            case BatteryManager.BATTERY_STATUS_FULL:
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                mIsCharging = true;
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                mIsCharging = false;
                break;
        }

//        Toast.makeText(context, " " + mCurrentLevel + " mIsCharging "+ mIsCharging, Toast.LENGTH_SHORT).show();
    }

    public boolean isCharging() {
        return mIsCharging;
    }

    public int getCurrentLevel() {
        return mCurrentLevel;
    }
}
