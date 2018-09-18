package com.snail.device;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.IEmulatorCheck;
import com.snail.device.jni.EmulatorCheckService;
import com.snail.device.jni.EmulatorDetectUtil;


public class MainActivity extends Activity {

    private Activity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        findViewById(R.id.btn_moni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, EmulatorCheckService.class);
                bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

            }
        });

        findViewById(R.id.btn_sycn_moni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < 10; i++) {
                    TextView textView = (TextView) findViewById(R.id.btn_sycn_moni);
                    textView.setText(" 是否模拟器 " + EmulatorDetectUtil.isEmulator());
                    Log.v("snail"," 是否模拟器 " + EmulatorDetectUtil.isEmulator());
                }
            }
        });
    }

    final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IEmulatorCheck IEmulatorCheck = com.android.internal.telephony.IEmulatorCheck.Stub.asInterface(service);
            if (IEmulatorCheck != null) {
                try {
                    Log.e("CacheCheck ", " start");
                    TextView textView = (TextView) findViewById(R.id.btn_moni);
                    boolean ret = IEmulatorCheck.isEmulator();
                    textView.setText(" 是否模拟器 " + ret);
                    unbindService(this);
                    Log.e("CacheCheck ", "" + ret);
                } catch (RemoteException e) {
                    Toast.makeText(MainActivity.this, "获取进程崩溃", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
//