package com.snail.device.jni;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.android.internal.telephony.IEmulatorCheck;

/**
 * Author: snail
 * Data: 2017/7/25 下午12:17
 * Des:
 * version:
 */

public class EmulatorCheckService extends Service {


    Handler mHandler = new Handler();
    private IEmulatorCheck.Stub mIEmulatorCheck = new  IEmulatorCheck.Stub() {

        @Override
        public boolean isEmulator() throws RemoteException {
            return EmulatorDetectUtil.isEmulator();
        }

        @Override
        public void kill() throws RemoteException {
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIEmulatorCheck;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 500);
    }
}
