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

                TextView textView = (TextView) findViewById(R.id.btn_sycn_moni);
                textView.setText(" 是否模拟器 " + EmulatorDetectUtil.isEmulator());

            }
        });
    }

    final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IEmulatorCheck IEmulatorCheck = com.android.internal.telephony.IEmulatorCheck.Stub.asInterface(service);
            if (IEmulatorCheck != null) {
                try {
                    Log.e("CacheCheck ", " start"  );
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
//0000000000000000 <detect>:
//        0:	d10043ff 	sub	sp, sp, #0x10
//        4:	b9000fff 	str	wzr, [sp,#12]
//        8:	d100c3ff 	sub	sp, sp, #0x30
//        c:	a9027bfd 	stp	x29, x30, [sp,#32]
//
//        0000000000000010 <smc>:
//        10:	d2800000 	mov	x0, #0x0                   	// #0
//        14:	10ffffe1 	adr	x1, 10 <smc>
//  18:	d2800002 	mov	x2, #0x0                   	// #0
//          1c:	91000400 	add	x0, x0, #0x1
//          20:	f9400023 	ldr	x3, [x1]
//
//          0000000000000024 <code>:
//        24:	91000442 	add	x2, x2, #0x1
//        28:	10ffffe1 	adr	x1, 24 <code>
//  2c:	d1003021 	sub	x1, x1, #0xc
//          30:	f9000023 	str	x3, [x1]
//          34:	f100285f 	cmp	x2, #0xa
//          38:	5400008a 	b.ge	48 <out>
//  3c:	f100281f 	cmp	x0, #0xa
//          40:	5400004a 	b.ge	48 <out>
//  44:	17fffff8 	b	24 <code>
//
//0000000000000048 <out>:
//        48:	aa0203e0 	mov	x0, x2
//        4c:	a9427bfd 	ldp	x29, x30, [sp,#32]
//        50:	9100c3ff 	add	sp, sp, #0x30
//        54:	aa0003e0 	mov	x0, x0
//        58:	b9000fe0 	str	w0, [sp,#12]
//        5c:	b9400fe0 	ldr	w0, [sp,#12]
//        60:	910043ff 	add	sp, sp, #0x10
//        64:	d65f03c0 	ret
