package com.snail.antifake.deviceid;

/**
 * Author: snail
 * Data: 2018/4/25.
 * Des:
 * version:
 */

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kalshen on 2017/7/5 0005.
 * ip 扫描类
 */

public class IpScanner {
    private Handler mHandler = new Handler(Looper.getMainLooper());//获取主线程的Looper

    /**
     * 获取局域网中的 存在的ip地址及对应的mac
     */
    public void startScan(final OnScanListener listener) {
        //局域网内存在的ip集合
        final List<String> ipList = new ArrayList<>();
        final Map<String, String> map = new HashMap<>();

        //获取本机所在的局域网地址
        String hostIP = getHostIP();
        int lastIndexOf = hostIP.lastIndexOf(".");
        final String substring = hostIP.substring(0, lastIndexOf + 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket dp = new DatagramPacket(new byte[0], 0, 0);
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket();
                    int position = 2;
                    while (position < 255) {
                        Log.e("kalshen", "run: udp-" + substring + position);
                        dp.setAddress(InetAddress.getByName(substring + String.valueOf(position)));
                        socket.send(dp);
                        position++;
                        if (position == 125) {//分两段掉包，一次性发的话，达到236左右，会耗时3秒左右再往下发
                            socket.close();
                            socket = new DatagramSocket();
                        }
                    }
                    socket.close();
                    execCatForArp(listener);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 执行 cat命令 查找android 设备arp表
     * arp表 包含ip地址和对应的mac地址
     */
    private void execCatForArp(final OnScanListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Map<String, String> map = new HashMap<>();
                    Process exec = Runtime.getRuntime().exec("cat proc/net/arp");
                    InputStream is = exec.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.e("kalshen", "run: " + line);
                        if (!line.contains("00:00:00:00:00:00") && !line.contains("IP")) {
                            String[] split = line.split("\\s+");
                            map.put(split[3], split[0]);
                            //                                Log.e("kalshen", "run: " + s1);
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.scan(map);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取本机 ip地址
     *
     * @return
     */
    private String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("kalshen", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }


    public interface OnScanListener {
        void scan(Map<String, String> resultMap);
    }

}